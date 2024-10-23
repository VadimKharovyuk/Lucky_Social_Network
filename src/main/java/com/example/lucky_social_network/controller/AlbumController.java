package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.Album;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.AlbumService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/albums")
@RequiredArgsConstructor
public class AlbumController {
    private final AlbumService albumService;
    private final UserService userService;


    @GetMapping
    public String getAllAlbums(Pageable pageable, Model model) {
        User currentUser = userService.getCurrentUser();
        Page<Album> albums = albumService.getAllUserAlbums(currentUser.getId(), currentUser, pageable);
        model.addAttribute("albums", albums);
        return "albums/list";
    }

    @GetMapping("/create")
    public String showCreateAlbumForm(Model model) {
        try {
            // Проверяем, что пользователь аутентифицирован
            userService.getCurrentUser();
            model.addAttribute("album", new Album());
            return "albums/create";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @PostMapping("/create")
    public String createAlbum(@RequestParam String title,
                              @RequestParam(required = false) String description,
                              @RequestParam(defaultValue = "false") Boolean isPrivate,
                              Model model) {
        try {
            User currentUser = userService.getCurrentUser();

            // Базовая валидация
            if (title == null || title.trim().isEmpty()) {
                model.addAttribute("error", "Название альбома не может быть пустым");
                return "albums/create";
            }

            // Создание альбома
            Album album = albumService.createAlbum(title, description, isPrivate, currentUser);

            // Логирование успешного создания
            log.info("Album created: {}, user: {}", album.getId(), currentUser.getId());

            return "redirect:/albums/" + album.getId();
        } catch (Exception e) {
            // Логирование ошибки
            log.error("Error creating album: {}", e.getMessage(), e);

            model.addAttribute("error", "Ошибка при создании альбома: " + e.getMessage());
            return "albums/create";
        }
    }

    @GetMapping("/{albumId}")
    public String getAlbum(@PathVariable Long albumId,
                           Model model) {
        User currentUser = userService.getCurrentUser();
        Album album = albumService.getAlbumById(albumId, currentUser);
        model.addAttribute("album", album);
        model.addAttribute("isOwner", albumService.isAlbumOwner(albumId, currentUser));
        return "albums/view";
    }

    @PostMapping("/{albumId}/delete")
    public String deleteAlbum(@PathVariable Long albumId) {
        User currentUser = userService.getCurrentUser();
        albumService.deleteAlbum(albumId, currentUser);
        return "redirect:/albums";
    }

    
}