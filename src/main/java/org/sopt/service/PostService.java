package org.sopt.service;

import org.sopt.domain.BoardType;
import org.sopt.domain.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.sopt.domain.Post;
// import org.sopt.exception.PostNotFoundException;
import org.sopt.repository.PostRepository;
import org.sopt.repository.UserRepository;
import org.sopt.validator.PostValidator;
import org.springframework.stereotype.Service;

import org.sopt.exception.NotFoundException;
import org.sopt.exception.ErrorCode;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createPost(String title, String content, Long userId, BoardType boardType) {
        PostValidator.validate(title, content);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        Post post = new Post(title, content, boardType, user);  // boardType 추가
        postRepository.save(post);
    }

    // getAllPosts() : 게시글 전체 조회
    @Transactional(readOnly = true)
    public List<Post> getAllPosts(int page, int size, BoardType boardType) {
        if (boardType != null) {
            return postRepository.findAllByBoardType(boardType);  // 게시판별 필터링
        }
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findAll(pageable).getContent();
    }



    // getPost(Long id) : 게시글 단일 조회
    @Transactional(readOnly = true)
    public Post getPost(Long id) {
        // findById()로 찾기
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));
    }

    // updatePost() : 게시글 수정
    @Transactional
    public void updatePost(Long id, String title, String newContent) {
        // 유효성 검증
        PostValidator.validate(title, newContent);

        // id로 게시글 찾기
        Post post = postRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        // 수정하기
        post.update(title, newContent);
    }

    // deletePost() : 게시글 삭제
    @Transactional
    public void deletePost(Long id) {
        // id로 게시글 찾기
        Post post = postRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));
        postRepository.delete(post);
    }
}
