package ru.testing.testSistem.models;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_answers")
@Data
@NoArgsConstructor
public class UserAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    private TestAttempt attempt; // к какой попытке относится

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;      // к какому вопросу

    @ManyToOne
    @JoinColumn(name = "answer_id")
    private Answer answer;      // какой ответ выбран

    @Column(columnDefinition = "TEXT")
    private String userTextAnswer;

    private boolean isCorrect;          // правильно или нет
}