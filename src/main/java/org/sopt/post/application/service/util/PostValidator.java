package org.sopt.post.application.service.util;

import org.sopt.common.exception.BadRequestException;
import org.sopt.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class PostValidator {

	public void validate(String title, String content) {
		if (title == null || title.isBlank()) {
			throw new BadRequestException(ErrorCode.POST_TITLE_EMPTY);
		}
		if (content == null || content.isBlank()) {
			throw new BadRequestException(ErrorCode.POST_CONTENT_EMPTY);
		}
	}
}
