# SOPT 3주차 과제 코드리뷰
> 📅 리뷰 날짜: [날짜 입력]
> 👤 리뷰어: Claude Code
> 🎯 과제 목표: JPA + MySQL 연동, User-Post 연관관계, Swagger 적용

---

## 📁 프로젝트 구조 요약

> ⬇️ Claude Code가 실제 프로젝트를 분석한 후 채워줄 섹션이에요

```
src/main/java/com/example/assignment/
├── controller/
│   ├── PostController.java        # 게시글 API 엔드포인트
│   └── UserController.java        # (있다면) 유저 API 엔드포인트
├── service/
│   ├── PostService.java           # 게시글 비즈니스 로직
│   └── UserService.java           # (있다면) 유저 비즈니스 로직
├── repository/
│   ├── PostRepository.java        # JpaRepository 상속
│   └── UserRepository.java        # JpaRepository 상속
├── domain/
│   ├── Post.java                  # 게시글 엔티티 (@Entity)
│   ├── User.java                  # 유저 엔티티 (@Entity)
│   └── BaseTimeEntity.java        # createdAt/updatedAt 자동관리
├── dto/
│   ├── request/
│   │   └── CreatePostRequest.java
│   └── response/
│       └── PostResponse.java
├── exception/
│   ├── BusinessException.java
│   ├── NotFoundException.java
│   ├── ErrorCode.java
│   └── GlobalExceptionHandler.java
└── common/
    └── BaseResponse.java
```

> 위 구조는 예시입니다. Claude Code가 실제 코드를 보고 덮어씁니다.

---

## ✅ 잘 구현된 부분

> Claude Code가 채워줄 섹션입니다.

### 예시 형식:

#### 🎉 User와 Post의 연관관계 설정

```java
// Post.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;
```

**왜 잘된 코드인가요?**
- `FetchType.LAZY` — Post를 조회할 때 User 정보를 즉시 가져오지 않고, 실제로 `post.getUser()`를 호출할 때만 DB에 쿼리를 날려요. 에브리타임에서 게시글 목록을 볼 때 작성자 정보가 필요 없는 경우가 많으니, 불필요한 DB 쿼리를 줄일 수 있어요.
- `FetchType.EAGER`를 쓰면 Post 1개 조회할 때마다 User도 무조건 JOIN해서 가져와요. 게시글 100개를 조회하면 User도 100번 조회 — 성능이 나빠져요.

---

## ⚠️ 개선이 필요한 부분

> Claude Code가 채워줄 섹션입니다.

### 예시 형식:

#### 🔧 개선 필요: @NoArgsConstructor 접근 제어

**현재 코드:**
```java
@Entity
@NoArgsConstructor  // AccessLevel을 지정하지 않음
public class Post extends BaseTimeEntity {
    ...
}
```

**문제점:**
기본 생성자가 `public`으로 만들어져요. 누군가 `new Post()`로 빈 객체를 만들어버릴 수 있어요. 에브리타임 게시글은 반드시 제목, 내용, 작성자가 있어야 하는데, 빈 객체가 생성되면 검증을 피해갈 수 있어요.

**수정된 코드:**
```java
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA용 생성자는 숨기기
public class Post extends BaseTimeEntity {
    ...
}
```

**왜 PROTECTED인가요?**
JPA는 내부적으로 DB에서 데이터를 꺼낼 때 기본 생성자로 객체를 만들어요. 그래서 기본 생성자가 아예 없으면 JPA가 동작하지 않아요. 그렇다고 public으로 열어두면 외부에서 남용될 수 있으니, JPA만 접근할 수 있는 PROTECTED로 설정해요.

---

## 💡 개발 꿀팁

> Claude Code가 채워줄 섹션입니다.

### 예시 형식:

#### 📌 @Transactional(readOnly = true) 왜 붙이나요?

```java
// PostService.java
@Transactional(readOnly = true)   // ← 이게 왜 있을까요?
public PostResponse getPost(Long id) {
    ...
}
```

**readOnly = true를 붙이면 생기는 일:**
1. JPA가 **더티 체킹(Dirty Checking)** 을 안 해요.
    - 더티 체킹 = 트랜잭션이 끝날 때 객체가 바뀌었는지 DB와 비교하는 작업
    - 조회만 할 때는 이 비교가 필요 없어요 → 성능 향상
2. DB 복제 환경에서 **읽기 전용 DB 서버**로 요청을 보낼 수 있어요
    - 에브리타임처럼 조회가 많은 서비스에서 유용해요

---

## 📊 필수 과제 체크리스트

| 항목 | 구현 여부 | 비고 |
|------|----------|------|
| User 엔티티 생성 | ⬜ | |
| Post ↔ User @ManyToOne 연관관계 | ⬜ | |
| 게시글 작성 시 userId 연결 | ⬜ | |
| BaseTimeEntity 적용 | ⬜ | |
| Swagger @Tag 적용 | ⬜ | |
| Swagger @Operation 적용 | ⬜ | |
| Swagger @ApiResponses 적용 | ⬜ | |
| Swagger @Parameter/@Schema 적용 | ⬜ | |

> ✅ 구현 완료 / ⚠️ 부분 구현 / ❌ 미구현

---

## 🔥 심화 과제 체크리스트

