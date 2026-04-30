package org.sopt.domain;

import jakarta.persistence.*;

@Entity
public class Post {
    // 필드 작성
    @Id // 앞에서 배운 PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)  // User : Post = 1 : N
    @JoinColumn(name = "user_id")       // post 테이블에 user_id FK 컬럼이 생겨요
    private User user;

//    private String author;
//    private String createdAt;

    protected Post() {}    // JPA 기본 생성자

    // 생성자
    public Post(Long id, String title, String content, User user) { //String author, String createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.user = user;
//        this.author = author;
//        this.createdAt = createdAt;
    }

    // Getter
    public Long getId() {
        return this.id;
    }
    public String getTitle() {
        return this.title;
    }
    public String getContent() {
        return this.content;
    }
//    public String getAuthor() {
//        return this.author;
//    }
//    public String getCreatedAt() {
//        return this.createdAt;
//    }

    // update() 메서드 : 게시글 수정
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // getInfo() 메서드 : 게시글 정보 출력
    public String getInfo() {
        return "[" + id + "] " + title;
    }
}