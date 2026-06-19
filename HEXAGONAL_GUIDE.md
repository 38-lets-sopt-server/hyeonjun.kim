# 헥사고날 아키텍처 리팩토링 가이드

> 이 가이드는 헥사고날 아키텍처를 처음 접하는 개발자를 위해 작성되었습니다.
> 현재 프로젝트 코드를 직접 예시로 사용하며, 단계별로 따라할 수 있도록 구성했습니다.

---

## 목차

1. [헥사고날 아키텍처란?](#1-헥사고날-아키텍처란)
2. [현재 구조의 문제점](#2-현재-구조의-문제점)
3. [핵심 개념 이해하기](#3-핵심-개념-이해하기)
4. [목표 패키지 구조](#4-목표-패키지-구조)
5. [Step 1 - 도메인 분리](#step-1---도메인-분리)
6. [Step 2 - 출력 포트 정의 (Output Port)](#step-2---출력-포트-정의-output-port)
7. [Step 3 - 입력 포트 정의 (Input Port / Use Case)](#step-3---입력-포트-정의-input-port--use-case)
8. [Step 4 - 서비스 구현 (Application Service)](#step-4---서비스-구현-application-service)
9. [Step 5 - 영속성 어댑터 구현 (Outbound Adapter)](#step-5---영속성-어댑터-구현-outbound-adapter)
10. [Step 6 - 웹 어댑터 구현 (Inbound Adapter)](#step-6---웹-어댑터-구현-inbound-adapter)
11. [Step 7 - 나머지 도메인 적용](#step-7---나머지-도메인-적용)
12. [자주 묻는 질문](#자주-묻는-질문)

---

## 1. 헥사고날 아키텍처란?

### 비유로 이해하기

헥사고날 아키텍처(Hexagonal Architecture)는 **포트와 어댑터 패턴(Ports and Adapters)**이라고도 불립니다.

전자제품의 콘센트를 생각해보세요.

```
    [노트북]   [선풍기]   [핸드폰 충전기]
        |          |            |
       [어댑터]  [어댑터]     [어댑터]
        |          |            |
        └──────────┴────────────┘
                   |
              [콘센트(포트)]
                   |
              [전력망(핵심)]
```

- **전력망 (핵심/도메인)**: 실제 전기를 공급하는 핵심 로직. 노트북이 뭔지, 선풍기가 뭔지 몰라도 됩니다.
- **콘센트 (포트)**: "220V 전기를 공급한다"는 약속(인터페이스). 어떤 기기가 꽂힐지 모르지만 규격은 정해져 있습니다.
- **어댑터**: 각 기기를 콘센트에 맞게 연결하는 플러그. 기기마다 다르지만, 콘센트 규격은 지킵니다.

### 아키텍처 다이어그램

```
                    ┌─────────────────────────────────────┐
                    │                                     │
  [HTTP 요청]       │   ┌───────────────────────────┐    │
       │            │   │                           │    │
  [웹 어댑터] ──────┼──▶│  Application Core         │    │
  (Controller)     │   │  (Domain + Use Cases)     │────┼──▶ [DB 어댑터]
                    │   │                           │    │    (JPA Repository)
  [CLI 요청]        │   │  비즈니스 규칙이           │    │
       │            │   │  여기에 집중됩니다         │    │
  [CLI 어댑터] ─────┼──▶│                           │    │────┼──▶ [외부 API 어댑터]
                    │   └───────────────────────────┘    │
                    │                                     │
                    └─────────────────────────────────────┘
                         ▲                       ▲
                    Input Port              Output Port
                    (Use Case)           (Repository Port)
```

**핵심 아이디어**: 비즈니스 로직(도메인)은 외부 기술(HTTP, JPA, DB)에 대해 아무것도 알지 못합니다. 오직 포트(인터페이스)만 알고 있습니다.

---

## 2. 현재 구조의 문제점

현재 프로젝트의 구조를 살펴봅시다.

```
org.sopt
├── controller/     ← HTTP 처리
├── service/        ← 비즈니스 로직
├── repository/     ← 데이터 접근
├── domain/         ← 엔티티
├── dto/            ← 데이터 전송 객체
└── ...
```

이 구조(레이어드 아키텍처)는 직관적이지만 몇 가지 문제가 있습니다.

### 문제 1: 의존성이 한 방향으로만 흐르지 않음

```java
// PostService.java - 현재 코드
public class PostService {
    private final PostRepository postRepository;  // ← JPA 인터페이스를 직접 의존
```

`PostService`가 Spring Data JPA의 `JpaRepository`를 직접 알고 있습니다. JPA를 다른 기술(예: MyBatis)로 바꾸면 `PostService` 코드도 수정해야 합니다.

### 문제 2: 도메인 객체가 JPA 어노테이션에 오염됨

```java
// Post.java - 현재 코드
@Entity                          // ← JPA 어노테이션
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(...)         // ← JPA 어노테이션
    private Long id;

    @Enumerated(EnumType.STRING) // ← JPA 어노테이션
    private BoardType boardType;
```

순수한 비즈니스 객체여야 할 `Post`가 JPA 어노테이션으로 가득 차 있습니다.

### 문제 3: 도메인별 경계가 없음

`User`, `Post`, `Auth` 관련 코드가 `controller/`, `service/`, `repository/` 디렉토리에 뒤섞여 있어서 특정 도메인의 코드를 찾으려면 여러 폴더를 뒤져야 합니다.

---

## 3. 핵심 개념 이해하기

리팩토링 전에 3가지 핵심 개념을 확실히 이해해야 합니다.

### 개념 1: 도메인 (Domain)

**"순수한 비즈니스 규칙"**입니다.

- Spring도 몰라도 됩니다.
- JPA도 몰라도 됩니다.
- HTTP도 몰라도 됩니다.
- 오직 비즈니스 로직만 있습니다.

```java
// 도메인 객체 예시 (어노테이션 없음!)
public class Post {
    private final Long id;
    private String title;
    private String content;

    public void update(String title, String content) {
        // 비즈니스 규칙: 제목과 내용 수정
        this.title = title;
        this.content = content;
    }
}
```

### 개념 2: 포트 (Port)

**"도메인이 외부 세계와 소통하는 계약(인터페이스)"**입니다.

포트는 두 종류입니다.

**입력 포트 (Input Port / Use Case)**: 외부에서 도메인을 호출하는 인터페이스
```java
// "게시글을 생성한다"는 계약
public interface CreatePostUseCase {
    void createPost(CreatePostCommand command);
}
```

**출력 포트 (Output Port)**: 도메인이 외부(DB 등)를 호출하는 인터페이스
```java
// "게시글을 저장한다"는 계약
public interface PostRepository {
    void save(Post post);
    Optional<Post> findById(Long id);
}
```

### 개념 3: 어댑터 (Adapter)

**"포트를 실제로 구현하는 기술 코드"**입니다.

어댑터도 두 종류입니다.

**인바운드 어댑터 (Inbound Adapter)**: 외부 요청을 받아 입력 포트를 호출
```java
// HTTP 요청을 받아서 UseCase를 호출
@RestController
public class PostController {
    private final CreatePostUseCase createPostUseCase; // 포트를 의존

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest request) {
        createPostUseCase.createPost(...); // 포트 호출
    }
}
```

**아웃바운드 어댑터 (Outbound Adapter)**: 출력 포트를 구현하여 실제 DB 접근
```java
// 출력 포트를 JPA로 구현
@Component
public class PostPersistenceAdapter implements PostRepository { // 포트 구현
    private final PostJpaRepository jpaRepository; // 실제 JPA

    @Override
    public void save(Post post) {
        jpaRepository.save(PostJpaEntity.from(post));
    }
}
```

### 의존성 방향

```
[Controller] → [UseCase Interface] ← [Service] → [PostRepository Interface] ← [JPA Adapter]
     ↑                                    ↑
인바운드 어댑터                      아웃바운드 어댑터
(기술 세계)                          (기술 세계)

                    ↑
               [도메인: Post, User]
               (순수 비즈니스 코드)
```

모든 의존성이 **도메인을 향해** 흐릅니다. 도메인은 외부를 전혀 모릅니다.

---

## 4. 목표 패키지 구조

```
src/main/java/org/sopt/
│
├── post/                                  ← 게시글 도메인
│   ├── domain/
│   │   └── Post.java                      ← 순수 도메인 객체 (JPA 어노테이션 없음)
│   │
│   ├── application/
│   │   ├── port/
│   │   │   ├── in/                        ← 입력 포트 (Use Case 인터페이스)
│   │   │   │   ├── CreatePostUseCase.java
│   │   │   │   ├── GetPostUseCase.java
│   │   │   │   ├── UpdatePostUseCase.java
│   │   │   │   └── DeletePostUseCase.java
│   │   │   └── out/                       ← 출력 포트 (Repository 인터페이스)
│   │   │       └── PostRepositoryPort.java
│   │   │
│   │   ├── service/
│   │   │   └── PostService.java           ← Use Case 구현체
│   │   │
│   │   └── dto/                           ← 애플리케이션 계층 DTO (Command/Query)
│   │       ├── CreatePostCommand.java
│   │       └── UpdatePostCommand.java
│   │
│   └── adapter/
│       ├── in/
│       │   └── web/                       ← 인바운드 어댑터 (Controller)
│       │       ├── PostController.java
│       │       ├── request/
│       │       │   ├── CreatePostRequest.java
│       │       │   └── UpdatePostRequest.java
│       │       └── response/
│       │           └── PostResponse.java
│       │
│       └── out/
│           └── persistence/               ← 아웃바운드 어댑터 (JPA)
│               ├── PostJpaEntity.java     ← JPA 전용 엔티티
│               ├── PostJpaRepository.java ← Spring Data JPA 인터페이스
│               └── PostPersistenceAdapter.java ← PostRepositoryPort 구현체
│
├── user/                                  ← 유저 도메인 (동일한 구조)
│   ├── domain/
│   ├── application/
│   └── adapter/
│
└── auth/                                  ← 인증 도메인 (동일한 구조)
    ├── domain/
    ├── application/
    └── adapter/
```

---

## Step 1 - 도메인 분리

가장 먼저 할 일은 **순수한 도메인 객체**를 만드는 것입니다.

현재 `Post.java`에는 JPA 어노테이션이 있습니다. 이것을 제거하고, JPA용 엔티티를 따로 만들 것입니다.

### 1-1. 순수 도메인 객체 생성

`src/main/java/org/sopt/post/domain/Post.java` 파일을 새로 만듭니다.

```java
package org.sopt.post.domain;

import org.sopt.user.domain.User;
import java.time.LocalDateTime;

// @Entity 없음! 순수 Java 클래스
public class Post {

    private final Long id;
    private String title;
    private String content;
    private final BoardType boardType;
    private final User author;           // User 도메인 객체를 참조
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 새 게시글 생성 (id 없음)
    public Post(String title, String content, BoardType boardType, User author) {
        this.id = null;
        this.title = title;
        this.content = content;
        this.boardType = boardType;
        this.author = author;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // DB에서 불러올 때 (id 있음)
    public Post(Long id, String title, String content, BoardType boardType, User author,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.boardType = boardType;
        this.author = author;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 비즈니스 메서드: 게시글 수정
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    // Getter만 제공 (setter 없음 - 불변성 유지)
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public BoardType getBoardType() { return boardType; }
    public User getAuthor() { return author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

> **왜 JPA 어노테이션을 제거하나요?**
> 도메인 객체는 비즈니스 규칙만 담아야 합니다. `@Entity`, `@GeneratedValue` 같은 어노테이션은 "어떻게 DB에 저장하는가"라는 기술적 관심사입니다. 이것을 분리해야 도메인을 독립적으로 테스트하고 재사용할 수 있습니다.

### 1-2. BoardType도 도메인으로 이동

`src/main/java/org/sopt/post/domain/BoardType.java`

```java
package org.sopt.post.domain;

public enum BoardType {
    FREE, HOT, SECRET
}
```

### 1-3. User 도메인 객체 생성

`src/main/java/org/sopt/user/domain/User.java`

```java
package org.sopt.user.domain;

// @Entity, @Getter, @NoArgsConstructor 없음
public class User {

    private final Long id;
    private final String nickname;
    private final String email;
    private final String password;

    public User(Long id, String nickname, String email, String password) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }

    // 신규 회원 (id 없음)
    public User(String nickname, String email, String password) {
        this(null, nickname, email, password);
    }

    public Long getId() { return id; }
    public String getNickname() { return nickname; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
```

---

## Step 2 - 출력 포트 정의 (Output Port)

도메인이 DB를 사용할 때 의존하는 **인터페이스**를 정의합니다. 이것이 출력 포트입니다.

> **핵심**: 도메인은 "저장할 수 있다"는 사실만 알고, "어떻게 저장하는가(JPA, MyBatis, 파일...)"는 모릅니다.

### 2-1. Post 출력 포트

`src/main/java/org/sopt/post/application/port/out/PostRepositoryPort.java`

```java
package org.sopt.post.application.port.out;

import org.sopt.post.domain.BoardType;
import org.sopt.post.domain.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepositoryPort {

    Post save(Post post);

    Optional<Post> findById(Long id);

    List<Post> findAll(int page, int size);

    List<Post> findAllByBoardType(BoardType boardType);

    void delete(Post post);
}
```

### 2-2. User 출력 포트

`src/main/java/org/sopt/user/application/port/out/UserRepositoryPort.java`

```java
package org.sopt.user.application.port.out;

import org.sopt.user.domain.User;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);
}
```

---

## Step 3 - 입력 포트 정의 (Input Port / Use Case)

외부(Controller)가 도메인을 호출할 때 사용하는 인터페이스입니다. **각 유스케이스를 작은 인터페이스로 분리**하는 것이 좋습니다.

> **왜 하나의 큰 인터페이스가 아닌 여러 개로 나누나요?**
> Controller마다 필요한 기능이 다릅니다. `DeletePostController`는 삭제 기능만 필요한데, 조회/수정 기능까지 알 필요가 없습니다. 필요한 것만 의존하게 하는 것이 인터페이스 분리 원칙(ISP)입니다.

### 3-1. Command 객체 정의

유스케이스에 전달하는 입력 데이터를 담는 객체입니다.

`src/main/java/org/sopt/post/application/dto/CreatePostCommand.java`

```java
package org.sopt.post.application.dto;

import org.sopt.post.domain.BoardType;

public record CreatePostCommand(
        String title,
        String content,
        Long userId,
        BoardType boardType
) {}
```

`src/main/java/org/sopt/post/application/dto/UpdatePostCommand.java`

```java
package org.sopt.post.application.dto;

public record UpdatePostCommand(
        Long postId,
        String title,
        String content
) {}
```

### 3-2. 입력 포트 인터페이스들

`src/main/java/org/sopt/post/application/port/in/CreatePostUseCase.java`

```java
package org.sopt.post.application.port.in;

import org.sopt.post.application.dto.CreatePostCommand;

public interface CreatePostUseCase {
    void createPost(CreatePostCommand command);
}
```

`src/main/java/org/sopt/post/application/port/in/GetPostUseCase.java`

```java
package org.sopt.post.application.port.in;

import org.sopt.post.domain.BoardType;
import org.sopt.post.domain.Post;

import java.util.List;

public interface GetPostUseCase {
    Post getPost(Long id);
    List<Post> getAllPosts(int page, int size, BoardType boardType);
}
```

`src/main/java/org/sopt/post/application/port/in/UpdatePostUseCase.java`

```java
package org.sopt.post.application.port.in;

import org.sopt.post.application.dto.UpdatePostCommand;

public interface UpdatePostUseCase {
    void updatePost(UpdatePostCommand command);
}
```

`src/main/java/org/sopt/post/application/port/in/DeletePostUseCase.java`

```java
package org.sopt.post.application.port.in;

public interface DeletePostUseCase {
    void deletePost(Long id);
}
```

---

## Step 4 - 서비스 구현 (Application Service)

입력 포트를 구현하고, 출력 포트를 사용하는 서비스입니다.

`src/main/java/org/sopt/post/application/service/PostService.java`

```java
package org.sopt.post.application.service;

import lombok.RequiredArgsConstructor;
import org.sopt.post.application.dto.CreatePostCommand;
import org.sopt.post.application.dto.UpdatePostCommand;
import org.sopt.post.application.port.in.CreatePostUseCase;
import org.sopt.post.application.port.in.DeletePostUseCase;
import org.sopt.post.application.port.in.GetPostUseCase;
import org.sopt.post.application.port.in.UpdatePostUseCase;
import org.sopt.post.application.port.out.PostRepositoryPort;
import org.sopt.post.domain.BoardType;
import org.sopt.post.domain.Post;
import org.sopt.user.application.port.out.UserRepositoryPort;
import org.sopt.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService implements CreatePostUseCase, GetPostUseCase,
        UpdatePostUseCase, DeletePostUseCase {

    // 출력 포트에 의존 (JPA를 직접 의존하지 않음!)
    private final PostRepositoryPort postRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    @Transactional
    public void createPost(CreatePostCommand command) {
        // 유효성 검증 (도메인 규칙)
        validatePostContent(command.title(), command.content());

        User user = userRepositoryPort.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Post post = new Post(command.title(), command.content(), command.boardType(), user);
        postRepositoryPort.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Post getPost(Long id) {
        return postRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getAllPosts(int page, int size, BoardType boardType) {
        if (boardType != null) {
            return postRepositoryPort.findAllByBoardType(boardType);
        }
        return postRepositoryPort.findAll(page, size);
    }

    @Override
    @Transactional
    public void updatePost(UpdatePostCommand command) {
        validatePostContent(command.title(), command.content());

        Post post = postRepositoryPort.findById(command.postId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.update(command.title(), command.content());
        postRepositoryPort.save(post);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        Post post = postRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        postRepositoryPort.delete(post);
    }

    private void validatePostContent(String title, String content) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 비어있을 수 없습니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 비어있을 수 없습니다.");
        }
    }
}
```

> **변경 포인트**:
> - `PostRepository` (JPA) → `PostRepositoryPort` (인터페이스)
> - `UserService` → `UserRepositoryPort` (서비스 간 직접 호출 제거)
> - 여러 UseCase 인터페이스를 `implements`

---

## Step 5 - 영속성 어댑터 구현 (Outbound Adapter)

이제 출력 포트를 실제 JPA로 구현합니다. 도메인 객체 ↔ JPA 엔티티 간의 변환도 여기서 담당합니다.

### 5-1. JPA 전용 엔티티 생성

JPA 어노테이션은 오직 이 클래스에만 존재합니다.

`src/main/java/org/sopt/post/adapter/out/persistence/PostJpaEntity.java`

```java
package org.sopt.post.adapter.out.persistence;

import jakarta.persistence.*;
import org.sopt.post.domain.BoardType;
import org.sopt.post.domain.Post;
import org.sopt.user.adapter.out.persistence.UserJpaEntity;
import org.sopt.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "post")
public class PostJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @Enumerated(EnumType.STRING)
    private BoardType boardType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserJpaEntity user;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected PostJpaEntity() {}

    private PostJpaEntity(Long id, String title, String content, BoardType boardType,
                          UserJpaEntity user, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.boardType = boardType;
        this.user = user;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 도메인 → JPA 엔티티
    public static PostJpaEntity from(Post post, UserJpaEntity userJpaEntity) {
        return new PostJpaEntity(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getBoardType(),
                userJpaEntity,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    // JPA 엔티티 → 도메인
    public Post toDomain() {
        User userDomain = user.toDomain();
        return new Post(id, title, content, boardType, userDomain, createdAt, updatedAt);
    }

    public Long getId() { return id; }
}
```

### 5-2. Spring Data JPA 인터페이스

`src/main/java/org/sopt/post/adapter/out/persistence/PostJpaRepository.java`

```java
package org.sopt.post.adapter.out.persistence;

import org.sopt.post.domain.BoardType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostJpaRepository extends JpaRepository<PostJpaEntity, Long> {
    List<PostJpaEntity> findAllByBoardType(BoardType boardType);
}
```

### 5-3. 영속성 어댑터 구현

출력 포트를 JPA로 구현합니다. 도메인 ↔ JPA 엔티티 변환을 담당합니다.

`src/main/java/org/sopt/post/adapter/out/persistence/PostPersistenceAdapter.java`

```java
package org.sopt.post.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.sopt.post.application.port.out.PostRepositoryPort;
import org.sopt.post.domain.BoardType;
import org.sopt.post.domain.Post;
import org.sopt.user.adapter.out.persistence.UserJpaEntity;
import org.sopt.user.adapter.out.persistence.UserJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostRepositoryPort {

    private final PostJpaRepository postJpaRepository;
    private final UserJpaRepository userJpaRepository;  // User JPA가 필요

    @Override
    public Post save(Post post) {
        // 1. User 도메인 → UserJpaEntity 조회
        UserJpaEntity userJpaEntity = userJpaRepository.findById(post.getAuthor().getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. Post 도메인 → PostJpaEntity 변환
        PostJpaEntity entity = PostJpaEntity.from(post, userJpaEntity);

        // 3. 저장 후 도메인으로 변환하여 반환
        return postJpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Post> findById(Long id) {
        return postJpaRepository.findById(id).map(PostJpaEntity::toDomain);
    }

    @Override
    public List<Post> findAll(int page, int size) {
        return postJpaRepository.findAll(PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(PostJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Post> findAllByBoardType(BoardType boardType) {
        return postJpaRepository.findAllByBoardType(boardType)
                .stream()
                .map(PostJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(Post post) {
        postJpaRepository.deleteById(post.getId());
    }
}
```

---

## Step 6 - 웹 어댑터 구현 (Inbound Adapter)

Controller는 HTTP 요청을 받아서 UseCase(입력 포트)를 호출합니다.

> **변경 포인트**: `PostService`를 직접 의존하던 것을 → UseCase 인터페이스를 의존하도록 변경

### 6-1. 웹 계층 DTO

`src/main/java/org/sopt/post/adapter/in/web/request/CreatePostRequest.java`

```java
package org.sopt.post.adapter.in.web.request;

import org.sopt.post.application.dto.CreatePostCommand;
import org.sopt.post.domain.BoardType;

public record CreatePostRequest(
        String title,
        String content,
        BoardType boardType
) {
    public CreatePostCommand toCommand(Long userId) {
        return new CreatePostCommand(title, content, userId, boardType);
    }
}
```

`src/main/java/org/sopt/post/adapter/in/web/request/UpdatePostRequest.java`

```java
package org.sopt.post.adapter.in.web.request;

import org.sopt.post.application.dto.UpdatePostCommand;

public record UpdatePostRequest(
        String title,
        String content
) {
    public UpdatePostCommand toCommand(Long postId) {
        return new UpdatePostCommand(postId, title, content);
    }
}
```

`src/main/java/org/sopt/post/adapter/in/web/response/PostResponse.java`

```java
package org.sopt.post.adapter.in.web.response;

import org.sopt.post.domain.Post;

public record PostResponse(
        Long id,
        String title,
        String content,
        String author,
        String createdAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getNickname(),
                post.getCreatedAt().toString()
        );
    }
}
```

### 6-2. Controller

`src/main/java/org/sopt/post/adapter/in/web/PostController.java`

```java
package org.sopt.post.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.sopt.post.adapter.in.web.request.CreatePostRequest;
import org.sopt.post.adapter.in.web.request.UpdatePostRequest;
import org.sopt.post.adapter.in.web.response.PostResponse;
import org.sopt.post.application.port.in.CreatePostUseCase;
import org.sopt.post.application.port.in.DeletePostUseCase;
import org.sopt.post.application.port.in.GetPostUseCase;
import org.sopt.post.application.port.in.UpdatePostUseCase;
import org.sopt.post.domain.BoardType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    // Service가 아닌 UseCase 인터페이스에 의존!
    private final CreatePostUseCase createPostUseCase;
    private final GetPostUseCase getPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final DeletePostUseCase deletePostUseCase;

    @PostMapping
    public ResponseEntity<Void> createPost(
            @RequestBody CreatePostRequest request,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        createPostUseCase.createPost(request.toCommand(userId));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) BoardType boardType
    ) {
        List<PostResponse> posts = getPostUseCase.getAllPosts(page, size, boardType)
                .stream()
                .map(PostResponse::from)
                .toList();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(PostResponse.from(getPostUseCase.getPost(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request
    ) {
        updatePostUseCase.updatePost(request.toCommand(id));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        deletePostUseCase.deletePost(id);
        return ResponseEntity.ok().build();
    }
}
```

---

## Step 7 - 나머지 도메인 적용

`user`와 `auth` 도메인도 동일한 패턴으로 작성합니다.

### User 도메인 체크리스트

- [ ] `user/domain/User.java` - 순수 도메인 객체 (Step 1-3 참고)
- [ ] `user/application/port/out/UserRepositoryPort.java` - 출력 포트
- [ ] `user/application/port/in/JoinUserUseCase.java` - 입력 포트
- [ ] `user/application/service/UserService.java` - 서비스 구현
- [ ] `user/adapter/out/persistence/UserJpaEntity.java` - JPA 엔티티
- [ ] `user/adapter/out/persistence/UserJpaRepository.java` - Spring Data JPA
- [ ] `user/adapter/out/persistence/UserPersistenceAdapter.java` - 출력 포트 구현
- [ ] `user/adapter/in/web/UserController.java` - Controller

### Auth 도메인 체크리스트

- [ ] `auth/application/port/in/LoginUseCase.java`
- [ ] `auth/application/port/in/ReissueTokenUseCase.java`
- [ ] `auth/application/port/out/RefreshTokenRepositoryPort.java`
- [ ] `auth/application/service/AuthService.java`
- [ ] `auth/adapter/out/persistence/RefreshTokenJpaEntity.java`
- [ ] `auth/adapter/out/persistence/RefreshTokenPersistenceAdapter.java`
- [ ] `auth/adapter/in/web/AuthController.java`

### JwtService 처리 방법

`JwtService`는 인프라 계층의 기술 서비스입니다. 아래와 같이 포트로 추상화합니다.

`src/main/java/org/sopt/auth/application/port/out/TokenProviderPort.java`

```java
package org.sopt.auth.application.port.out;

public interface TokenProviderPort {
    String generateAccessToken(Long userId, String email);
    String generateRefreshToken(Long userId);
    Long verifyAndGetUserId(String token);
}
```

`src/main/java/org/sopt/auth/adapter/out/jwt/JwtTokenAdapter.java`

```java
package org.sopt.auth.adapter.out.jwt;

import org.sopt.auth.application.port.out.TokenProviderPort;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenAdapter implements TokenProviderPort {
    // 기존 JwtService 로직을 여기로 이동
}
```

---

## 자주 묻는 질문

### Q1. 파일이 너무 많아지는 것 아닌가요?

맞습니다. 헥사고날 아키텍처는 파일 수가 증가합니다. 그 대신 얻는 것이 있습니다.

- **각 파일의 역할이 명확**합니다. `PostPersistenceAdapter`를 보면 "JPA로 구현한 게시글 저장소"임을 바로 알 수 있습니다.
- **변경 범위가 좁아**집니다. JPA를 MongoDB로 바꾸면 `PostPersistenceAdapter`만 수정하면 됩니다.
- **테스트가 쉬워**집니다. 포트를 Mock으로 쉽게 대체할 수 있습니다.

### Q2. `PostService`가 `UserRepositoryPort`를 직접 의존해도 되나요?

예, 괜찮습니다. `PostService`가 유저 정보를 조회해야 하기 때문에 `UserRepositoryPort`를 사용하는 것은 자연스럽습니다. 단, `UserService`를 직접 의존하는 것은 피해야 합니다 (서비스 간 순환 의존 위험).

### Q3. `@Transactional`은 어디에 붙이나요?

Application Service (`PostService`)에 붙입니다. 트랜잭션 관리는 비즈니스 로직의 일부이므로 도메인/서비스 계층에서 관리합니다.

### Q4. 기존 코드를 한 번에 다 바꿔야 하나요?

아닙니다. 도메인별로 하나씩 옮길 수 있습니다. 예를 들어 `Post` 도메인만 먼저 헥사고날로 이동하고, `User`와 `Auth`는 나중에 옮겨도 됩니다. 단계적으로 진행하세요.

### Q5. Command 객체와 Request DTO의 차이는?

| 구분 | 위치 | 역할 |
|---|---|---|
| `CreatePostRequest` | `adapter/in/web` | HTTP 요청 JSON을 받는 웹 계층 전용 DTO |
| `CreatePostCommand` | `application/dto` | 유스케이스에 전달하는 입력 데이터. 웹과 무관 |

분리하는 이유: REST API 외에 CLI나 메시지 큐로도 같은 유스케이스를 호출할 수 있습니다. 각각 다른 Request DTO가 있어도 Command는 동일하게 사용할 수 있습니다.

---

## 리팩토링 진행 순서 요약

```
1단계: Post 도메인 순수화
  └─ post/domain/Post.java (JPA 어노테이션 제거)

2단계: 출력 포트 정의
  └─ post/application/port/out/PostRepositoryPort.java

3단계: 입력 포트 (Use Case) 정의
  └─ post/application/port/in/CreatePostUseCase.java 외 3개

4단계: 서비스 구현
  └─ post/application/service/PostService.java

5단계: JPA 어댑터 구현
  ├─ post/adapter/out/persistence/PostJpaEntity.java
  ├─ post/adapter/out/persistence/PostJpaRepository.java
  └─ post/adapter/out/persistence/PostPersistenceAdapter.java

6단계: 웹 어댑터 구현
  └─ post/adapter/in/web/PostController.java

7단계: User → Auth 순으로 반복
```

이 순서대로 진행하면 각 단계마다 컴파일 에러가 발생하지만, 다음 단계를 완료하면 해소됩니다. 한 도메인(Post)을 완전히 끝낸 후 나머지를 진행하는 것을 권장합니다.
