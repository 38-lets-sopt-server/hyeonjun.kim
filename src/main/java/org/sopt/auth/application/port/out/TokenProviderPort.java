package org.sopt.auth.application.port.out;

public interface TokenProviderPort {
    String generateAccessToken(Long userId, String email);
    String generateRefreshToken(Long userId);
    Long verifyAndGetUserId(String token);
    long getRefreshTokenExpiresInSeconds();
}
