package org.sopt.post.application.port.in;

import org.sopt.post.application.dto.UpdatePostCommand;

public interface UpdatePostUseCase {
	void updatePost(UpdatePostCommand command);
}
