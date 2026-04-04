package org.sopt.dto.response;

// 게시글 조회 응답 (서버 → 클라이언트)
public class PostResponse {
    public Long id;
    public String title;
    public String content;
    public String author;
    public String createdAt;

    public PostResponse(Long id, String title, String content, String author, String createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + title + " - " + author + " (" + createdAt + ")\n" + content;
    }
}
