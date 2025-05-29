package ru.testing.testSistem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.testing.testSistem.models.Test;

import java.time.LocalDate;
import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {

    boolean existsByIdAndAuthorId(Long testId, Long authorId);

    List<Test> findByAuthorId(Long authorId);
    // Поиск по названию или описанию
    @Query("SELECT t FROM Test t WHERE " +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Test> findByTitleOrDescription(@Param("searchTerm") String searchTerm); // в хэлп

    // Поиск по дате
    @Query(value = "SELECT * FROM tests WHERE DATE(created_at) = :date", nativeQuery = true)
    List<Test> findByDate(@Param("date") LocalDate date);
}