package ru.testing.testSistem.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
//
@Data
public class TestForm {
    @NotBlank(message = "Название теста обязательно")
    private String title;

    private String description;
    private boolean privateTest;
    @Min(value = 1, message = "Лимит времени должен быть не менее 1 минуты")
    private Integer timeLimitMinutes = 30;
}
