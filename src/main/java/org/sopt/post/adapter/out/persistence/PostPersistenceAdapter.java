package org.sopt.post.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.sopt.post.application.port.out.PostRepositoryPort;
import org.sopt.post.domain.BoardType;
import org.sopt.post.domain.Post;
import org.sopt.user.adapter.out.persistence.UserJpaEntity;
import org.sopt.user.adapter.out.persistence.UserJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostRepositoryPort {

	private final PostJpaRepository postJpaRepository;
	private final UserJpaRepository userJpaRepository;  // User JPA가 필요

	@Override
	public Post save(Post post) {
		// 1. User 도메인 → UserJpaEntity 조회
		UserJpaEntity userJpaEntity = userJpaRepository.findById(post.getAuthor().getId())
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		// 2. Post 도메인 → PostJpaEntity 변환
		PostJpaEntity entity = PostJpaEntity.from(post, userJpaEntity);

		// 3. 저장 후 도메인으로 변환하여 반환
		return postJpaRepository.save(entity).toDomain();
	}

	@Override
	public Optional<Post> findById(Long id) {
		return postJpaRepository.findById(id).map(PostJpaEntity::toDomain);
	}

	@Override
	public List<Post> findAll(int page, int size) {
		return postJpaRepository.findAll(PageRequest.of(page, size))
			.getContent()
			.stream()
			.map(PostJpaEntity::toDomain)
			.toList();
	}

	@Override
	public List<Post> findAllByBoardType(BoardType boardType) {
		return postJpaRepository.findAllByBoardType(boardType)
			.stream()
			.map(PostJpaEntity::toDomain)
			.toList();
	}

	@Override
	public void delete(Post post) {
		postJpaRepository.deleteById(post.getId());
	}
}
