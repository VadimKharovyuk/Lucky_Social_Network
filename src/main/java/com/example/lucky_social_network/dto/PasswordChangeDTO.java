package com.example.lucky_social_network.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDTO {
    @NotEmpty(message = "Введите текущий пароль")
    private String currentPassword;

    @NotEmpty(message = "Введите новый пароль")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$",
            message = "Пароль должен содержать буквы и цифры")
    private String newPassword;

    @NotEmpty(message = "Подтвердите новый пароль")
    private String confirmPassword;
}
