package org.sopt.user.adapter.in.web.request;

import org.sopt.user.application.dto.JoinUserCommand;

public record CreateUserRequest(
        String nickname,
        String email,
        String password
) {
    public JoinUserCommand toCommand() {
        return new JoinUserCommand(nickname, email, password);
    }
}
