package ru.testing.testSistem.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import ru.testing.testSistem.controllers.RegistrationController;
import  ru.testing.testSistem.models.User;
import ru.testing.testSistem.models.Role;
import ru.testing.testSistem.repo.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Ищем пользователя: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        logger.info("Найден пользователь: {}", user.getUsername());
        logger.info("Хеш пароля в БД: {}", user.getPassword());
        logger.info("Активен: {}", user.isEnabled());
        logger.info("Роли: {}", user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        if (!user.isEmailVerified()) {
            throw new UsernameNotFoundException("Email не подтвержден для пользователя: " + username);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user.getRoles()))
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}