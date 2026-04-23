package org.sopt.controller;

import org.sopt.domain.Post;
import org.sopt.dto.response.ApiResponse;
import org.sopt.service.PostService;

import java.util.List;


public class PostController {
    private final PostService postService = new PostService();


    public ApiResponse<Void> createPost(String title, String content, String author) {
        try {
            // postService.createPost() 호출
            postService.createPost(title, content, author);
            // 성공 -> ApiResponse.success(null) 반환
            return ApiResponse.success(null);
        } catch (Exception e) {
            // 예외 -> ApiResponse.fail(예외메세지) 반환
            return ApiResponse.fail(e.getMessage());
        }
    }

    public ApiResponse<List<Post>> getAllPosts() {
        try {
            List<Post> posts = postService.getAllPosts();
            return ApiResponse.success(posts);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    public ApiResponse<Post> getPost(Long id) {
        try {
            Post post = postService.getPost(id);
            return ApiResponse.success(post);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    public ApiResponse<Void> updatePost(Long id, String title, String newContent) {
        try {
            postService.updatePost(id, title, newContent);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    public ApiResponse<Void> deletePost(Long id) {
        try {
            postService.deletePost(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
