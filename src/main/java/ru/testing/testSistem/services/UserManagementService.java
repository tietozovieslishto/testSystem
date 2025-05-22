package ru.testing.testSistem.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.testing.testSistem.models.Role;
import ru.testing.testSistem.models.User;
import ru.testing.testSistem.repo.RoleRepository;
import ru.testing.testSistem.repo.UserRepository;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public void changeUserRole(String username, String roleName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Role newRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid role"));

        user.getRoles().clear();
        user.getRoles().add(newRole);
        userRepository.save(user);
    }
}