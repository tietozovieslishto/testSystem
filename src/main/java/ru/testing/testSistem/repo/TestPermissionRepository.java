package ru.testing.testSistem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.testing.testSistem.models.TestPermission;
import ru.testing.testSistem.models.User;

import java.util.List;
import java.util.Optional;

public interface TestPermissionRepository extends JpaRepository<TestPermission, Long> {
    Optional<TestPermission> findByTestIdAndUserId(Long testId, Long userId);
    boolean existsByTestIdAndUserIdAndAllowedTrue(Long testId, Long userId);

    @Query("SELECT tp.user FROM TestPermission tp WHERE tp.test.id = :testId AND tp.allowed = true")
    List<User> findAllAllowedUsersByTestId(@Param("testId") Long testId);  // поиск всех пользователей у которых есть доступ к тесту



    // поиск пользователей без доступа
    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
            "(SELECT tp.user.id FROM TestPermission tp WHERE tp.test.id = :testId) " +
            "AND (u.username LIKE %:query% OR u.email LIKE %:query%)")
    List<User> findUsersWithoutAccess(@Param("testId") Long testId, @Param("query") String query);
}
