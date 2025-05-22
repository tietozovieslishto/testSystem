package ru.testing.testSistem.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class AnswerForm {
    @NotBlank(message = "Текст ответа обязателен")
    private String text;

    @Getter
    @Setter
    private boolean correct;


}