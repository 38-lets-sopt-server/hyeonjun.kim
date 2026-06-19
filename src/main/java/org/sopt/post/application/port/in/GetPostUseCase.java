package org.sopt.post.application.port.in;

import java.util.List;

import org.sopt.post.domain.BoardType;
import org.sopt.post.domain.Post;

public interface GetPostUseCase {
	Post getPost(Long id);

	List<Post> getAllPosts(int page, int size, BoardType boardType);
}
