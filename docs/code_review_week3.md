# SOPT 3주차 과제 코드리뷰

> 리뷰어: Claude Code | 리뷰 날짜: 2026-05-01
>
> **대상 독자**: Java/Spring을 배운 지 3주 된 입문자 기준으로 작성했습니다. 어려운 용어는 최대한 풀어서 설명합니다.

---

## 📦 전체 구조 요약

```
src/main/java/org/sopt/
├── Main.java                          ← 앱 실행 진입점, @EnableJpaAuditing 설정
├── SpringBootApplication.java         ← ⚠️ 사용 목적 불명확한 파일 (설명 참고)
│
├── controller/
│   └── PostController.java            ← HTTP 요청을 받아 Service를 호출하는 계층
│
├── service/
│   └── PostService.java               ← 비즈니스 로직 담당, @Transactional 관리
│
├── repository/
│   ├── PostRepository.java            ← Post DB 접근 (JpaRepository 상속)
│   └── UserRepository.java            ← User DB 접근 (JpaRepository 상속)
│
├── domain/
│   ├── Post.java                      ← 게시글 엔티티 (테이블과 1:1 매핑)
│   ├── User.java                      ← 사용자 엔티티
│   ├── BaseTimeEntity.java            ← 생성/수정 시간 자동화 부모 클래스
│   └── BoardType.java                 ← 게시판 종류 Enum (FREE, HOT, SECRET)
│
├── dto/
│   ├── request/
│   │   ├── CreatePostRequest.java     ← 게시글 생성 요청 DTO (Record)
│   │   └── UpdatePostRequest.java     ← 게시글 수정 요청 DTO (Record)
│   └── response/
│       ├── BaseResponse.java          ← 공통 응답 래퍼 클래스
│       └── PostResponse.java          ← 게시글 응답 DTO (Record)
│
├── exception/
│   ├── ErrorCode.java                 ← 에러 코드 Enum (status, code, message)
│   ├── NotFoundException.java         ← 404 예외 클래스
│   ├── PostNotFoundException.java     ← ⚠️ 레거시 예외 클래스 (정리 필요)
│   └── GlobalExceptionHandler.java    ← 전역 예외 처리기
│
└── validator/
    └── PostValidator.java             ← 게시글 유효성 검증 유틸리티
```

---

## ✅ 필수 과제 체크리스트 결과

| 항목 | 결과 |
|------|------|
| User 엔티티 생성 + Post에 @ManyToOne 연관관계 추가 | ✅ 완료 |
| 게시글 작성 시 userId를 받아서 User 엔티티와 연결 | ✅ 완료 |
| 전체 API에 Swagger 어노테이션 적용 | ✅ 완료 |
| BaseTimeEntity를 Post에 적용 | ✅ 완료 |

**필수 과제 4개 모두 완료했습니다! 🎉**

---

## 🔥 심화 과제 체크리스트 결과

| 항목 | 결과 |
|------|------|
| Like 엔티티 + 좋아요 추가/취소 API | ❌ 미구현 |
| 중복 좋아요 예외처리 (ConflictException) | ❌ 미구현 |
| 게시글 목록 조회 시 좋아요 수 반환 + fetch join으로 N+1 해결 | ❌ 미구현 |
| @Version을 이용한 낙관적 락 + 재시도 로직 | ❌ 미구현 |
| JPQL @Query로 게시글 검색 API | ❌ 미구현 |
| QueryDSL 동적 쿼리 | ❌ 미구현 |

심화 과제는 이번에 구현하지 않은 것으로 보입니다. 다음 주에 도전해보세요!

---

## 1. JPA & 엔티티 설계

### ✅ 잘 구현된 부분

**`@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)` 올바른 사용**

