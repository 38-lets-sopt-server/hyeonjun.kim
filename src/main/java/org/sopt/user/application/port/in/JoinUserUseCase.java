package org.sopt.user.application.port.in;

import org.sopt.user.application.dto.JoinUserCommand;

public interface JoinUserUseCase {
    void join(JoinUserCommand command);
}
