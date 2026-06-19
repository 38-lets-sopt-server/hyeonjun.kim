package org.sopt.user.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sopt.common.response.BaseResponse;
import org.sopt.user.adapter.in.web.request.CreateUserRequest;
import org.sopt.user.application.port.in.JoinUserUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "유저 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final JoinUserUseCase joinUserUseCase;

    @Operation(summary = "회원가입")
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> join(@RequestBody CreateUserRequest request) {
        joinUserUseCase.join(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(null));
    }
}
