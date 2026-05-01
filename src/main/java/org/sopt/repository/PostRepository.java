package org.sopt.repository;

import org.sopt.domain.BoardType;
import org.sopt.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // JPA가 save / findAll / findById / deleteById 전부 자동 제공!
    // 여기엔 아무것도 안 써도 돼요
    List<Post> findAllByBoardType(BoardType boardType);
}