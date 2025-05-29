package ru.testing.testSistem.services;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.testing.testSistem.models.Test;
import ru.testing.testSistem.models.TestAttempt;
import ru.testing.testSistem.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final Environment env; // доступ к application properties

    public void sendVerificationEmail(User user, String token) {
        try {
            String subject = "Подтверждение регистрации";
            String confirmationUrl = env.getProperty("app.base-url", "http://localhost:8080") + "/auth/verify?token=" + token;
            String message = "Для завершения регистрации перейдите по ссылке: " + confirmationUrl;

            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(user.getEmail());
            email.setSubject(subject);
            email.setText(message);
            email.setFrom(env.getProperty("spring.mail.username"));

            mailSender.send(email);
            logger.info("Письмо с подтверждением отправлено на: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Ошибка при отправке письма с подтверждением", e);
            throw new RuntimeException("Не удалось отправить письмо с подтверждением");
        }
    }
    public void sendTestResultsEmail(User user, TestAttempt attempt) {
        try {
            String subject = "Результаты теста: " + attempt.getTest().getTitle();

            //  текст письма с результатами
            // %s - строка
            // %d - число
            String message = String.format(
                    "Здравствуйте, %s!\n\n" +
                            "Вы завершили тест: %s\n" +
                            "Ваш результат: %d/%d баллов\n\n" +
                            "Спасибо за участие!",
                    user.getUsername(),
                    attempt.getTest().getTitle(),
                    attempt.getScore(),
                    attempt.getTest().getQuestions().size()
            );

            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(user.getEmail());
            email.setSubject(subject);
            email.setText(message);
            email.setFrom(env.getProperty("spring.mail.username"));

            mailSender.send(email);
            logger.info("Письмо с результатами теста отправлено на: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Ошибка при отправке письма с результатами теста", e);
            throw new RuntimeException("Не удалось отправить письмо с результатами теста");
        }
    }
    public void sendTestInvitation(User sender, String recipientEmail, Test test, String customMessage) {
        try {
            String subject = "Приглашение на тест: " + test.getTitle();

            String message = String.format(
                    "Здравствуйте!\n\n" +
                            "Вы получили приглашение на тест от %s.\n" +
                            "Название теста: %s\n" +
                            "Описание: %s\n\n" +
                            "%s\n\n" +
                            "Для прохождения теста перейдите по ссылке: %s/tests/%d\n\n" +
                            "С уважением,\nКоманда тестирования",
                    sender.getUsername(),
                    test.getTitle(),
                    test.getDescription(),
                    customMessage != null && !customMessage.isEmpty() ? "Сообщение от отправителя:\n" + customMessage : "",
                    env.getProperty("app.base-url", "http://localhost:8080"),
                    test.getId()
            );

            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(recipientEmail);
            email.setSubject(subject);
            email.setText(message);
            email.setFrom(env.getProperty("spring.mail.username"));

            mailSender.send(email);
            logger.info("Приглашение на тест отправлено на: {}", recipientEmail);
        } catch (Exception e) {
            logger.error("Ошибка при отправке приглашения на тест", e);
            throw new RuntimeException("Не удалось отправить приглашение на тест");
        }
    }
}