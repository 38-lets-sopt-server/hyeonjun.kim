package org.sopt.controller;

import org.sopt.domain.BoardType;
import org.sopt.dto.request.UpdatePostRequest;
import org.sopt.dto.response.BaseResponse;
import org.sopt.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.response.PostResponse;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<BaseResponse<Void>> createPost(@RequestBody CreatePostRequest request) {
        postService.createPost(request.title(), request.content(), request.userId(), request.boardType());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(null));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<PostResponse>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) BoardType boardType  // 추가! 없으면 전체 조회
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(
                postService.getAllPosts(page, size, boardType).stream()
                        .map(PostResponse::from)
                        .toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<PostResponse>> getPost(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(PostResponse.from(postService.getPost(id))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> updatePost(@PathVariable Long id, @RequestBody UpdatePostRequest request) {
        postService.updatePost(id, request.title(), request.content());
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(null)); // 통일!
    }
}