```java
// Post.java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

`GenerationType.IDENTITY`는 MySQL의 `AUTO_INCREMENT`를 그대로 활용하는 방식입니다.
DB가 새 행을 저장할 때마다 id를 자동으로 1씩 증가시켜줍니다. 가장 흔하게 쓰이는 올바른 방식이에요.

**`protected Post() {}`로 기본 생성자 보호**

```java
// Post.java
protected Post() {}
```

JPA는 DB에서 데이터를 읽어올 때 내부적으로 기본 생성자(파라미터 없는 생성자)를 사용합니다.
그런데 `public`으로 열어두면 개발자가 실수로 빈 Post 객체를 만들 수 있어요.
`protected`로 막아두면 "JPA는 사용할 수 있지만, 외부 코드에서는 직접 못 만든다"는 의도가 명확해집니다.

> 💡 **꿀팁**: Lombok을 사용하면 `@NoArgsConstructor(access = AccessLevel.PROTECTED)`로 한 줄에 처리할 수 있어요.

---

### ⚠️ 개선이 필요한 부분

**`@Column` 어노테이션 미사용**

현재 코드:
```java
// Post.java
private String title;
private String content;
```

문제점: DB 컬럼에 대한 제약 조건(null 불가, 최대 길이 등)이 Java 코드에만 있고 DB에는 없습니다.
`PostValidator`에서 title을 50자 이하로 막지만, DB 컬럼 자체에는 제한이 없어요.
만약 Validator를 거치지 않는 경로가 생기면 무한히 긴 데이터가 저장될 수 있습니다.

수정 제안:
```java
// Post.java
@Column(nullable = false, length = 50)
private String title;

@Column(nullable = false, columnDefinition = "TEXT")
private String content;
```

이렇게 하면 DB 레벨에서도 제약이 걸려서 이중으로 안전해집니다.

---

## 2. BaseTimeEntity — 생성일/수정일 자동화

### ✅ 잘 구현된 부분

**BaseTimeEntity 전체 구조가 올바릅니다!**

```java
// BaseTimeEntity.java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

- `@MappedSuperclass`: "이 클래스는 테이블을 만들지 않고, 상속받는 엔티티의 컬럼만 추가해줘" 라는 의미예요.
- `@EntityListeners(AuditingEntityListener.class)`: Spring이 저장/수정 이벤트를 감지해서 날짜를 자동으로 넣어주는 리스너입니다.
- `@CreatedDate` / `@LastModifiedDate`: 각각 최초 저장 시, 그리고 수정될 때마다 자동으로 시간이 기록됩니다.

**`@EnableJpaAuditing` 설정 완료**

```java
// Main.java
@EnableJpaAuditing
@SpringBootApplication
public class Main { ... }
```

이 어노테이션이 있어야 `@CreatedDate`, `@LastModifiedDate`가 실제로 동작합니다. 잊지 않고 추가했어요!

---

### ⚠️ 개선이 필요한 부분

**`@EnableJpaAuditing`의 위치 — 분리 권장**

현재 코드: Main 클래스에 `@EnableJpaAuditing`이 함께 있음

문제점: 나중에 테스트 코드를 작성할 때 `@WebMvcTest` 같은 슬라이스 테스트를 사용하면, Main 클래스를 로드하지 않아서 `@EnableJpaAuditing`이 누락되어 오류가 발생할 수 있습니다.

수정 제안: 별도의 설정 클래스로 분리하세요.

```java
// config/JpaConfig.java (새 파일)
package org.sopt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
```

그리고 Main.java에서는 `@EnableJpaAuditing`을 제거합니다.

---

## 3. 소프트 딜리트 (Soft Delete)

### ⚠️ 개선이 필요한 부분 (중요!)

**소프트 딜리트가 구현되지 않았습니다.**

현재 코드:
```java
// PostService.java
public void deletePost(Long id) {
    Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));
    postRepository.delete(post);  // 실제로 DB에서 행을 삭제함!
}
```

문제점: `postRepository.delete(post)`는 DB에서 해당 행을 완전히 지워버립니다.
실제 서비스에서는 삭제된 게시글의 이력이 필요하거나, "삭제된 게시글입니다"를 보여줘야 할 때가 많아요.
또한 실수로 삭제된 경우 복구가 불가능해집니다.

**소프트 딜리트(Soft Delete)**는 실제로 행을 지우는 대신, `deleted_at` 컬럼에 삭제 시간을 기록하는 방식입니다.

수정 제안:

```java
// Post.java에 추가
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@SQLDelete(sql = "UPDATE post SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Post extends BaseTimeEntity {

    // ... 기존 필드들 ...

    private LocalDateTime deletedAt;  // 삭제 시간 (null이면 정상, 값이 있으면 삭제됨)
}
```

