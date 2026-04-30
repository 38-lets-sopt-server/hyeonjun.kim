package org.sopt.domain;

import jakarta.persistence.*;

@Entity
public class Post extends BaseTimeEntity {  // BaseTimeEntity 상속 추가!

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    protected Post() {}

    public Post(String title, String content, User user) {  // id 제거! JPA가 자동 발급해요
        this.title = title;
        this.content = content;
        this.user = user;
    }

    public Long getId() { return this.id; }
    public String getTitle() { return this.title; }
    public String getContent() { return this.content; }
    public User getUser() { return this.user; }  // getUser() getter 추가!

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getInfo() {
        return "[" + id + "] " + title;
    }
}