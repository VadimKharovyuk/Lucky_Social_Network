package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.Photo;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.PhotoService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/albums/{albumId}/photos")
@RequiredArgsConstructor
public class PhotoController {
    private final PhotoService photoService;
    private final UserService userService;

    // Существующий метод для загрузки одного фото
    @PostMapping("/upload")
    public String uploadPhoto(@PathVariable Long albumId,
                              @RequestParam("photo") MultipartFile file,
                              @RequestParam(required = false) String description,
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();

            // Проверка файла
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Файл пуст");
                return "redirect:/albums/" + albumId;
            }

            // Проверка типа файла
            if (!file.getContentType().startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Можно загружать только изображения");
                return "redirect:/albums/" + albumId;
            }

            // Загрузка фото
            photoService.addPhotoToAlbum(file.getBytes(), description, albumId, currentUser);
            redirectAttributes.addFlashAttribute("success", "Фото успешно загружено");

            return "redirect:/albums/" + albumId;
        } catch (IOException e) {
            log.error("Error uploading photo to album {}: {}", albumId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке фото");
            return "redirect:/albums/" + albumId;
        }
    }

    // Новый метод для множественной загрузки
    @PostMapping("/upload-multiple")
    public String uploadMultiplePhotos(@PathVariable Long albumId,
                                       @RequestParam("photos") List<MultipartFile> files,
                                       RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();

            if (files.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Пожалуйста, выберите фотографии для загрузки");
                return "redirect:/albums/" + albumId;
            }

            List<String> errors = new ArrayList<>();
            List<byte[]> imagesData = new ArrayList<>();

            // Проверяем каждый файл
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                // Проверка типа файла
                if (!file.getContentType().startsWith("image/")) {
                    errors.add("Файл '" + file.getOriginalFilename() + "' не является изображением");
                    continue;
                }

                // Проверка размера файла (например, максимум 5MB)
                if (file.getSize() > 5 * 1024 * 1024) {
                    errors.add("Файл '" + file.getOriginalFilename() + "' превышает допустимый размер 5MB");
                    continue;
                }

                imagesData.add(file.getBytes());
            }

            // Если есть ошибки, возвращаем их
            if (!errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:/albums/" + albumId;
            }

            // Загружаем фотографии
            List<Photo> uploadedPhotos = photoService.addPhotosToAlbum(imagesData, null, albumId, currentUser);

            redirectAttributes.addFlashAttribute("success",
                    "Успешно загружено " + uploadedPhotos.size() + " фотографий");

            return "redirect:/albums/" + albumId;

        } catch (Exception e) {
            log.error("Error uploading multiple photos to album {}: {}", albumId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке фотографий");
            return "redirect:/albums/" + albumId;
        }
    }

    @PostMapping("/{photoId}/delete")
    public String deletePhoto(@PathVariable Long albumId,
                              @PathVariable Long photoId,
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            photoService.removePhotoFromAlbum(photoId, currentUser);
            redirectAttributes.addFlashAttribute("success", "Фотография успешно удалена");
            return "redirect:/albums/" + albumId;
        } catch (Exception e) {
            log.error("Error deleting photo {}: {}", photoId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении фотографии");
            return "redirect:/albums/" + albumId;
        }
    }
}