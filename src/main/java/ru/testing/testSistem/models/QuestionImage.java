package ru.testing.testSistem.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "question_images")
@Data
@NoArgsConstructor
public class QuestionImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imagePath; // путь к файлу изображения

    @OneToOne
    @JoinColumn(name = "question_id")
    private Question question; // принадлежность к вопросу
}