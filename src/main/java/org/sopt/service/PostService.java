package org.sopt.service;

import org.sopt.domain.Post;
import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.request.UpdatePostRequest;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostResponse;
import org.sopt.repository.PostRepository;
import org.sopt.validator.PostValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PostService {
    private final PostRepository postRepository = new PostRepository();

    // CREATE
    public CreatePostResponse createPost(CreatePostRequest request) {
        PostValidator.validateCreate(request);
        String createdAt = LocalDate.now().toString();
        Post post = new Post(
                postRepository.generateId(),
                request.title,
                request.content,
                request.author,
                false,
                createdAt
        );
        postRepository.save(post);
        return new CreatePostResponse(post.getId(), "게시글 등록 완료!");
    }

    // READ - 전체
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(p -> new PostResponse(p.getId(), p.getTitle(), p.getContent(), p.getAuthor(), p.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // READ - 단건
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(), post.getAuthor(), post.getCreatedAt());
    }

    // UPDATE
    public void updatePost(Long id, String newTitle, String newContent) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
        PostValidator.validateUpdate(new UpdatePostRequest(newTitle, newContent));
        post.update(newTitle, newContent);
    }

    // DELETE
    public void deletePost(Long id) {
        if (!postRepository.deleteById(id)) {
            throw new IllegalArgumentException("해당 게시글을 찾을 수 없습니다.");
        }
    }
}
