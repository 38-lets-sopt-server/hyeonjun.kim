package org.sopt.repository;

import org.sopt.domain.Post;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
//public class PostRepository {
public interface PostRepository extends JpaRepository<Post, Long> {
    // DB 대신 배열에 저장 (나중에 DB 추가하면 삭제)
    private final List<Post> postList = new ArrayList<>();
    private Long nextId = 1L;

    // save() 메서드 : postList에 게시글 저장
    public Post save(Post post) {
        postList.add(post);
        return post;
    }

    // generateId() 메서드 : id 발급
//    public Long generateId() {
//        return nextId++;
//    }

    // findAll() 메서드 : 게시글 전체 목록 조회
    public List<Post> findAll() {
        return postList;
    }

    // findById() : id로 특정 게시글 조회
    public Optional<Post> findById(Long id) {
        return postList.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    // deleteById() : id로 특정 게시글 삭제
    public boolean deleteById(Long id) {
        return postList.removeIf(p -> p.getId().equals(id));
    }
}