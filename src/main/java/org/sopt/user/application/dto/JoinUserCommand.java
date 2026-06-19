package org.sopt.user.application.dto;

public record JoinUserCommand(
        String nickname,
        String email,
        String password
) {}
