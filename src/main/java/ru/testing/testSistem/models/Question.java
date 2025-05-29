package ru.testing.testSistem.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;
    private Integer points;

    @ManyToOne
    @JoinColumn(name = "test_id")
    private Test test; // принадлежность к тесту

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true) // в help
    private List<Answer> answers = new ArrayList<>();  // варианты ответа

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private QuestionImage image;

}