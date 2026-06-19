package org.sopt.post.application.dto;

public record UpdatePostCommand(
	Long postId,
	String title,
	String content
) {
}
