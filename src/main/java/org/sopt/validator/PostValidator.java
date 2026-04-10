package org.sopt.validator;

import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.request.UpdatePostRequest;

public class PostValidator {

    public static void validateCreate(CreatePostRequest request) {
        validateTitle(request.title);
        validateContent(request.content);
    }

    public static void validateUpdate(UpdatePostRequest request) {
        validateTitle(request.title);
        validateContent(request.content);
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다!");
        }
        if (title.length() > 50) {
            throw new IllegalArgumentException("제목은 50자 이하이어야 합니다!");
        }
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다!");
        }
    }
}
