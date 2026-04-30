package org.sopt.dto.response;

import org.sopt.domain.Post;

public record PostResponse(
        Long id,
        String title,
        String content,
        String author,      // 작성자 닉네임
        String createdAt    // BaseTimeEntity에서 자동 관리
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getNickname(),           // author → User 닉네임으로
                post.getCreatedAt().toString()          // BaseTimeEntity의 createdAt
        );
    }
}