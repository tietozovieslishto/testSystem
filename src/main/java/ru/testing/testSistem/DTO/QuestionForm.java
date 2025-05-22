package ru.testing.testSistem.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import ru.testing.testSistem.models.QuestionType;

@Data
public class QuestionForm {
    @NotBlank(message = "Текст вопроса обязателен")
    private String text;

    @NotNull(message = "Тип вопроса обязателен")
    private QuestionType type;

    @Min(1)
    private Integer points = 1;

    private MultipartFile image;
}
