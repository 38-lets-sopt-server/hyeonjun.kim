package org.sopt.post.adapter.out.persistence;

import java.time.LocalDateTime;

import org.sopt.post.domain.BoardType;
import org.sopt.post.domain.Post;
import org.sopt.user.adapter.out.persistence.UserJpaEntity;
import org.sopt.user.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

	protected PostJpaEntity() {
	}

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

	public Long getId() {
		return id;
	}
}
