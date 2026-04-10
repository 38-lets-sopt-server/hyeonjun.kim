package org.sopt.controller;

import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostResponse;
import org.sopt.service.PostService;

import java.util.ArrayList;
import java.util.List;

public class PostController {
    private final PostService postService = new PostService();

    // POST /posts
    public CreatePostResponse createPost(CreatePostRequest request) {
        try {
            return postService.createPost(request);
        } catch (IllegalArgumentException e) {
            return new CreatePostResponse(null, "🚫 " + e.getMessage());
        }
    }

    // GET /posts
    public List<PostResponse> getAllPosts() {
        try {
            return postService.getAllPosts();
        } catch (IllegalArgumentException e) {
            System.out.println("🚫 " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // GET /posts/{id}
    public PostResponse getPost(Long id) {
        try {
            return postService.getPost(id);
        } catch (IllegalArgumentException e) {
            System.out.println("🚫 " + e.getMessage());
            return null;
        }
    }

    // PUT /posts/{id}
    public void updatePost(Long id, String newTitle, String newContent) {
        try {
            postService.updatePost(id, newTitle, newContent);
            System.out.println("✅ 게시글이 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("🚫 " + e.getMessage());
        }
    }

    // DELETE /posts/{id}
    public void deletePost(Long id) {
        try {
            postService.deletePost(id);
            System.out.println("✅ 게시글이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("🚫 " + e.getMessage());
        }
    }
}
