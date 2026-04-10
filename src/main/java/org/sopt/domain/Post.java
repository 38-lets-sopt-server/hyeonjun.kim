package org.sopt.domain;

public class Post {
    private Long id;
    private String title;
    private String content;
    private String author;
    private boolean isAnonymous;
    private String createdAt;

    public Post(Long id, String title, String content, String author, boolean isAnonymous, String createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.isAnonymous = isAnonymous;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public boolean isAnonymous() { return isAnonymous; }
    public String getCreatedAt() { return createdAt; }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}