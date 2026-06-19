package org.sopt.auth.adapter.in.web.response;

import org.sopt.auth.application.dto.TokenResult;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
    public static TokenResponse from(TokenResult result) {
        return new TokenResponse(result.accessToken(), result.refreshToken());
    }
}
