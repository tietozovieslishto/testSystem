package ru.testing.testSistem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.testing.testSistem.models.QuestionImage;
//

public interface QuestionImageRepository extends JpaRepository<QuestionImage, Long> {

}