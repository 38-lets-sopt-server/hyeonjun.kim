package org.sopt.service;

import org.sopt.domain.Post;
import org.sopt.exception.PostNotFoundException;
import org.sopt.repository.PostRepository;
import org.sopt.validator.PostValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // createPost() : 새 게시글 생성
    public void createPost(String title, String content, String author) {
        // 유효성 검증 (PostValidator 사용)
        PostValidator.validate(title, content);

        // Post 객체 생성
        String createAd = java.time.LocalDateTime.now().toString();
        Post post = new Post(postRepository.generateId(), title, content, author, createAd);

        // 저장
        postRepository.save(post);
    }

    // getAllPosts() : 게시글 전체 조회
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    // getPost(Long id) : 게시글 단일 조회
    public Post getPost(Long id) {
        // findById()로 찾기
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    // updatePost() : 게시글 수정
    public void updatePost(Long id, String title, String newContent) {
        // 유효성 검증
        PostValidator.validate(title, newContent);

        // id로 게시글 찾기
        Post post = postRepository.findById(id)
                    .orElseThrow(() -> new PostNotFoundException(id));

        // 수정하기
        post.update(title, newContent);
    }

    // deletePost() : 게시글 삭제
    public void deletePost(Long id) {
        // id로 게시글 찾기
        Post post = postRepository.findById(id)
                    .orElseThrow(() -> new PostNotFoundException(id));
        postRepository.deleteById(id);
    }
}
