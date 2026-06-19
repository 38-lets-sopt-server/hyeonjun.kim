package org.sopt.post.adapter.out.persistence;

import java.util.Optional;

import org.sopt.common.exception.ErrorCode;
import org.sopt.common.exception.NotFoundException;
import org.sopt.post.application.port.out.PostRepositoryPort;
import org.sopt.post.domain.BoardType;
import org.sopt.post.domain.Post;
import org.sopt.user.adapter.out.persistence.UserJpaEntity;
import org.sopt.user.adapter.out.persistence.UserJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostRepositoryPort {

	private final PostJpaRepository postJpaRepository;
	private final UserJpaRepository userJpaRepository;

	@Override
	public Post save(Post post) {
		UserJpaEntity userJpaEntity = userJpaRepository.findById(post.getAuthor().getId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		PostJpaEntity entity = PostJpaEntity.from(post, userJpaEntity);

		return postJpaRepository.save(entity).toDomain();
	}

	@Override
	public Optional<Post> findById(Long id) {
		return postJpaRepository.findById(id).map(PostJpaEntity::toDomain);
	}

	@Override
	public Page<Post> findAll(BoardType boardType, Pageable pageable) {
		Page<PostJpaEntity> entityPage = (boardType != null)
			? postJpaRepository.findAllByBoardType(boardType, pageable)
			: postJpaRepository.findAll(pageable);

		return entityPage.map(PostJpaEntity::toDomain);
	}

	@Override
	public void delete(Post post) {
		postJpaRepository.deleteById(post.getId());
	}
}
