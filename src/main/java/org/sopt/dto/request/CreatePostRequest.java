package org.sopt.dto.request;

import org.sopt.domain.BoardType;

public record CreatePostRequest(
        String title,
        String content,
        Long userId,         // author(String) → userId(Long)으로 변경
        BoardType boardType
) {}