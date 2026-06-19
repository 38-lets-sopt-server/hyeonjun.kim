package org.sopt.post.application.port.out;

import java.util.Optional;

import org.sopt.post.domain.BoardType;
import org.sopt.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryPort {

	Post save(Post post);

	Optional<Post> findById(Long id);

	Page<Post> findAll(BoardType boardType, Pageable pageable);

	void delete(Post post);
}
