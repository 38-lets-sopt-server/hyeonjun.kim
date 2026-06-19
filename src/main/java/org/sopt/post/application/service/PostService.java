package org.sopt.post.application.service;

import java.util.List;

import org.sopt.common.exception.ErrorCode;
import org.sopt.common.exception.NotFoundException;
import org.sopt.post.application.dto.CreatePostCommand;
import org.sopt.post.application.dto.UpdatePostCommand;
import org.sopt.post.application.port.in.CreatePostUseCase;
import org.sopt.post.application.port.in.DeletePostUseCase;
import org.sopt.post.application.port.in.GetPostUseCase;
import org.sopt.post.application.port.in.UpdatePostUseCase;
import org.sopt.post.application.port.out.PostRepositoryPort;
import org.sopt.post.application.service.util.PostValidator;
import org.sopt.post.domain.BoardType;
import org.sopt.post.domain.Post;
import org.sopt.user.application.port.out.UserRepositoryPort;
import org.sopt.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService implements CreatePostUseCase, GetPostUseCase,
	UpdatePostUseCase, DeletePostUseCase {

	// 출력 포트에 의존 (JPA를 직접 의존하지 않음!)
	private final PostRepositoryPort postRepositoryPort;
	private final UserRepositoryPort userRepositoryPort;

	private final PostValidator postValidator;

	@Override
	@Transactional
	public void createPost(CreatePostCommand command) {
		// 유효성 검증 (도메인 규칙)
		postValidator.validate(command.title(), command.content());

		User user = userRepositoryPort.findById(command.userId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		Post post = new Post(command.title(), command.content(), command.boardType(), user);
		postRepositoryPort.save(post);
	}

	@Override
	@Transactional(readOnly = true)
	public Post getPost(Long id) {
		return postRepositoryPort.findById(id)
			.orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Post> getAllPosts(int page, int size, BoardType boardType) {
		if (boardType != null) {
			return postRepositoryPort.findAllByBoardType(boardType);
		}
		return postRepositoryPort.findAll(page, size);
	}

	@Override
	@Transactional
	public void updatePost(UpdatePostCommand command) {
		postValidator.validate(command.title(), command.content());

		Post post = postRepositoryPort.findById(command.postId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

		post.update(command.title(), command.content());
		postRepositoryPort.save(post);
	}

	@Override
	@Transactional
	public void deletePost(Long id) {
		Post post = postRepositoryPort.findById(id)
			.orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));
		postRepositoryPort.delete(post);
	}
}
