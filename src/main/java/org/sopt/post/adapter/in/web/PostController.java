package org.sopt.post.adapter.in.web;

import java.util.List;

import org.sopt.post.adapter.in.web.request.CreatePostRequest;
import org.sopt.post.adapter.in.web.request.UpdatePostRequest;
import org.sopt.post.adapter.in.web.response.PostResponse;
import org.sopt.post.application.port.in.CreatePostUseCase;
import org.sopt.post.application.port.in.DeletePostUseCase;
import org.sopt.post.application.port.in.GetPostUseCase;
import org.sopt.post.application.port.in.UpdatePostUseCase;
import org.sopt.post.domain.BoardType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

	// Service가 아닌 UseCase 인터페이스에 의존!
	private final CreatePostUseCase createPostUseCase;
	private final GetPostUseCase getPostUseCase;
	private final UpdatePostUseCase updatePostUseCase;
	private final DeletePostUseCase deletePostUseCase;

	@PostMapping
	public ResponseEntity<Void> createPost(
		@RequestBody CreatePostRequest request,
		Authentication authentication
	) {
		Long userId = Long.parseLong(authentication.getName());
		createPostUseCase.createPost(request.toCommand(userId));
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping
	public ResponseEntity<List<PostResponse>> getAllPosts(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(required = false) BoardType boardType
	) {
		List<PostResponse> posts = getPostUseCase.getAllPosts(page, size, boardType)
			.stream()
			.map(PostResponse::from)
			.toList();
		return ResponseEntity.ok(posts);
	}

	@GetMapping("/{id}")
	public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
		return ResponseEntity.ok(PostResponse.from(getPostUseCase.getPost(id)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Void> updatePost(
		@PathVariable Long id,
		@RequestBody UpdatePostRequest request
	) {
		updatePostUseCase.updatePost(request.toCommand(id));
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePost(@PathVariable Long id) {
		deletePostUseCase.deletePost(id);
		return ResponseEntity.ok().build();
	}
}
