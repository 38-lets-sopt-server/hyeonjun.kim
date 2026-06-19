package org.sopt.post.application.port.in;

import org.sopt.post.application.dto.CreatePostCommand;

public interface CreatePostUseCase {
	void createPost(CreatePostCommand command);
}
