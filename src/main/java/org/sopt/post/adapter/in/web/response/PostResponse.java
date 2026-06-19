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
