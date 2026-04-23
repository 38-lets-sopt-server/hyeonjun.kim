package org.sopt.validator;

public class PostValidator {
    public static void validate(String title, String content) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is blank");
        }
        if (title.length() > 50) {
            throw new IllegalArgumentException("title is too long");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is blank");
        }
    }
}
