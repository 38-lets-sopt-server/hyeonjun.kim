package org.sopt.user.application.service;

import lombok.RequiredArgsConstructor;
import org.sopt.user.application.dto.JoinUserCommand;
import org.sopt.user.application.port.in.JoinUserUseCase;
import org.sopt.user.application.port.out.UserRepositoryPort;
import org.sopt.user.domain.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements JoinUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void join(JoinUserCommand command) {
        String encodedPassword = passwordEncoder.encode(command.password());
        User user = new User(command.nickname(), command.email(), encodedPassword);
        userRepositoryPort.save(user);
    }
}
