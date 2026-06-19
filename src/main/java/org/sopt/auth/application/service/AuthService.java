package org.sopt.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.sopt.auth.application.dto.TokenResult;
import org.sopt.auth.application.dto.UserInfoResult;
import org.sopt.auth.application.port.in.GetCurrentUserUseCase;
import org.sopt.auth.application.port.in.LoginUseCase;
import org.sopt.auth.application.port.in.ReissueTokenUseCase;
import org.sopt.auth.application.port.out.RefreshTokenRepositoryPort;
import org.sopt.auth.application.port.out.TokenProviderPort;
import org.sopt.auth.domain.RefreshToken;
import org.sopt.user.application.port.out.UserRepositoryPort;
import org.sopt.user.domain.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements LoginUseCase, GetCurrentUserUseCase, ReissueTokenUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final TokenProviderPort tokenProviderPort;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public TokenResult login(String email, String password) {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = tokenProviderPort.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = tokenProviderPort.generateRefreshToken(user.getId());

        refreshTokenRepositoryPort.deleteByUserId(user.getId());
        refreshTokenRepositoryPort.save(
                RefreshToken.create(user.getId(), refreshToken, tokenProviderPort.getRefreshTokenExpiresInSeconds())
        );

        return new TokenResult(accessToken, refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResult getCurrentUser(Long userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        return new UserInfoResult(user.getId(), user.getEmail());
    }

    @Override
    @Transactional
    public TokenResult reissue(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepositoryPort.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Refresh Token입니다."));

        if (refreshToken.isExpired()) {
            refreshTokenRepositoryPort.delete(refreshToken);
            throw new IllegalArgumentException("Refresh Token이 만료되었습니다. 다시 로그인해주세요.");
        }

        User user = userRepositoryPort.findById(refreshToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        String newAccessToken = tokenProviderPort.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = tokenProviderPort.generateRefreshToken(user.getId());

        refreshToken.rotate(newRefreshToken, tokenProviderPort.getRefreshTokenExpiresInSeconds());
        refreshTokenRepositoryPort.save(refreshToken);

        return new TokenResult(newAccessToken, newRefreshToken);
    }
}
