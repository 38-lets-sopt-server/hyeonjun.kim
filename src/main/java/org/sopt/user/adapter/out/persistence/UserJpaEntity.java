package org.sopt.user.adapter.out.persistence;

import org.sopt.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    protected UserJpaEntity() {}

    private UserJpaEntity(Long id, String nickname, String email, String password) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }

    public static UserJpaEntity from(User user) {
        return new UserJpaEntity(user.getId(), user.getNickname(), user.getEmail(), user.getPassword());
    }

    public User toDomain() {
        return new User(id, nickname, email, password);
    }

    public Long getId() { return id; }
    public String getNickname() { return nickname; }
    public String getEmail() { return email; }
}
