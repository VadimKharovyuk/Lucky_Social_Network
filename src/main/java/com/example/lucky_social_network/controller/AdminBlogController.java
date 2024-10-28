package com.example.lucky_social_network.controller;


import com.example.lucky_social_network.dto.BlogDTO;
import com.example.lucky_social_network.model.BlogPost;
import com.example.lucky_social_network.service.BlogService;
import com.example.lucky_social_network.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

//
//@Controller
//@RequestMapping("/admin/blog")  // Базовый путь для админского контроллера
//@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
//@RequiredArgsConstructor
//public class AdminBlogController {
//    private final BlogService blogService;
//    private final UserService userService;
//
//    // Список всех блогов для админа
//    @GetMapping
//    public String blogManagement(Model model) {
//        model.addAttribute("blogs", blogService.getAllBlogs(null, Pageable.unpaged()));
//        model.addAttribute("blogTypes", BlogPost.BlogType.values());
//        return "admin/blog/management";
//    }
//
//    // Форма создания
//    @GetMapping("/create")
//    public String showCreateForm(Model model) {
//        model.addAttribute("blog", new BlogDTO());
//        model.addAttribute("blogTypes", BlogPost.BlogType.values());
//        return "admin/blog/create";
//    }
//
//    // Сохранение нового блога
//    @PostMapping("/create")
//    public String createBlog(
//            @Valid @ModelAttribute BlogDTO blogDTO,
//            BindingResult bindingResult,
//            @RequestParam(required = false) List<MultipartFile> images,
//            Principal principal,
//            Model model
//    ) {
//        if (bindingResult.hasErrors()) {
//            model.addAttribute("blogTypes", BlogPost.BlogType.values());
//            return "admin/blog/create";
//        }
//        Long authorId = userService.findByUsername(principal.getName()).getId();
//        blogService.createBlog(blogDTO, images, authorId);
//        return "redirect:/admin/blog";
//    }
//
//    // Форма редактирования
//    @GetMapping("/edit/{id}")
//    public String showEditForm(@PathVariable Long id, Model model) {
//        model.addAttribute("blog", blogService.getBlogById(id));
//        model.addAttribute("blogTypes", BlogPost.BlogType.values());
//        return "admin/blog/edit";
//    }
//
//    // Обновление блога
//    @PostMapping("/edit/{id}")
//    public String updateBlog(
//            @PathVariable Long id,
//            @Valid @ModelAttribute BlogDTO blogDTO,
//            BindingResult bindingResult,
//            @RequestParam(required = false) List<MultipartFile> newImages,
//            Model model
//    ) {
//        if (bindingResult.hasErrors()) {
//            model.addAttribute("blogTypes", BlogPost.BlogType.values());
//            return "admin/blog/edit";
//        }
//        blogService.updateBlog(id, blogDTO, newImages);
//        return "redirect:/admin/blog";
//    }
//
//    // Публикация блога
//    @PostMapping("/{id}/publish")
//    public String publishBlog(@PathVariable Long id) {
//        blogService.publishBlog(id);
//        return "redirect:/admin/blog";
//    }
//
//    // Снятие с публикации
//    @PostMapping("/{id}/unpublish")
//    public String unpublishBlog(@PathVariable Long id) {
//        blogService.unpublishBlog(id);
//        return "redirect:/admin/blog";
//    }
//
//    // Удаление блога
//    @PostMapping("/delete/{id}")
//    public String deleteBlog(@PathVariable Long id) {
//        blogService.deleteBlog(id);
//        return "redirect:/admin/blog";
//    }
//}
@Controller
@RequestMapping("/admin/blog")  // Базовый путь для админского контроллера
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminBlogController {
    private final BlogService blogService;
    private final UserService userService;

    // Список всех блогов для админа
    @GetMapping
    public String blogManagement(Model model) {
        model.addAttribute("blogs", blogService.getAllBlogs(null, Pageable.unpaged()));
        model.addAttribute("blogTypes", BlogPost.BlogType.values());
        return "admin/blog/management";
    }

    // Форма создания
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("blog", new BlogDTO());
        model.addAttribute("blogTypes", BlogPost.BlogType.values());
        return "admin/blog/create";
    }

    // Сохранение нового блога
    @PostMapping("/create")
    public String createBlog(
            @Valid @ModelAttribute BlogDTO blogDTO,
            BindingResult bindingResult,
            @RequestParam(required = false) List<MultipartFile> images,
            Principal principal,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("blogTypes", BlogPost.BlogType.values());
            return "admin/blog/create";
        }
        Long authorId = userService.findByUsername(principal.getName()).getId();
        blogService.createBlog(blogDTO, images, authorId);
        return "redirect:/admin/blog";
    }

    // Форма редактирования
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("blog", blogService.getBlogById(id));
        model.addAttribute("blogTypes", BlogPost.BlogType.values());
        return "admin/blog/edit";
    }

    // Обновление блога
    @PostMapping("/edit/{id}")
    public String updateBlog(
            @PathVariable Long id,
            @Valid @ModelAttribute BlogDTO blogDTO,
            BindingResult bindingResult,
            @RequestParam(required = false) List<MultipartFile> newImages,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("blogTypes", BlogPost.BlogType.values());
            return "admin/blog/edit";
        }
        blogService.updateBlog(id, blogDTO, newImages);
        return "redirect:/admin/blog";
    }

    // Публикация блога
    @PostMapping("/{id}/publish")
    public String publishBlog(@PathVariable Long id) {
        blogService.publishBlog(id);
        return "redirect:/admin/blog";
    }

    // Снятие с публикации
    @PostMapping("/{id}/unpublish")
    public String unpublishBlog(@PathVariable Long id) {
        blogService.unpublishBlog(id);
        return "redirect:/admin/blog";
    }

    // Удаление блога
    @PostMapping("/delete/{id}")
    public String deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return "redirect:/admin/blog";
    }

}
