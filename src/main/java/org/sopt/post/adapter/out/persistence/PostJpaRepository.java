package org.sopt.post.adapter.out.persistence;

import org.sopt.post.domain.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostJpaRepository extends JpaRepository<PostJpaEntity, Long> {
	Page<PostJpaEntity> findAllByBoardType(BoardType boardType, Pageable pageable);
}
