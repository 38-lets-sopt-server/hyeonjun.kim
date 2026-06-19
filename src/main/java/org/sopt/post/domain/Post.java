package org.sopt.post.domain;

import java.time.LocalDateTime;

import org.sopt.user.domain.User;

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
	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public BoardType getBoardType() {
		return boardType;
	}

	public User getAuthor() {
		return author;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
