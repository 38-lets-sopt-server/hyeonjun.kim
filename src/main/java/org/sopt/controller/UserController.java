package org.sopt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sopt.dto.request.CreateUserRequest;
import org.sopt.dto.response.BaseResponse;
import org.sopt.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "유저 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "회원가입")
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> join(
            @RequestBody CreateUserRequest request
    ) {
        userService.join(request.nickname(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(null));
    }
}