| 항목 | 구현 여부 | 비고 |
|------|----------|------|
| Like 엔티티 + 좋아요 API | ⬜ | |
| 중복 좋아요 예외처리 | ⬜ | |
| 게시글 목록 + 좋아요 수 반환 | ⬜ | |
| fetch join으로 N+1 해결 | ⬜ | |
| @Version 낙관적 락 | ⬜ | |
| JPQL 검색 API | ⬜ | |
| QueryDSL 동적 쿼리 | ⬜ | |

---

## 📖 이번 주 핵심 개념 복습

> 이 코드를 예시로 들면서 핵심 개념을 설명합니다.
> Claude Code가 실제 프로젝트 코드를 인용해서 채워줄 섹션이에요.

---

### 1. JPA란? — 왜 직접 SQL을 안 쓰나요?

**2주차까지 우리 코드:**
```java
// 직접 ArrayList에 저장
private final List<Post> postList = new ArrayList<>();
postList.add(post);
```

**3주차 코드:**
```java
// JpaRepository가 알아서 SQL 만들어줌
postRepository.save(post);  // → INSERT INTO post (...) VALUES (...)
postRepository.findById(id); // → SELECT * FROM post WHERE id = ?
```

JPA(Java Persistence API)는 Java 객체와 DB 테이블을 자동으로 연결해주는 기술이에요.
`save()`, `findById()` 같은 메서드를 호출하면 JPA가 SQL을 자동으로 만들어 실행해줘요.
직접 SQL을 짜고, 결과를 Java 객체로 변환하는 번거로운 작업을 대신해줘요.

---

### 2. 연관관계 — User와 Post를 어떻게 연결하나요?

**현실 세계:**
> 에브리타임 게시글 1개에는 작성자 1명이 있어요.
> 한 명의 유저는 여러 개의 게시글을 쓸 수 있어요.
> → **User(1) : Post(N) 관계**

**DB 테이블:**
```
user 테이블               post 테이블
┌────────────────┐        ┌────────────────────────────┐
│ id │ nickname  │        │ id │ title   │ user_id(FK) │
├────┼───────────┤        ├────┼─────────┼────────────┤
│  1 │ 김솝트    │◄───────│  1 │ 학식뭐임│     1      │
│  2 │ 이솝트    │        │  2 │ 강의평가│     1      │
└────────────────┘        └────────────────────────────┘
```

**Java 코드:**
```java
// Post.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")  // post 테이블의 user_id 컬럼
private User user;
```

`@ManyToOne` = "많은 Post가 하나의 User에 속한다"는 의미예요.

---

### 3. @Transactional — 왜 붙이나요?

**트랜잭션이 없으면 생기는 문제:**
```
좋아요를 누를 때:
  1. like 테이블에 레코드 추가  ← 성공
  2. post.likeCount + 1          ← 서버 오류로 실패!

결과: like는 추가됐는데 likeCount는 그대로 → 데이터 불일치
```

**@Transactional을 붙이면:**
```
1번과 2번이 하나의 묶음으로 처리돼요.
→ 둘 다 성공하거나, 하나라도 실패하면 둘 다 취소(Rollback)
```

에브리타임에서 "좋아요 누르기"처럼 여러 DB 작업이 한 번에 성공/실패해야 하는 경우에 사용해요.

---

### 4. 소프트 딜리트 — 왜 데이터를 완전히 안 지우나요?

```java
@SQLDelete(sql = "UPDATE post SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
```

**물리 삭제 vs 소프트 딜리트:**

| | 물리 삭제 | 소프트 딜리트 |
|--|---------|------------|
| 방법 | DELETE FROM post WHERE id = ? | UPDATE post SET deleted_at = NOW() |
| 복구 가능? | ❌ 영구 삭제 | ✅ deleted_at 초기화로 복구 가능 |
| 사용 예시 | - | 에브리타임 게시글 삭제, 회원 탈퇴 |

`postRepository.delete(post)` 호출 시 실제론 DELETE 쿼리가 아닌 UPDATE 쿼리가 나가요.
이후 조회 시 `@Where(clause = "deleted_at IS NULL")` 덕분에 삭제된 글은 자동으로 제외돼요.

---

### 5. Swagger — API 문서를 왜 코드에서 만드나요?

**노션으로 API 명세서를 쓸 때의 문제:**
```
개발자 A: 코드를 수정했는데 노션 업데이트를 깜빡했어요
개발자 B: 노션 보고 구현했는데 실제 API랑 다르게 나와요!
```

**Swagger를 쓰면:**
```java
@Operation(summary = "게시글 단건 조회")
@GetMapping("/{id}")
public ResponseEntity<...> getPost(...) { ... }
// 코드가 바뀌면 Swagger 문서도 자동으로 바뀌어요!
```

`http://localhost:8080/swagger-ui/index.html` 에서 자동 생성된 문서를 볼 수 있어요.
Try it out 버튼으로 Postman 없이 바로 API 테스트도 할 수 있어요.

---

## 🏆 종합 평가

> Claude Code가 채워줄 섹션입니다.

| 항목 | 점수 | 코멘트 |
|------|-----|-------|
| 엔티티 설계 | - | |
| 연관관계 매핑 | - | |
| 트랜잭션 관리 | - | |
| 예외 처리 | - | |
| Swagger 문서화 | - | |
| 코드 가독성 | - | |

**전체 코멘트:**
> (Claude Code가 작성)

---

*이 파일은 SOPT 38기 서버 파트 세미나 학습용 코드리뷰 결과물입니다.*
*코드리뷰는 완성된 코드를 평가하는 게 아니라, 더 좋은 코드를 향해 함께 성장하는 과정이에요! 💪*