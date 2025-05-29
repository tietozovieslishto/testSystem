package ru.testing.testSistem.DTO;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Setter;


@Data
public class RegistrationDto {

    @Setter
    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Имя пользователя может содержать только буквы, цифры и подчеркивание")// ^[a-zA-Z0-9_]+$ разрешает:
//Латинские буквы (a-z, A-Z)
    //Цифры (0-9) и подчеркивания

    private String username;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Пароль должен содержать хотя бы одну цифру, одну заглавную букву, одну строчную букву и один специальный символ"
    )
    private String password;



    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }



}