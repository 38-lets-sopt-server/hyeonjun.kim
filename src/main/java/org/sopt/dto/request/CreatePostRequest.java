package org.sopt.dto.request;

public record CreatePostRequest(
        String title,
        String content,
        Long userId         // author(String) → userId(Long)으로 변경
) {}