- `@SQLDelete`: JPA가 `delete()`를 호출할 때 실제 DELETE 쿼리 대신 이 UPDATE 쿼리를 실행합니다.
- `@Where`: 모든 조회 쿼리에 `WHERE deleted_at IS NULL` 조건이 자동으로 붙어서, 삭제된 게시글은 조회 결과에서 제외됩니다.

이렇게 하면 Service 코드는 그대로 `postRepository.delete(post)`를 호출하면 되고, JPA가 내부적으로 처리해줍니다!

---

## 4. User 엔티티와 연관관계

### ✅ 잘 구현된 부분

**User 엔티티 구조가 올바릅니다.**

```java
// User.java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;
    private String email;
}
```

`@Table(name = "users")`: MySQL에서 `user`는 예약어이기 때문에 테이블명을 `users`로 지정한 것은 매우 좋은 판단입니다!

**`@ManyToOne(fetch = FetchType.LAZY)`로 지연 로딩 설정**

```java
// Post.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;
```

`FetchType.LAZY`(지연 로딩)란, Post를 조회할 때 User 정보를 즉시 가져오지 않고, 실제로 `post.getUser()`를 호출하는 순간에 DB에서 가져오는 방식입니다.

반대로 `FetchType.EAGER`(즉시 로딩)를 쓰면, Post 하나를 조회할 때마다 User도 같이 조회하는 쿼리가 실행됩니다. 게시글 목록 100개를 조회하면 User 조회 쿼리도 100번 실행되는 N+1 문제가 발생할 수 있어요.

---

## 5. JpaRepository

### ✅ 잘 구현된 부분

**PostRepository와 UserRepository 모두 올바르게 구현되었습니다.**

```java
// PostRepository.java
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByBoardType(BoardType boardType);
}
```

`JpaRepository<Post, Long>`에서 `Post`는 관리할 엔티티, `Long`은 그 엔티티의 PK 타입입니다.
`findAllByBoardType` 같은 메서드는 Spring Data JPA가 메서드 이름을 분석해서 자동으로 SQL을 만들어줍니다. `findAllBy[필드명]` 형식이에요!

---

### ⚠️ 개선이 필요한 부분

**UserRepository에 불필요한 import가 있습니다.**

```java
// UserRepository.java
import jakarta.persistence.Entity;  // ← 이 import는 사용하지 않아요!
```

현재 코드에서 `@Entity`를 사용하지 않는데 import가 남아있습니다. 삭제해주세요.

---

## 6. Service 계층 — @Transactional

### ✅ 잘 구현된 부분

**`@Transactional`과 `@Transactional(readOnly = true)` 구분 사용**

```java
@Transactional                    // 데이터 변경 메서드
public void createPost(...) { ... }

@Transactional(readOnly = true)   // 조회 메서드
public List<Post> getAllPosts(...) { ... }
```

`@Transactional`은 "이 메서드 전체를 하나의 작업 단위로 묶어서, 중간에 오류가 나면 전부 되돌려줘" 라는 의미입니다.
`readOnly = true`를 추가하면 JPA가 "이건 조회만 할 거야"라는 것을 알고 최적화를 해줍니다. 데이터를 수정하지 않으니 변경 감지(dirty checking) 작업을 생략해서 더 빠릅니다.

**`orElseThrow` 패턴으로 예외 처리**

```java
User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
```

`findById()`는 결과가 있을 수도 없을 수도 있어서 `Optional<User>`를 반환합니다.
`orElseThrow()`는 "결과가 없으면 이 예외를 던져줘"라는 뜻입니다. `if (user == null) throw new ...`보다 훨씬 깔끔하죠!

---

### ⚠️ 개선이 필요한 부분

**boardType 필터링 시 Pagination이 적용되지 않습니다.**

현재 코드:
```java
public List<Post> getAllPosts(int page, int size, BoardType boardType) {
    if (boardType != null) {
        return postRepository.findAllByBoardType(boardType);  // ← 전체를 다 가져옴!
    }
    Pageable pageable = PageRequest.of(page, size);
    return postRepository.findAll(pageable).getContent();
}
```

문제점: `boardType`이 있을 때는 `Pageable`을 사용하지 않아서 해당 게시판의 모든 게시글을 한 번에 가져옵니다.
게시글이 10만 개라면 10만 개를 전부 메모리에 올려야 해요.

수정 제안:
```java
// PostRepository.java에 추가
Page<Post> findAllByBoardType(BoardType boardType, Pageable pageable);
```

