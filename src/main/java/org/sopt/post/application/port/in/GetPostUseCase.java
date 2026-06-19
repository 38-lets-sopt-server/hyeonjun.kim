package org.sopt.post.application.port.in;

import org.sopt.post.adapter.in.web.response.PostResponse;
import org.sopt.post.domain.BoardType;
import org.springframework.data.domain.Page;

public interface GetPostUseCase {

	PostResponse getPost(Long id);

	Page<PostResponse> getAllPosts(int page, int size, BoardType boardType);
}
