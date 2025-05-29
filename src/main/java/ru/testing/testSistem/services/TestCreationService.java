package ru.testing.testSistem.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.testing.testSistem.DTO.AnswerForm;
import ru.testing.testSistem.DTO.QuestionForm;
import ru.testing.testSistem.DTO.TestForm;
import ru.testing.testSistem.models.*;
import ru.testing.testSistem.repo.AnswerRepository;
import ru.testing.testSistem.repo.QuestionImageRepository;
import ru.testing.testSistem.repo.QuestionRepository;
import ru.testing.testSistem.repo.TestRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TestCreationService {
    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionImageRepository questionImageRepository;
    private final FileStorageService fileStorageService;

    public Test createTest(TestForm form, User creator) {
        Test test = new Test();
        test.setTitle(form.getTitle());
        test.setDescription(form.getDescription());
        test.setPrivate(form.isPrivateTest());
        test.setTimeLimitMinutes(form.getTimeLimitMinutes());
        test.setAuthor(creator);
        test.setActive(true);

        return testRepository.save(test);
    }

    public Question addQuestionToTest(Long testId, QuestionForm form, MultipartFile image) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found"));
        Question question = new Question();
        question.setTest(test);
        question.setText(form.getText());
        question.setQuestionType(form.getType());
        question.setPoints(form.getPoints() != null ? form.getPoints() : 1);

        Question savedQuestion = questionRepository.save(question);
        if (image != null && !image.isEmpty()) {
            saveQuestionImage(savedQuestion, image);
        }

        return savedQuestion;
    }

    private void saveQuestionImage(Question question, MultipartFile image) {
        try {
            String filename = "question_" + question.getId() + "_" + System.currentTimeMillis() +
                    "." + StringUtils.getFilenameExtension(image.getOriginalFilename()); // последнее это расширение
            fileStorageService.store(image, filename);
            QuestionImage questionImage = new QuestionImage();
            questionImage.setQuestion(question);
            questionImage.setImagePath("/uploads/" + filename);

            questionImageRepository.save(questionImage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store image for question", e);
        }
    }
    public Answer addAnswerToQuestion(Long questionId, AnswerForm form) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setText(form.getText());
        answer.setCorrect(form.isCorrect());

        return answerRepository.save(answer);
    }
    public List<Question> getTestQuestions(Long testId) {
        return questionRepository.findByTestId(testId);
    }

}