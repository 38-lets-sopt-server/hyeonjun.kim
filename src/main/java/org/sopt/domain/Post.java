package org.sopt.domain;

public class Post {
    // 필드 작성
    private Long id;
    private String title;
    private String content;
    private String author;
    private String createdAt;

    // 생성자
    public Post(Long id, String title, String content, String author, String createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
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
    public String getAuthor() {
        return this.author;
    }
    public String getCreatedAt() {
        return this.createdAt;
    }

    // update() 메서드 : 게시글 수정
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // getInfo() 메서드 : 게시글 정보 출력
    public String getInfo() {
        return "[" + id + "] " + title + " - " + author + " (" + createdAt + ")";
    }
}