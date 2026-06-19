package org.sopt.user.domain;

// @Entity, @Getter, @NoArgsConstructor 없음
public class User {

	private final Long id;
	private final String nickname;
	private final String email;
	private final String password;

	public User(Long id, String nickname, String email, String password) {
		this.id = id;
		this.nickname = nickname;
		this.email = email;
		this.password = password;
	}

	// 신규 회원 (id 없음)
	public User(String nickname, String email, String password) {
		this(null, nickname, email, password);
	}

	public Long getId() {
		return id;
	}

	public String getNickname() {
		return nickname;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}
}
