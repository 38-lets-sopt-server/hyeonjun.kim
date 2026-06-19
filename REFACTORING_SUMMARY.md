# 헥사고날 아키텍처 리팩토링 결과 정리

---

## 목차

1. [변경 전 vs 변경 후 구조 비교](#1-변경-전-vs-변경-후-구조-비교)
2. [파일별 역할 요약](#2-파일별-역할-요약)
3. [의존성 방향 변화](#3-의존성-방향-변화)
4. [HTTP 요청별 파일 흐름](#4-http-요청별-파일-흐름)

---

## 1. 변경 전 vs 변경 후 구조 비교

### 변경 전 (레이어드 아키텍처)

```
src/main/java/org/sopt/
├── controller/
│   ├── AuthController.java
│   ├── PostController.java
│   └── UserController.java
├── service/
│   ├── AuthService.java
│   ├── JwtService.java
│   ├── PostService.java
│   └── UserService.java
├── repository/
│   ├── PostRepository.java
│   ├── RefreshTokenRepository.java
│   └── UserRepository.java
├── domain/
│   ├── BaseTimeEntity.java    ← JPA 어노테이션이 섞인 엔티티
│   ├── BoardType.java
│   ├── Post.java              ← @Entity, @ManyToOne 등 JPA 오염
│   ├── RefreshToken.java
│   └── User.java
├── dto/
│   ├── request/               ← 모든 도메인 DTO가 한 곳에 뒤섞임
│   └── response/
├── exception/
├── filter/
│   └── JwtAuthFilter.java
├── validator/
│   └── PostValidator.java
└── config/
```

**문제점**: User, Post, Auth 코드가 같은 레이어 폴더에 뒤섞여 있고, 도메인 객체(`Post`, `User`)가 `@Entity`, `@ManyToOne` 같은 JPA 어노테이션에 의존합니다. `PostService`가 JPA Repository를 직접 의존해 기술 교체 시 비즈니스 코드 수정이 불가피합니다.

---

### 변경 후 (헥사고날 아키텍처)

```
src/main/java/org/sopt/
├── Main.java
│
├── common/                             ← 모든 도메인이 공유하는 공통 코드
│   ├── exception/
│   │   ├── ErrorCode.java
│   │   ├── NotFoundException.java
│   │   └── GlobalExceptionHandler.java
│   ├── persistence/
│   │   └── BaseJpaEntity.java          ← JPA Auditing 공통 엔티티
│   └── response/
│       └── BaseResponse.java
│
├── config/
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
│
├── post/                               ← 게시글 도메인
│   ├── domain/
│   │   ├── Post.java                   ← 순수 Java 클래스 (JPA 없음)
│   │   └── BoardType.java
│   ├── application/
│   │   ├── port/
│   │   │   ├── in/                     ← 입력 포트 (UseCase 인터페이스)
│   │   │   │   ├── CreatePostUseCase.java
│   │   │   │   ├── GetPostUseCase.java
│   │   │   │   ├── UpdatePostUseCase.java
│   │   │   │   └── DeletePostUseCase.java
│   │   │   └── out/                    ← 출력 포트 (Repository 인터페이스)
│   │   │       └── PostRepositoryPort.java
│   │   ├── dto/
│   │   │   ├── CreatePostCommand.java
│   │   │   └── UpdatePostCommand.java
│   │   └── service/
│   │       └── PostService.java        ← 4개의 UseCase를 구현
│   └── adapter/
│       ├── in/web/                     ← 인바운드 어댑터 (HTTP)
│       │   ├── PostController.java
│       │   ├── request/
│       │   │   ├── CreatePostRequest.java
│       │   │   └── UpdatePostRequest.java
│       │   └── response/
│       │       └── PostResponse.java
│       └── out/persistence/            ← 아웃바운드 어댑터 (JPA)
│           ├── PostJpaEntity.java      ← JPA 어노테이션은 여기에만
│           ├── PostJpaRepository.java
│           └── PostPersistenceAdapter.java
│
├── user/                               ← 유저 도메인 (동일한 구조)
│   ├── domain/
│   │   └── User.java
│   ├── application/
│   │   ├── port/
│   │   │   ├── in/
│   │   │   │   └── JoinUserUseCase.java
│   │   │   └── out/
│   │   │       └── UserRepositoryPort.java
│   │   ├── dto/
│   │   │   └── JoinUserCommand.java
│   │   └── service/
│   │       └── UserService.java
│   └── adapter/
│       ├── in/web/
│       │   ├── UserController.java
│       │   └── request/
│       │       └── CreateUserRequest.java
│       └── out/persistence/
│           ├── UserJpaEntity.java
│           ├── UserJpaRepository.java
│           └── UserPersistenceAdapter.java
│
└── auth/                               ← 인증 도메인
    ├── domain/
    │   └── RefreshToken.java           ← 순수 Java 클래스
    ├── application/
    │   ├── port/
    │   │   ├── in/
    │   │   │   ├── LoginUseCase.java
    │   │   │   ├── GetCurrentUserUseCase.java
    │   │   │   └── ReissueTokenUseCase.java
    │   │   └── out/
    │   │       ├── RefreshTokenRepositoryPort.java
    │   │       └── TokenProviderPort.java   ← JWT를 인터페이스로 추상화
    │   ├── dto/
    │   │   ├── TokenResult.java
    │   │   └── UserInfoResult.java
    │   └── service/
    │       └── AuthService.java
    └── adapter/
        ├── in/
        │   ├── web/
        │   │   ├── AuthController.java
        │   │   └── response/
        │   │       ├── TokenResponse.java
        │   │       └── UserInfoResponse.java
        │   └── security/
        │       └── JwtAuthFilter.java   ← TokenProviderPort에만 의존
        └── out/
            ├── persistence/
            │   ├── RefreshTokenJpaEntity.java
            │   ├── RefreshTokenJpaRepository.java
            │   └── RefreshTokenPersistenceAdapter.java
            └── jwt/
                └── JwtTokenAdapter.java ← JWT 구현체 (TokenProviderPort 구현)
```

---

## 2. 파일별 역할 요약

### common (공통)

| 파일 | 역할 | 이전 파일 |
|---|---|---|
| `common/exception/ErrorCode.java` | 에러 코드 enum | `exception/ErrorCode.java` |
| `common/exception/NotFoundException.java` | 404 예외 클래스 | `exception/NotFoundException.java` |
| `common/exception/GlobalExceptionHandler.java` | 전역 예외 처리 | `exception/GlobalExceptionHandler.java` |
| `common/persistence/BaseJpaEntity.java` | JPA Auditing 상위 클래스 | `domain/BaseTimeEntity.java` |
| `common/response/BaseResponse.java` | 공통 응답 래퍼 | `dto/response/BaseResponse.java` |

### post 도메인

| 레이어 | 파일 | 역할 |
|---|---|---|
| Domain | `post/domain/Post.java` | 순수 게시글 비즈니스 객체. JPA 어노테이션 없음 |
| Domain | `post/domain/BoardType.java` | 게시판 종류 enum |
| Input Port | `application/port/in/CreatePostUseCase.java` | "게시글 작성" 계약 인터페이스 |
| Input Port | `application/port/in/GetPostUseCase.java` | "게시글 조회" 계약 인터페이스 |
| Input Port | `application/port/in/UpdatePostUseCase.java` | "게시글 수정" 계약 인터페이스 |
| Input Port | `application/port/in/DeletePostUseCase.java` | "게시글 삭제" 계약 인터페이스 |
| Output Port | `application/port/out/PostRepositoryPort.java` | "게시글 저장소" 계약 인터페이스 |
| App DTO | `application/dto/CreatePostCommand.java` | 게시글 작성 입력 데이터 (UseCase용) |
| App DTO | `application/dto/UpdatePostCommand.java` | 게시글 수정 입력 데이터 (UseCase용) |
| Service | `application/service/PostService.java` | 4개 UseCase 구현. 비즈니스 로직 담당 |
| Web Adapter | `adapter/in/web/PostController.java` | HTTP 요청 → UseCase 호출 |
| Web Adapter | `adapter/in/web/request/CreatePostRequest.java` | HTTP 요청 JSON → Command 변환 |
| Web Adapter | `adapter/in/web/request/UpdatePostRequest.java` | HTTP 요청 JSON → Command 변환 |
| Web Adapter | `adapter/in/web/response/PostResponse.java` | Post 도메인 → HTTP 응답 JSON 변환 |
| Persistence Adapter | `adapter/out/persistence/PostJpaEntity.java` | JPA 전용 엔티티. 도메인 ↔ JPA 변환 |
| Persistence Adapter | `adapter/out/persistence/PostJpaRepository.java` | Spring Data JPA 인터페이스 |
| Persistence Adapter | `adapter/out/persistence/PostPersistenceAdapter.java` | PostRepositoryPort를 JPA로 구현 |

### user 도메인

| 레이어 | 파일 | 역할 |
|---|---|---|
| Domain | `user/domain/User.java` | 순수 유저 비즈니스 객체 |
| Input Port | `application/port/in/JoinUserUseCase.java` | "회원가입" 계약 인터페이스 |
| Output Port | `application/port/out/UserRepositoryPort.java` | "유저 저장소" 계약 인터페이스 |
| App DTO | `application/dto/JoinUserCommand.java` | 회원가입 입력 데이터 |
| Service | `application/service/UserService.java` | JoinUserUseCase 구현. 비밀번호 암호화 |
| Web Adapter | `adapter/in/web/UserController.java` | POST /users 처리 |
| Web Adapter | `adapter/in/web/request/CreateUserRequest.java` | HTTP 요청 → JoinUserCommand 변환 |
| Persistence Adapter | `adapter/out/persistence/UserJpaEntity.java` | JPA 전용 엔티티 |
| Persistence Adapter | `adapter/out/persistence/UserJpaRepository.java` | Spring Data JPA 인터페이스 |
| Persistence Adapter | `adapter/out/persistence/UserPersistenceAdapter.java` | UserRepositoryPort를 JPA로 구현 |

### auth 도메인

| 레이어 | 파일 | 역할 |
|---|---|---|
| Domain | `auth/domain/RefreshToken.java` | 순수 리프레시 토큰 객체. rotate/isExpired 로직 포함 |
| Input Port | `application/port/in/LoginUseCase.java` | "로그인" 계약 인터페이스 |
| Input Port | `application/port/in/GetCurrentUserUseCase.java` | "내 정보 조회" 계약 인터페이스 |
| Input Port | `application/port/in/ReissueTokenUseCase.java` | "토큰 재발급" 계약 인터페이스 |
| Output Port | `application/port/out/RefreshTokenRepositoryPort.java` | "리프레시토큰 저장소" 계약 인터페이스 |
| Output Port | `application/port/out/TokenProviderPort.java` | "토큰 생성/검증" 계약 인터페이스 (JWT 추상화) |
| App DTO | `application/dto/TokenResult.java` | 토큰 쌍 (accessToken, refreshToken) |
| App DTO | `application/dto/UserInfoResult.java` | 유저 정보 결과 (id, email) |
| Service | `application/service/AuthService.java` | 3개 UseCase 구현. 로그인/재발급/조회 |
| Web Adapter | `adapter/in/web/AuthController.java` | /api/v1/auth/* 엔드포인트 처리 |
| Web Adapter | `adapter/in/web/response/TokenResponse.java` | TokenResult → HTTP 응답 변환 |
| Web Adapter | `adapter/in/web/response/UserInfoResponse.java` | UserInfoResult → HTTP 응답 변환 |
| Security Adapter | `adapter/in/security/JwtAuthFilter.java` | 요청마다 JWT 검증. SecurityContext에 인증 정보 저장 |
| Persistence Adapter | `adapter/out/persistence/RefreshTokenJpaEntity.java` | JPA 전용 엔티티 |
| Persistence Adapter | `adapter/out/persistence/RefreshTokenJpaRepository.java` | Spring Data JPA 인터페이스 |
| Persistence Adapter | `adapter/out/persistence/RefreshTokenPersistenceAdapter.java` | RefreshTokenRepositoryPort를 JPA로 구현 |
| JWT Adapter | `adapter/out/jwt/JwtTokenAdapter.java` | TokenProviderPort를 JWT 라이브러리로 구현 |

---

## 3. 의존성 방향 변화

### 변경 전

```
PostController → PostService → PostRepository (JPA 인터페이스)
                    ↓
               UserService → UserRepository (JPA 인터페이스)
```

- `PostService`가 JPA `PostRepository`를 직접 알고 있음
- `PostService`가 `UserService`를 직접 알고 있음 (서비스 간 직접 의존)

### 변경 후

```
[PostController]
      │ (implements: PostController → UseCase interface)
      ▼
[CreatePostUseCase / GetPostUseCase / ...]   ← 인터페이스 (Input Port)
      │ (implements: PostService → these interfaces)
      ▼
[PostService]
      │ (uses: PostRepositoryPort interface)
      ▼
[PostRepositoryPort]     ← 인터페이스 (Output Port)
      │ (implements: PostPersistenceAdapter → this interface)
      ▼
[PostPersistenceAdapter] → PostJpaRepository → DB

[PostService]
      │ (uses: UserRepositoryPort interface)
      ▼
[UserRepositoryPort]     ← 인터페이스 (Output Port)
      │ (implements: UserPersistenceAdapter → this interface)
      ▼
[UserPersistenceAdapter] → UserJpaRepository → DB
```

**모든 의존성이 도메인을 향해 흐릅니다.** 도메인(`Post`, `User`, `RefreshToken`)은 JPA도, Spring도, HTTP도 모릅니다.

---

## 4. HTTP 요청별 파일 흐름

> 모든 요청은 먼저 `JwtAuthFilter` → `SecurityConfig` 순서를 거칩니다.

---

### POST /users — 회원가입

```
HTTP POST /users { "nickname": "...", "email": "...", "password": "..." }
    │
    ├─ [JwtAuthFilter]                  Authorization 헤더 없음 → 인증 없이 통과
    ├─ [SecurityConfig]                 /users 는 permitAll → 인증 불필요
    │
    ├─ [UserController.join()]
    │       ↓
    │   CreateUserRequest.toCommand()   → JoinUserCommand 생성
    │       ↓
    ├─ [JoinUserUseCase.join(command)]
    │       ↓ (구현체: UserService)
    ├─ [UserService.join(command)]
    │       BCryptPasswordEncoder.encode(password)
    │       new User(nickname, email, encodedPassword)
    │       ↓
    ├─ [UserRepositoryPort.save(user)]
    │       ↓ (구현체: UserPersistenceAdapter)
    ├─ [UserPersistenceAdapter.save(user)]
    │       UserJpaEntity.from(user)
    │       ↓
    └─ [UserJpaRepository.save(entity)] → DB INSERT

응답: 201 Created
```

---

### POST /api/v1/auth/login — 로그인

```
HTTP POST /api/v1/auth/login?email=...&password=...
    │
    ├─ [JwtAuthFilter]                  토큰 없음 → 인증 없이 통과
    ├─ [SecurityConfig]                 /api/v1/auth/login 은 permitAll
    │
    ├─ [AuthController.login(email, password)]
    │       ↓
    ├─ [LoginUseCase.login(email, password)]
    │       ↓ (구현체: AuthService)
    ├─ [AuthService.login(email, password)]
    │   ├─ [UserRepositoryPort.findByEmail(email)]
    │   │       ↓ (UserPersistenceAdapter → UserJpaRepository → DB SELECT)
    │   │   User 도메인 반환
    │   │
    │   ├─ BCryptPasswordEncoder.matches(입력PW, 저장된PW) — 비밀번호 검증
    │   │
    │   ├─ [TokenProviderPort.generateAccessToken(userId, email)]
    │   │       ↓ (JwtTokenAdapter) → JWT Access Token 생성
    │   │
    │   ├─ [TokenProviderPort.generateRefreshToken(userId)]
    │   │       ↓ (JwtTokenAdapter) → JWT Refresh Token 생성
    │   │
    │   ├─ [RefreshTokenRepositoryPort.deleteByUserId(userId)]
    │   │       ↓ (RefreshTokenPersistenceAdapter → DB DELETE 기존 토큰)
    │   │
    │   ├─ RefreshToken.create(userId, token, expiresIn)   — 도메인 객체 생성
    │   └─ [RefreshTokenRepositoryPort.save(refreshToken)]
    │           ↓ (RefreshTokenPersistenceAdapter → DB INSERT)
    │
    │   TokenResult(accessToken, refreshToken)
    │       ↓
    └─ TokenResponse.from(result)

응답: 200 OK { "accessToken": "...", "refreshToken": "..." }
```

---

### GET /api/v1/auth/me — 내 정보 조회

```
HTTP GET /api/v1/auth/me
Authorization: Bearer {accessToken}
    │
    ├─ [JwtAuthFilter]
    │       header = "Bearer eyJ..."
    │       token = "eyJ..."
    │       [TokenProviderPort.verifyAndGetUserId(token)]
    │           ↓ (JwtTokenAdapter.verifyAndGetUserId) → JWT 서명 검증 → userId 추출
    │       UsernamePasswordAuthenticationToken(userId, ...) → SecurityContextHolder 저장
    │
    ├─ [SecurityConfig]                 anyRequest().authenticated() → 인증 확인 → 통과
    │
    ├─ [AuthController.me(authentication)]
    │       userId = Long.parseLong(authentication.getName())
    │       ↓
    ├─ [GetCurrentUserUseCase.getCurrentUser(userId)]
    │       ↓ (구현체: AuthService)
    ├─ [AuthService.getCurrentUser(userId)]
    │   └─ [UserRepositoryPort.findById(userId)]
    │           ↓ (UserPersistenceAdapter → UserJpaRepository → DB SELECT)
    │       UserInfoResult(id, email)
    │           ↓
    └─ UserInfoResponse.from(result)

응답: 200 OK { "id": 1, "email": "user@example.com" }
```

---

### POST /api/v1/auth/reissue — 토큰 재발급

```
HTTP POST /api/v1/auth/reissue
Authorization: Bearer {refreshToken}
    │
    ├─ [JwtAuthFilter]                  Refresh Token은 Access Token과 구조가 다를 수 있어
    │                                   검증 실패 시 인증 없이 통과 (재발급 API는 permitAll)
    ├─ [SecurityConfig]                 /api/v1/auth/reissue 는 permitAll
    │
    ├─ [AuthController.reissue(authorization)]
    │       refreshToken = authorization.substring("Bearer ".length())
    │       ↓
    ├─ [ReissueTokenUseCase.reissue(refreshToken)]
    │       ↓ (구현체: AuthService)
    ├─ [AuthService.reissue(refreshTokenValue)]
    │   ├─ [RefreshTokenRepositoryPort.findByToken(token)]
    │   │       ↓ (DB SELECT) → RefreshToken 도메인 반환
    │   │
    │   ├─ refreshToken.isExpired()      만료 여부 확인 (도메인 로직)
    │   │
    │   ├─ [UserRepositoryPort.findById(refreshToken.getUserId())]
    │   │       ↓ (DB SELECT) → User 도메인 반환
    │   │
    │   ├─ [TokenProviderPort.generateAccessToken/generateRefreshToken]
    │   │       ↓ (JwtTokenAdapter) → 새 토큰 생성
    │   │
    │   ├─ refreshToken.rotate(newToken, expiresIn)  도메인 상태 변경 (토큰 교체)
    │   └─ [RefreshTokenRepositoryPort.save(refreshToken)]
    │           ↓ (DB UPDATE)
    │
    └─ TokenResponse.from(result)

응답: 200 OK { "accessToken": "새토큰...", "refreshToken": "새토큰..." }
```

---

### POST /posts — 게시글 작성

```
HTTP POST /posts { "title": "...", "content": "...", "boardType": "FREE" }
Authorization: Bearer {accessToken}
    │
    ├─ [JwtAuthFilter]                  토큰 검증 → SecurityContextHolder에 userId 저장
    ├─ [SecurityConfig]                 POST /posts 는 authenticated() → 인증 확인
    │
    ├─ [PostController.createPost(request, authentication)]
    │       userId = Long.parseLong(authentication.getName())
    │       CreatePostRequest.toCommand(userId)   → CreatePostCommand
    │       ↓
    ├─ [CreatePostUseCase.createPost(command)]
    │       ↓ (구현체: PostService)
    ├─ [PostService.createPost(command)]
    │   ├─ validateContent(title, content)         제목/내용 유효성 검증
    │   │
    │   ├─ [UserRepositoryPort.findById(userId)]
    │   │       ↓ (UserPersistenceAdapter → DB SELECT) → User 도메인 반환
    │   │
    │   ├─ new Post(title, content, boardType, user)  도메인 객체 생성
    │   └─ [PostRepositoryPort.save(post)]
    │           ↓ (구현체: PostPersistenceAdapter)
    │
    ├─ [PostPersistenceAdapter.save(post)]
    │   ├─ UserJpaRepository.findById(post.getAuthor().getId()) → UserJpaEntity 조회
    │   ├─ PostJpaEntity.from(post, userJpaEntity)              → JPA 엔티티 변환
    │   └─ PostJpaRepository.save(entity)                       → DB INSERT
    │
응답: 201 Created
```

---

### GET /posts — 게시글 목록 조회

```
HTTP GET /posts?page=0&size=10&boardType=FREE
    │
    ├─ [JwtAuthFilter]                  토큰 없으면 인증 없이 통과
    ├─ [SecurityConfig]                 GET /posts 는 permitAll
    │
    ├─ [PostController.getAllPosts(page, size, boardType)]
    │       ↓
    ├─ [GetPostUseCase.getAllPosts(page, size, boardType)]
    │       ↓ (구현체: PostService)
    ├─ [PostService.getAllPosts(page, size, boardType)]
    │   └─ [PostRepositoryPort.findAllByBoardType(boardType)]  (boardType 있는 경우)
    │           ↓ (PostPersistenceAdapter)
    │       [PostJpaRepository.findAllByBoardType(boardType)]  → DB SELECT
    │       PostJpaEntity::toDomain                            → List<Post> 반환
    │
    └─ PostResponse::from (각 Post → PostResponse 변환)

응답: 200 OK [{ "id": 1, "title": "...", "author": "닉네임", ... }]
```

---

### GET /posts/{id} — 게시글 단건 조회

```
HTTP GET /posts/1
    │
    ├─ [JwtAuthFilter]                  토큰 없으면 인증 없이 통과
    ├─ [SecurityConfig]                 GET /posts/** 는 permitAll
    │
    ├─ [PostController.getPost(id)]
    │       ↓
    ├─ [GetPostUseCase.getPost(id)]
    │       ↓ (PostService)
    ├─ [PostService.getPost(id)]
    │   └─ [PostRepositoryPort.findById(id)]
    │           ↓ (PostPersistenceAdapter → PostJpaRepository → DB SELECT)
    │       PostJpaEntity.toDomain() → Post 도메인 반환
    │       없으면 NotFoundException(POST_NOT_FOUND)
    │
    └─ PostResponse.from(post)

응답: 200 OK { "id": 1, "title": "...", "content": "...", "author": "..." }
```

---

### PUT /posts/{id} — 게시글 수정

```
HTTP PUT /posts/1 { "title": "새제목", "content": "새내용" }
Authorization: Bearer {accessToken}
    │
    ├─ [JwtAuthFilter]                  토큰 검증 → userId 저장
    ├─ [SecurityConfig]                 PUT /posts/** 는 authenticated()
    │
    ├─ [PostController.updatePost(id, request)]
    │       UpdatePostRequest.toCommand(id)  → UpdatePostCommand(postId=1, ...)
    │       ↓
    ├─ [UpdatePostUseCase.updatePost(command)]
    │       ↓ (PostService)
    ├─ [PostService.updatePost(command)]
    │   ├─ validateContent(title, content)         유효성 검증
    │   ├─ [PostRepositoryPort.findById(postId)]
    │   │       ↓ (DB SELECT) → Post 도메인 반환
    │   │
    │   ├─ post.update(title, content)             도메인 상태 변경
    │   └─ [PostRepositoryPort.save(post)]
    │           ↓ (PostPersistenceAdapter)
    │
    ├─ [PostPersistenceAdapter.save(post)]         (post.getId() != null → UPDATE)
    │   ├─ UserJpaRepository.findById(authorId)    → UserJpaEntity 조회
    │   ├─ PostJpaEntity.from(post, userJpaEntity) → JPA 엔티티 생성 (id 포함)
    │   └─ PostJpaRepository.save(entity)          → DB UPDATE (merge)
    │
응답: 200 OK
```

---

### DELETE /posts/{id} — 게시글 삭제

```
HTTP DELETE /posts/1
Authorization: Bearer {accessToken}
    │
    ├─ [JwtAuthFilter]                  토큰 검증 → userId 저장
    ├─ [SecurityConfig]                 DELETE /posts/** 는 authenticated()
    │
    ├─ [PostController.deletePost(id)]
    │       ↓
    ├─ [DeletePostUseCase.deletePost(id)]
    │       ↓ (PostService)
    ├─ [PostService.deletePost(id)]
    │   ├─ [PostRepositoryPort.findById(id)]
    │   │       ↓ (DB SELECT) → Post 도메인 반환
    │   │       없으면 NotFoundException(POST_NOT_FOUND)
    │   │
    │   └─ [PostRepositoryPort.delete(post)]
    │           ↓ (PostPersistenceAdapter)
    │       PostJpaRepository.deleteById(post.getId()) → DB DELETE
    │
응답: 200 OK
```

---

## 핵심 변경 포인트 요약

| 항목 | 변경 전 | 변경 후 |
|---|---|---|
| 도메인 객체 | `@Entity`, `@ManyToOne` 등 JPA 오염 | 순수 Java 클래스 |
| 비즈니스 로직 위치 | Service가 JPA 기술에 직접 의존 | UseCase 인터페이스 + Service 구현 |
| DB 접근 추상화 | 없음 (JPA 직접 사용) | `*RepositoryPort` 인터페이스 |
| JWT 추상화 | `JwtService` 직접 의존 | `TokenProviderPort` 인터페이스 |
| 도메인 경계 | 기술별 패키지 (controller/, service/...) | 도메인별 패키지 (post/, user/, auth/) |
| 테스트 용이성 | JPA 없이 Service 단위 테스트 어려움 | Port를 Mock으로 교체해 순수 단위 테스트 가능 |
| 기술 교체 | JPA→MyBatis 시 Service 코드 수정 필요 | `*PersistenceAdapter`만 교체하면 됨 |
