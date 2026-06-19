package org.sopt.post.adapter.in.web.request;

import org.sopt.post.application.dto.UpdatePostCommand;

public record UpdatePostRequest(
	String title,
	String content
) {
	public UpdatePostCommand toCommand(Long postId) {
		return new UpdatePostCommand(postId, title, content);
	}
}
