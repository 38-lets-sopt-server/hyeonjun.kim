package org.sopt.auth.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sopt.auth.adapter.in.web.response.TokenResponse;
import org.sopt.auth.adapter.in.web.response.UserInfoResponse;
import org.sopt.auth.application.port.in.GetCurrentUserUseCase;
import org.sopt.auth.application.port.in.LoginUseCase;
import org.sopt.auth.application.port.in.ReissueTokenUseCase;
import org.sopt.common.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final ReissueTokenUseCase reissueTokenUseCase;

    @Operation(summary = "로그인 (Access Token + Refresh Token 발급)")
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenResponse>> login(
            @RequestParam("email") String email,
            @RequestParam("password") String password
    ) {
        return ResponseEntity.ok(BaseResponse.success(TokenResponse.from(loginUseCase.login(email, password))));
    }

    @Operation(summary = "내 정보 조회 (Access Token 검증)")
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserInfoResponse>> me(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("인증되지 않았습니다.");
        }
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(BaseResponse.success(
                UserInfoResponse.from(getCurrentUserUseCase.getCurrentUser(userId))));
    }

    @Operation(summary = "토큰 재발급 (Refresh Token → 새 토큰 발급)")
    @PostMapping("/reissue")
    public ResponseEntity<BaseResponse<TokenResponse>> reissue(
            @RequestHeader("Authorization") String authorization
    ) {
        String refreshToken = authorization.substring("Bearer ".length()).trim();
        return ResponseEntity.ok(BaseResponse.success(TokenResponse.from(reissueTokenUseCase.reissue(refreshToken))));
    }
}
