package com.example.lucky_social_network.valid;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

// Валидатор для проверки файлов
@Component
public class FileValidator implements Validator {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif"
    );

    @Override
    public boolean supports(Class<?> clazz) {
        return MultipartFile.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MultipartFile file = (MultipartFile) target;

        // Проверка размера файла
        if (file.getSize() > MAX_FILE_SIZE) {
            errors.rejectValue("images", "file.too.large",
                    "Файл слишком большой. Максимальный размер: 5MB");
        }

        // Проверка типа файла
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            errors.rejectValue("images", "file.invalid.type",
                    "Недопустимый тип файла. Разрешены только JPEG, PNG и GIF");
        }
    }
}
