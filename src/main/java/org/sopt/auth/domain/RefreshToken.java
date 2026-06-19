package org.sopt.auth.domain;

import java.time.LocalDateTime;

public class RefreshToken {

    private final Long id;
    private final Long userId;
    private String token;
    private LocalDateTime expiresAt;

    public RefreshToken(Long id, Long userId, String token, LocalDateTime expiresAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public static RefreshToken create(Long userId, String token, long expiresInSeconds) {
        return new RefreshToken(null, userId, token, LocalDateTime.now().plusSeconds(expiresInSeconds));
    }

    public void rotate(String newToken, long expiresInSeconds) {
        this.token = newToken;
        this.expiresAt = LocalDateTime.now().plusSeconds(expiresInSeconds);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getToken() { return token; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
