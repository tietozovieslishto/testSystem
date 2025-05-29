package ru.testing.testSistem.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.testing.testSistem.models.Test;
import ru.testing.testSistem.models.TestPermission;
import ru.testing.testSistem.models.User;
import ru.testing.testSistem.repo.TestPermissionRepository;
import ru.testing.testSistem.repo.TestRepository;
import ru.testing.testSistem.repo.UserRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TestPermissionService {
    private final TestPermissionRepository testPermissionRepository;
    private final TestRepository testRepository;
    private final UserRepository userRepository;

    /*
      выдаёт доступ к тесту
     */
    public void grantAccess(Long testId, Long userId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with id: " + testId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        testPermissionRepository.findByTestIdAndUserId(testId, userId)
                .ifPresentOrElse(
                        permission -> permission.setAllowed(true),  // permission - найденный объект
                        () -> {
                            TestPermission newPermission = new TestPermission();
                            newPermission.setTest(test);
                            newPermission.setUser(user);
                            newPermission.setAllowed(true);
                            testPermissionRepository.save(newPermission);
                        }
                );
    }


    //  Отзывает доступ пользователя к тесту

    public void revokeAccess(Long testId, Long userId) {
        testPermissionRepository.findByTestIdAndUserId(testId, userId)
                .ifPresent(permission -> {
                    permission.setAllowed(false);
                    testPermissionRepository.save(permission);
                });
    }


    //  Проверяет доступ пользователя к тесту

    public boolean hasAccess(Long testId, Long userId) {
        // Если тест не приватный - доступ есть у всех
        if (!testRepository.findById(testId)
                .orElseThrow(() -> new EntityNotFoundException("Test not found"))
                .isPrivate()) {
            return true;
        }

        return testPermissionRepository.existsByTestIdAndUserIdAndAllowedTrue(testId, userId);
    }




    public boolean canEditTest(Long userId, Long testId) {
        // Автор теста может его редактировать
        if (testRepository.existsByIdAndAuthorId(testId, userId)) {
            return true;
        }

        // Администраторы могут редактировать любой тест
        return userRepository.existsByIdAndRoles_Name(userId, "ROLE_ADMIN");
    }



    public List<User> getAllowedUsers(Long testId) {
        return testPermissionRepository.findAllAllowedUsersByTestId(testId); // Поиск пользователей с доступом к тесту
    }
    /*
    поиск пользователей без доступа
      */
    public List<User> searchUsersToAdd(Long testId, String query) {
        return testPermissionRepository.findUsersWithoutAccess(testId, query);
    }
}