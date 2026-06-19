package org.sopt.auth.application.port.in;

import org.sopt.auth.application.dto.TokenResult;

public interface LoginUseCase {
    TokenResult login(String email, String password);
}