```java
// PostService.java 수정
public List<Post> getAllPosts(int page, int size, BoardType boardType) {
    Pageable pageable = PageRequest.of(page, size);
    if (boardType != null) {
        return postRepository.findAllByBoardType(boardType, pageable).getContent();
    }
    return postRepository.findAll(pageable).getContent();
}
```

---

## 7. 예외 처리 계층 구조

### ✅ 잘 구현된 부분

**ErrorCode enum에 status, code, message가 잘 정의되어 있습니다.**

```java
// ErrorCode.java
public enum ErrorCode {
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_001", "게시글을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다.");
}
```

에러 코드를 enum으로 중앙 관리하는 건 매우 좋은 방식이에요. 에러가 추가될 때 이 파일 하나만 수정하면 됩니다.

---

### ⚠️ 개선이 필요한 부분

**`BusinessException` 기반 클래스가 없고, `PostNotFoundException`이 레거시로 남아있습니다.**

현재 상황:
- `NotFoundException`이 직접 `RuntimeException`을 상속하고 있음
- `PostNotFoundException`이 과거에 만든 클래스인데 지금은 사용하지 않음
- `GlobalExceptionHandler`에서 `PostNotFoundException`도 잡고 있어서 혼란스러움

권장 구조:
```
RuntimeException
└── BusinessException (공통 기반 클래스)
    ├── NotFoundException (404)
    └── BadRequestException (400)
```

수정 제안:

```java
// exception/BusinessException.java (새 파일)
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }
}
```

```java
// exception/NotFoundException.java 수정
public class NotFoundException extends BusinessException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
```

그리고 `PostNotFoundException.java`는 삭제하세요. 이미 `NotFoundException(ErrorCode.POST_NOT_FOUND)`으로 대체됐습니다.

**GlobalExceptionHandler에서 ErrorCode의 status를 활용하지 않습니다.**

현재 코드:
```java
@ExceptionHandler(NotFoundException.class)
public ResponseEntity<BaseResponse<Void>> handleNotFoundException(NotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)  // ← 하드코딩
                         .body(BaseResponse.fail(e.getMessage()));
}
```

문제점: `ErrorCode`에 `HttpStatus`를 정의해뒀는데 활용하지 않고 있어요. 나중에 `ErrorCode`에 `HttpStatus.FORBIDDEN` 같은 다른 상태 코드를 추가해도 항상 404만 반환됩니다.

수정 제안:
```java
@ExceptionHandler(BusinessException.class)
public ResponseEntity<BaseResponse<Void>> handleBusinessException(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    return ResponseEntity.status(errorCode.getStatus())  // ErrorCode에서 status 활용!
                         .body(BaseResponse.fail(errorCode.getMessage()));
}
```

`BusinessException` 하나로 모든 하위 예외를 한 번에 잡을 수 있어서 코드도 훨씬 간결해집니다.

---

## 8. DTO 설계

### ✅ 잘 구현된 부분

**Java Record로 DTO를 만들었습니다.**

```java
// CreatePostRequest.java
public record CreatePostRequest(
        String title,
        String content,
        Long userId,
        BoardType boardType
) {}
```

`record`는 Java 16에서 도입된 불변 데이터 클래스입니다. 생성자, getter, equals, hashCode, toString을 자동으로 만들어줘서 코드가 매우 간결해요. DTO처럼 데이터만 담는 객체에 딱 맞는 방식입니다!

**`PostResponse.from(post)` 정적 팩토리 메서드 패턴**

```java
// PostResponse.java
public static PostResponse from(Post post) {
    return new PostResponse(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getUser().getNickname(),
            post.getCreatedAt().toString()
    );
}
```

Controller에서 `PostResponse.from(post)`처럼 엔티티를 DTO로 변환하는 책임이 DTO 클래스 자체에 있어서 좋습니다. 변환 로직이 분산되지 않고 한 곳에 모여 있어요.

---

### ⚠️ 개선이 필요한 부분

**`@Schema` 어노테이션으로 Swagger 문서를 더 풍부하게 만들 수 있습니다.**

현재 코드:
```java
public record CreatePostRequest(
        String title,
        String content,
        Long userId,
        BoardType boardType
) {}
```

