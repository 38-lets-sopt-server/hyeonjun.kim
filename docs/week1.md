# 1주차 과제 — 화면설계서 분석

에브리타임 자유게시판 Storyboard & User Flow를 서버 개발자의 시각으로 분석합니다.

---

## ① 자유게시판 목록 화면 (Board List Screen)

### 저장해야 하는 데이터

| 필드명 | 타입 | 설명 |
|---|---|---|
| `id` | Long | 게시글 고유 식별자 (상세 화면 진입 시 필요) |
| `title` | String | 게시글 제목 |
| `content` | String | 게시글 본문 (목록에서는 일부만 노출, 전체 저장) |
| `author` | String | 작성자 (익명 여부에 따라 "익명" 표시) |
| `isAnonymous` | boolean | 익명 여부 |
| `createdAt` | String | 작성 시각 ("14분 전" 등 변환은 클라이언트 처리) |
| `likeCount` | int | 공감 수 |
| `commentCount` | int | 댓글 수 |
| `scrapCount` | int | 스크랩 수 |

### 요청 / 응답

- **클라이언트 → 서버 요청:** 자유게시판 게시글 목록 조회
- **서버 → 클라이언트 응답:** 게시글 리스트 (id, title, content 미리보기, author, createdAt, likeCount, commentCount)

### 발생하는 행위 (기능)

- **게시글 전체 조회** — 스크롤 시 게시글 피드를 순서대로 불러옴
- **글쓰기 버튼(FAB) 클릭** — 글 쓰기 화면으로 이동 → 게시글 생성 기능 필요

---

## ② 게시글 상세 화면 (Post Detail Screen)

### 저장해야 하는 데이터

| 필드명 | 타입 | 설명 |
|---|---|---|
| `id` | Long | 특정 게시글을 식별하는 고유 번호 |
| `title` | String | 게시글 제목 |
| `content` | String | 게시글 전체 본문 (목록과 달리 전체 내용 필요) |
| `author` | String | 작성자 닉네임 또는 "익명" |
| `createdAt` | String | 작성 시각 (예: 03/27 15:22) |
| `likeCount` | int | 공감 수 |
| `commentCount` | int | 댓글 수 |
| `scrapCount` | int | 스크랩 수 |

### 요청 / 응답

- **클라이언트 → 서버 요청:** 특정 게시글 단건 조회 (id 포함)
- **서버 → 클라이언트 응답:** 해당 게시글의 전체 데이터 (title, content 전체, author, createdAt, likeCount, commentCount, scrapCount)

### 발생하는 행위 (기능)

- **게시글 단건 조회** — id를 기준으로 특정 게시글 1개 조회
- **공감(좋아요)** — 공감 수 증가 기능 필요
- **스크랩** — 스크랩 수 증가 기능 필요
- **뒤로가기** — 목록 화면으로 복귀 (서버 요청 없음)
- **댓글 작성** — 댓글 입력창에서 전송 버튼 클릭 시 댓글 생성 (3차 세미나에서 구현)

---

## ③ 글 쓰기 화면 (Write Post Screen)

### 서버가 받아야 하는 데이터 (요청값)

| 필드명 | 타입 | 필수 여부 | 설명 |
|---|---|---|---|
| `title` | String | 필수 | 제목 입력 필드 |
| `content` | String | 필수 | 본문 입력 필드 |
| `isAnonymous` | boolean | 선택 | 익명 체크박스 |

### 응답

- **서버 → 클라이언트 응답:** 생성된 게시글의 id, 등록 완료 메시지

### 발생하는 행위 (기능)

- **게시글 생성** — 완료 버튼 클릭 시 서버에 게시글 저장 요청

---

## ④ 검증해야 하는 조건 (Validation)

| 검증 항목 | 조건 | 처리 방법 |
|---|---|---|
| 제목 필수 입력 | `title`이 null이거나 빈 문자열 | 예외 발생: "제목은 필수입니다!" |
| 제목 길이 제한 | `title`이 50자 초과 | 예외 발생: "제목은 50자 이하이어야 합니다!" |
| 본문 필수 입력 | `content`가 null이거나 빈 문자열 | 예외 발생: "내용은 필수입니다!" |
| 존재하지 않는 게시글 조회 | 요청한 id에 해당하는 게시글이 없음 | 예외 발생: "해당 게시글을 찾을 수 없습니다." |
| 클라이언트 검증 우회 방지 | 클라이언트에서 50자 제한을 걸어도 API 직접 호출로 우회 가능 | **서버에서도 반드시 유효성 검증 수행** |

> 💡 클라이언트에서 완료 버튼을 제목이 있어야만 활성화하더라도,  
> API를 직접 호출하면 빈 제목으로 요청이 올 수 있습니다.  
> 서버는 항상 자체적으로 검증해야 합니다.

---

## ⑤ 최종 정리 — Post 클래스 설계

화면설계서 분석 결과, 서버에서 관리해야 할 `Post` 클래스는 다음과 같이 설계할 수 있습니다:

```java
class Post {
    private Long id;             // 게시글 고유 식별자
    private String title;        // 제목 (최대 50자, 필수)
    private String content;      // 본문 (필수)
    private String author;       // 작성자
    private boolean isAnonymous; // 익명 여부
    private String createdAt;    // 작성 시각
    private int likeCount;       // 공감 수
    private int commentCount;    // 댓글 수 (3차 세미나에서 연동)
    private int scrapCount;      // 스크랩 수
}
```

> 오늘은 DB가 없으므로 `ArrayList`가 임시 저장소 역할을 합니다.  
> `commentCount`와 `scrapCount`는 3차 세미나(JPA, DB 연결)에서 실제 데이터와 연동될 예정입니다.