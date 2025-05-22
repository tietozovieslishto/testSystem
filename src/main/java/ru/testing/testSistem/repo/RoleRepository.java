package ru.testing.testSistem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.testing.testSistem.models.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}