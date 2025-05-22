package ru.testing.testSistem.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.testing.testSistem.DTO.RegistrationDto;
import ru.testing.testSistem.models.Role;
import ru.testing.testSistem.models.User;
import ru.testing.testSistem.repo.RoleRepository;
import ru.testing.testSistem.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    private static final int TOKEN_EXPIRATION_HOURS = 24;

    @Transactional
    public void registerUser(RegistrationDto registrationDto) {
        logger.debug("Начинаем регистрацию пользователя: {}", registrationDto.getUsername());

        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("Имя пользователя уже занято");
        }
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email уже используется");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEnabled(false); // Пользователь не активен до подтверждения email
        user.setEmailVerified(false);

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role newRole = new Role("ROLE_USER");
                    return roleRepository.save(newRole);
                });

        user.getRoles().add(userRole);

        User savedUser = userRepository.saveAndFlush(user);

        emailService.sendVerificationEmail(savedUser, token);
        logger.info("Пользователь {} успешно зарегистрирован. Ожидается подтверждение email.",
                registrationDto.getUsername());
    }

    @Transactional
    public boolean verifyUser(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Неверный токен подтверждения"));

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("Email уже подтвержден");
        }

        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Срок действия токена истек");
        }
        logger.info("Попытка подтверждения email с токеном: {}", token);
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);

        userRepository.save(user);
        return true;
    }
}