수정 제안:
```java
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 생성 요청")
public record CreatePostRequest(
        @Schema(description = "게시글 제목", example = "오늘의 일기", maxLength = 50)
        String title,
        @Schema(description = "게시글 내용", example = "오늘은 날씨가 맑았다.")
        String content,
        @Schema(description = "작성자 ID", example = "1")
        Long userId,
        @Schema(description = "게시판 종류", example = "FREE")
        BoardType boardType
) {}
```

Swagger UI에서 각 필드의 설명과 예시 값이 표시되어 API를 사용하는 사람이 바로 이해할 수 있어요.

---

## 9. Swagger 어노테이션

### ✅ 잘 구현된 부분

**`springdoc-openapi-starter-webmvc-ui` 의존성이 올바르게 추가되어 있습니다.**

```groovy
// build.gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
```

**`@Tag`, `@Operation`, `@ApiResponses`, `@Parameter` 모두 잘 사용했습니다.**

```java
@Tag(name = "Post", description = "게시글 관련 API")
@RestController
public class PostController {

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "게시글 작성 성공"),
        @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> createPost(...) { ... }
}
```

**`BaseResponse`라는 이름 사용 — Swagger와 이름 충돌 방지**

`springdoc`의 `@ApiResponse`와 이름이 겹치지 않도록 `ApiResponse` 대신 `BaseResponse`를 사용한 것은 올바른 판단입니다!

---

### ⚠️ 개선이 필요한 부분

**API URL에 버전 정보가 없습니다.**

현재 코드:
```java
@RequestMapping("/posts")
```

수정 제안:
```java
@RequestMapping("/api/v1/posts")
```

`/api/v1/`을 붙이는 이유는 나중에 API 스펙이 바뀌어도 기존 클라이언트(앱 등)가 계속 v1을 쓸 수 있게 버전 관리를 위해서입니다. 실무에서는 거의 필수예요.

---

## 10. application.yml 설정

### ✅ 잘 구현된 부분

**주요 설정이 모두 올바르게 되어 있습니다.**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/assignment
    username: root
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

- `ddl-auto: update`: 엔티티 변경 시 DB 스키마를 자동으로 업데이트합니다. 개발 중에 편리한 설정이에요.
- `show-sql: true`: 실행되는 SQL 쿼리가 콘솔에 출력됩니다.
- `format_sql: true`: SQL이 보기 좋게 포맷팅되어 출력됩니다.

---

## 11. 그 외 발견된 문제들

### ⚠️ `SpringBootApplication.java` — 의도를 알 수 없는 파일

현재 코드:
```java
// SpringBootApplication.java
package org.sopt;

public @interface SpringBootApplication {
}
```

이 파일은 사용자 정의 어노테이션을 만드는 코드인데, 실제로 프로젝트 어디서도 사용되지 않습니다.
Spring의 `@SpringBootApplication`과 이름이 같아서 혼란스러울 수 있어요.
삭제를 권장합니다.

---

### ⚠️ `build.gradle` 중복 의존성

