package org.sopt.repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);  // 메서드 이름으로 쿼리 자동 생성
}