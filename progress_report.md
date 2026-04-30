# SOPT 38기 서버 파트 — 과제 진행 현황 보고서

> 생성일: 2026-04-30

---

## 전체 요약

| 구분 | 완료 | 미완료 | 부분완료 |
|------|:----:|:------:|:-------:|
| 1주차 전용 필수 | 0 | 1 | 0 |
| 1주차 전용 심화 | 1 | 0 | 0 |
| 1→2주차 전환 항목 | 4 | 0 | 3 |
| 2주차 전용 필수 | 0 | 0 | 2 (PR 확인 필요) |
| 2주차 전용 심화 | 1 | 2 | 1 |

---

## 전환 항목 상세 (1주차 기반 → 2주차 완성)

### CRUD 전환
- **1주차 기반**: ✅ `PostService`에 `getAllPosts()`, `getPost(id)`, `updatePost()`, `deletePost()` 모두 구현됨
- **2주차 완성**: ✅ `PostController`에 `@GetMapping`(전체/단건), `@PutMapping("/{id}")`, `@DeleteMapping("/{id}")` 모두 매핑됨

### PostNotFoundException 전환
- **1주차 기반**: ✅ `PostNotFoundException.java` 클래스 존재
- **2주차 완성**: ⚠️ **구조적 불일치** — `GlobalExceptionHandler`에 `@ExceptionHandler(PostNotFoundException.class)` 핸들러가 존재하고 HTTP 404를 반환하나, 실제 `PostService`는 `PostNotFoundException`이 아닌 `NotFoundException`을 throw함. `NotFoundException`에 대한 핸들러는 등록되어 있지 않아 런타임에 이 핸들러가 실제로 호출되지 않음.

### ApiResponse\<T\> 전환
- **1주차 기반**: ✅ `ApiResponse<T>` 클래스(`success`, `fail` 정적 팩토리 메서드 포함) 존재
- **2주차 완성**: ⚠️ **부분 적용** — `POST`, `GET`, `PUT` 메서드는 `ResponseEntity<ApiResponse<T>>`로 감싸져 있으나, `deletePost()`가 `ResponseEntity.noContent().build()`를 반환하여 `ApiResponse` 미포함

### 에러 코드 체계
- **1주차 기반**: (별도 항목 없음)
- **2주차 완성**: ✅ `ErrorCode.java` enum에 `POST_001("게시글을 찾을 수 없습니다.")`, `USER_001("사용자를 찾을 수 없습니다.")` 정의됨. `HttpStatus`, 코드 문자열, 메시지 3가지 필드 포함

---

## 1주차 전용 항목

### ❌ 미완료
- **화면설계서 분석 문서**: `README.md`에 배너 이미지만 존재, `docs/` 폴더 없음. 저장할 데이터·요청/응답 형태·검증 조건을 명시한 별도 파일 없음

### ✅ 완료
- **PostValidator 분리**: `src/main/java/org/sopt/validator/PostValidator.java` 존재. `PostService` 외부에 독립된 정적 유틸리티 클래스로 분리되어 있으며 title(null·blank·50자 초과), content(null·blank) 검증 담당. `PostService`에서 `PostValidator.validate(title, content)` 형태로 호출됨

---

## 2주차 전용 항목

### PR에서 직접 확인 필요
- **노션 API 명세서 링크**: `.github/PULL_REQUEST_TEMPLATE.md` 템플릿만 확인됨. 실제 PR 내용은 파일로 확인 불가 → PR에서 직접 확인 필요
- **Postman 테스트 캡처**: 프로젝트 내 이미지 폴더·README에 캡처 흔적 없음 → PR 첨부파일에서 직접 확인 필요

### ✅ 완료
- **ErrorCode enum**: `exception/ErrorCode.java`에 `HttpStatus`, 코드 문자열, 메시지 3필드 포함. `POST_001`, `USER_001` 케이스 정의됨

### ❌ 미완료
- **Pagination**: `PostController.getAllPosts()`에 `@RequestParam page, size` 파라미터 없음. 단순 `findAll()` 전체 반환
- **BoardType enum + 게시판별 조회**: `BoardType` enum 없음, `Post` 도메인에 `boardType` 필드 없음, 게시판 필터링 API 없음

### ⚠️ 부분완료
- **PostRepository 인터페이스 + 구현체 분리**: `PostRepository`가 `JpaRepository<Post, Long>`을 상속하는 인터페이스로 선언됨 (방향은 맞음). 그러나 파일 내부에 이전 In-Memory 구현 코드(`postList`, `findAll()`, `findById()` 등)가 잔존하여 **컴파일 에러 발생**. 심화 과제 의도인 InMemoryPostRepository 별도 구현체 분리는 미완성

---

## 컴파일/런타임 버그 (즉시 수정 필요)

현재 코드는 빌드 자체가 실패할 수 있는 결함이 다수 존재합니다.

### 🔴 심각

