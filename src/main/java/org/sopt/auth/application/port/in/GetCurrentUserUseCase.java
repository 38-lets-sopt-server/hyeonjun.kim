package org.sopt.auth.application.port.in;

import org.sopt.auth.application.dto.UserInfoResult;

public interface GetCurrentUserUseCase {
    UserInfoResult getCurrentUser(Long userId);
}
