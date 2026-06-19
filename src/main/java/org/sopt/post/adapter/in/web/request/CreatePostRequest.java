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