현재 코드:
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'   // ← 두 번!
    testImplementation 'org.springframework.boot:spring-boot-starter-test'  // ← 두 번!
    // ...
    implementation 'org.springframework.boot:spring-boot-starter-web'   // ← 또 등장
    testImplementation 'org.springframework.boot:spring-boot-starter-test'  // ← 또 등장
}
```

기능적으로는 문제없지만 코드가 지저분해 보입니다. 중복 줄을 제거해주세요.

---

## 📊 종합 평가

| 항목 | 점수 | 비고 |
|------|------|------|
| JPA & 엔티티 설계 | 🟡 보통 | @Column 미사용 |
| BaseTimeEntity | 🟢 좋음 | @EnableJpaAuditing 위치만 개선 권장 |
| 소프트 딜리트 | 🔴 미구현 | 3주차 핵심 개념이라 아쉬움 |
| User 연관관계 | 🟢 좋음 | LAZY 로딩 올바르게 사용 |
| JpaRepository | 🟢 좋음 | 불필요한 import 1개 |
| @Transactional | 🟡 보통 | boardType 필터 시 Pagination 누락 |
| 예외 처리 계층 | 🟡 보통 | BusinessException 기반 클래스 없음, 레거시 잔존 |
| DTO 설계 | 🟢 좋음 | Record, 정적 팩토리 패턴 잘 활용 |
| Swagger | 🟡 보통 | @Schema 미사용, URL 버전 없음 |
| application.yml | 🟢 좋음 | 모든 설정 완비 |

---

## 📖 이번 주 핵심 개념 복습

### 1. JPA란 무엇인가요?

JPA(Java Persistence API)는 Java 객체와 데이터베이스 테이블을 연결해주는 기술입니다.
기존에는 SQL을 직접 작성해야 했지만, JPA를 쓰면 Java 코드로 DB 작업을 할 수 있어요.

이 프로젝트에서 `Post.java`가 바로 JPA 엔티티입니다:
```java
@Entity  // "이 클래스는 DB 테이블과 연결된다"는 선언
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // DB의 AUTO_INCREMENT와 연결
}
```

### 2. 연관관계 — Post와 User는 어떻게 이어져 있을까요?

현실 세계에서 "게시글은 한 명의 사용자가 작성한다"는 관계가 있죠?
이걸 DB로 표현하면 `post` 테이블에 `user_id` 컬럼을 넣는 방식(외래 키)으로 구현합니다.
JPA에서는 이렇게 표현해요:

```java
// Post.java
@ManyToOne(fetch = FetchType.LAZY)  // 여러 Post가 한 User에 속한다
@JoinColumn(name = "user_id")       // DB에서 "user_id" 컬럼으로 연결
private User user;
```

`LAZY`(지연 로딩)를 쓰면 Post를 가져올 때 User 정보는 당장 가져오지 않고,
실제로 `post.getUser()`를 호출할 때 비로소 DB에서 가져옵니다.
불필요한 DB 조회를 줄여서 성능을 높일 수 있어요.

### 3. @Transactional — 왜 필요한가요?

은행 이체를 생각해보세요. "A의 잔액 차감" + "B의 잔액 증가"가 하나의 작업이잖아요.
중간에 오류가 나면 A 잔액은 줄었는데 B는 안 늘어나는 문제가 생길 수 있어요.

`@Transactional`은 이런 여러 DB 작업을 하나로 묶어줍니다:
```java
@Transactional
public void createPost(String title, String content, Long userId, ...) {
    User user = userRepository.findById(userId)...;  // 1. User 조회
    Post post = new Post(title, content, boardType, user);
    postRepository.save(post);  // 2. Post 저장
    // 2번에서 오류가 나면 1번도 없던 일이 된다!
}
```

### 4. 소프트 딜리트 — 왜 실제로는 안 지우나요?

`@SQLDelete`와 `@Where`를 이용한 소프트 딜리트는 이렇게 동작합니다:

```java
@SQLDelete(sql = "UPDATE post SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Post extends BaseTimeEntity {
    private LocalDateTime deletedAt;  // null = 정상, 값 있음 = 삭제됨
}
```

`postRepository.delete(post)`를 호출하면:
- 실제로 DB에서 행이 지워지지 않아요
- 대신 `deleted_at` 컬럼에 현재 시간이 기록됩니다
- 조회할 때는 `deleted_at IS NULL`인 것만 가져오니까 삭제된 것처럼 보이죠

이렇게 하면 삭제 이력이 남고, 실수로 삭제했을 때 복구도 쉽습니다.

### 5. Swagger — API 문서를 자동으로 만들어준다고요?

`springdoc-openapi`를 추가하면 코드의 어노테이션을 읽어서 자동으로 API 문서를 만들어줍니다.
서버를 실행하고 `http://localhost:8080/swagger-ui.html`에 접속하면 웹 UI로 API를 바로 테스트해볼 수 있어요!

```java
@Tag(name = "Post", description = "게시글 관련 API")
@RestController
public class PostController {

    @Operation(summary = "게시글 작성")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "성공"),
        @ApiResponse(responseCode = "404", description = "유저 없음")
    })
    @PostMapping
    public ResponseEntity<...> createPost(...) { ... }
}
```

Postman 없이도 Swagger UI로 바로 API를 호출하고 응답을 확인할 수 있어서 개발 중에 매우 편리합니다!

---

*리뷰 끝. 필수 과제 4개를 모두 완성한 것 수고 많으셨어요! 소프트 딜리트와 예외 계층 구조 개선이 가장 우선순위가 높은 피드백입니다. 다음 주 심화 과제도 화이팅! 🚀*
