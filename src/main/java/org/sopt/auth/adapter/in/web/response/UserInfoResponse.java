package org.sopt.auth.adapter.in.web.response;

import org.sopt.auth.application.dto.UserInfoResult;

public record UserInfoResponse(
        Long id,
        String email
) {
    public static UserInfoResponse from(UserInfoResult result) {
        return new UserInfoResponse(result.id(), result.email());
    }
}