**① `PostRepository.java` — 인터페이스에 인스턴스 필드·메서드 바디 혼재**
- `interface` 선언이지만 내부에 `private final List<Post> postList = new ArrayList<>()` 필드와 메서드 구현체가 남아있어 Java 문법 위반 → 컴파일 불가
- **수정**: In-Memory 코드 전체 삭제, `JpaRepository` 상속 인터페이스만 유지

**② `PostResponse.java` — 삭제된 메서드 호출**
- `PostResponse.from(Post post)` 내에서 `post.getAuthor()`, `post.getCreatedAt()` 호출
- `Post` 엔티티에서 해당 필드/메서드가 주석 처리되어 존재하지 않음 → 컴파일 에러
- **수정**: `post.getUser().getNickname()` 활용, `createdAt` 필드 `Post`에 복원 또는 응답에서 제거

**③ `CreatePostRequest` — 타입 불일치**
- `record CreatePostRequest(String title, String content, String author)` — `author`가 `String`
- `PostService.createPost(String title, String content, Long userId)` — 세 번째 파라미터가 `Long`
- `PostController`에서 `request.author()`를 `createPost()`에 전달하면 타입 불일치 → 컴파일 에러
- **수정**: Request DTO의 `author` → `userId(Long)`으로 변경

**④ `GlobalExceptionHandler` — 실제 발생 예외를 처리하지 못함**
- `PostService`에서 `throw new NotFoundException(ErrorCode.POST_NOT_FOUND)` 발생
- 등록된 핸들러는 `PostNotFoundException.class`만 처리, `NotFoundException`은 미처리 → 게시글 미존재 시 500 에러 가능
- **수정**: `NotFoundException` 핸들러 추가 또는 Service에서 `PostNotFoundException` throw로 통일

### 🟡 주의

**⑤ `SpringBootApplication.java` — 불필요한 커스텀 어노테이션 선언**
- `public @interface SpringBootApplication {}` 형태로 직접 정의
- `Main.java`의 `@SpringBootApplication`은 Spring의 것을 사용하므로 충돌 가능
- **수정**: 해당 파일 삭제

**⑥ `User.java`, `UserRepository.java` — import 누락**
- `User.java`에 `jakarta.persistence.*`, `@Table`, `@Entity` 등 어노테이션 선언이 있으나 import 문이 파일 내에 존재하지 않음
- **수정**: 필요한 import 문 추가

---

## 개선 제안

1. **`NotFoundException` 계층 정리**: `PostNotFoundException`과 `NotFoundException`이 병존하여 혼란스러움. `NotFoundException`을 공통 기반 예외로 사용하고 도메인별 예외를 상속 구조로 정리하거나, 반대로 `PostNotFoundException`만 사용하도록 Service 코드 수정 — 둘 중 하나로 통일 권장

2. **`deletePost` 응답 일관성**: DELETE 응답을 `204 No Content`로 유지할지, `ApiResponse<Void>`로 감싸 `200 OK`를 반환할지 팀 컨벤션 기준으로 통일 필요

3. **`PostValidator` Spring Bean화 고려**: 현재 정적 유틸리티로 구현되어 있어 단위 테스트에서 Mock 처리가 어려움. `@Component`로 등록하면 의존성 주입을 통한 테스트가 수월해짐

4. **`ApiResponse` JSON 직렬화 확인**: Getter만 존재하고 Lombok `@Getter` 또는 Jackson 설정이 없으면 JSON 응답이 빈 객체 `{}`로 나올 수 있음. 실제 Postman 테스트로 확인 필요

---

## 다음 액션 아이템 (우선순위 순)

| 우선순위 | 항목 | 구분 |
|:--------:|------|------|
| 🔴 1 | `PostRepository.java` — 인터페이스 내 In-Memory 코드 전체 제거 | 버그 수정 |
| 🔴 2 | `PostResponse.java` — `getAuthor()`, `getCreatedAt()` 호출 오류 수정 | 버그 수정 |
| 🔴 3 | `CreatePostRequest` — `author: String` → `userId: Long` 타입 수정 | 버그 수정 |
| 🔴 4 | `GlobalExceptionHandler` — `NotFoundException` 핸들러 추가 | 버그 수정 |
| 🟡 5 | `SpringBootApplication.java` 불필요 파일 삭제 | 정리 |
| 🟡 6 | `User.java`, `UserRepository.java` import 누락 추가 | 버그 수정 |
| 🟡 7 | `deletePost()` — `ApiResponse<Void>` 래핑 또는 팀 패턴 통일 | 과제 보완 |
| 🟢 8 | 화면설계서 분석 내용 작성 (docs/ 또는 README) | 1주차 필수 |
| 🟢 9 | Pagination (`@RequestParam page, size`) 구현 | 2주차 심화 |
| 🟢 10 | BoardType enum + 게시판별 조회 API 구현 | 2주차 심화 |
| 🟢 11 | PR에 노션 API 명세서 링크·Postman 캡처 첨부 확인 | 2주차 필수 |
