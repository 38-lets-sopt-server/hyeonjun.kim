package org.sopt.auth.adapter.out.persistence;

import org.sopt.auth.domain.RefreshToken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
public class RefreshTokenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    protected RefreshTokenJpaEntity() {}

    private RefreshTokenJpaEntity(Long id, Long userId, String token, LocalDateTime expiresAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public static RefreshTokenJpaEntity from(RefreshToken domain) {
        return new RefreshTokenJpaEntity(domain.getId(), domain.getUserId(), domain.getToken(), domain.getExpiresAt());
    }

    public RefreshToken toDomain() {
        return new RefreshToken(id, userId, token, expiresAt);
    }

    public Long getId() { return id; }
}
