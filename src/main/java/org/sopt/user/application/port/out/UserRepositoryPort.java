package org.sopt.user.application.port.out;

import java.util.Optional;

import org.sopt.user.domain.User;

public interface UserRepositoryPort {

	User save(User user);

	Optional<User> findById(Long id);

	Optional<User> findByEmail(String email);
}
