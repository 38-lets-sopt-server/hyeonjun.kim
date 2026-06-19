package org.sopt.post.application.dto;

import org.sopt.post.domain.BoardType;

public record CreatePostCommand(
	String title,
	String content,
	Long userId,
	BoardType boardType
) {
}
