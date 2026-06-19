package org.sopt.auth.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.sopt.auth.application.port.out.RefreshTokenRepositoryPort;
import org.sopt.auth.domain.RefreshToken;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenJpaRepository.save(RefreshTokenJpaEntity.from(refreshToken)).toDomain();
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenJpaRepository.findByToken(token).map(RefreshTokenJpaEntity::toDomain);
    }

    @Override
    public void deleteByUserId(Long userId) {
        refreshTokenJpaRepository.deleteByUserId(userId);
    }

    @Override
    public void delete(RefreshToken refreshToken) {
        refreshTokenJpaRepository.deleteById(refreshToken.getId());
    }
}
