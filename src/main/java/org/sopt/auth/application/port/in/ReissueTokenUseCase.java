package org.sopt.auth.application.port.in;

import org.sopt.auth.application.dto.TokenResult;

public interface ReissueTokenUseCase {
    TokenResult reissue(String refreshToken);
}
