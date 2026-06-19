package org.sopt.post.adapter.out.persistence;

import java.util.List;

import org.sopt.post.domain.BoardType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostJpaRepository extends JpaRepository<PostJpaEntity, Long> {
	List<PostJpaEntity> findAllByBoardType(BoardType boardType);
}
