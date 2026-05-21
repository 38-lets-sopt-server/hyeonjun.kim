package org.sopt.service;

import lombok.RequiredArgsConstructor;
import org.sopt.domain.User;
import org.sopt.exception.ErrorCode;
import org.sopt.exception.NotFoundException;
import org.sopt.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public void join(String nickname, String email, String password) {
        User user = new User(nickname, email, password);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}
