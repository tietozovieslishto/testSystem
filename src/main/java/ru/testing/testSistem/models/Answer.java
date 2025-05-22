package ru.testing.testSistem.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answers")
@Data
@NoArgsConstructor
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text; // текст ответа
    @Column(name = "is_correct")
    private boolean correct;
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;  // принадлежность к вопросу
}