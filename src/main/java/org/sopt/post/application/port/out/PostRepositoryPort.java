package org.sopt.post.application.port.out;

import java.util.List;
import java.util.Optional;

import org.sopt.post.domain.BoardType;
import org.sopt.post.domain.Post;

public interface PostRepositoryPort {

	Post save(Post post);

	Optional<Post> findById(Long id);

	List<Post> findAll(int page, int size);

	List<Post> findAllByBoardType(BoardType boardType);

	void delete(Post post);
}
