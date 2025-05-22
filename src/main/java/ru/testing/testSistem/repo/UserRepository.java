package ru.testing.testSistem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.testing.testSistem.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    // Проверка на админа
    boolean existsByIdAndRoles_Name(Long userId, String roleName);
    //  поиск по токену подтверждения
    Optional<User> findByVerificationToken(String verificationToken);
}