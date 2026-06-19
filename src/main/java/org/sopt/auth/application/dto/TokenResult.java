package org.sopt.auth.application.dto;

public record TokenResult(
        String accessToken,
        String refreshToken
) {}
