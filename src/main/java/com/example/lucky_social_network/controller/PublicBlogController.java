package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.BlogDTO;
import com.example.lucky_social_network.model.BlogPost;
import com.example.lucky_social_network.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/news")
@RequiredArgsConstructor
public class PublicBlogController {

    private final BlogService blogService;

    @GetMapping
    public String listBlogs(
            @RequestParam(required = false) BlogPost.BlogType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // Используем метод для получения только опубликованных постов
        Page<BlogDTO> blogs = blogService.getPublishedBlogs(type, pageable);

        model.addAttribute("blogs", blogs);
        model.addAttribute("blogTypes", BlogPost.BlogType.values());
        model.addAttribute("currentType", type);

        return "blog/BlogPageTemplate";
    }

    @GetMapping("/{id}")
    public String viewBlog(@PathVariable Long id, Model model) {
        BlogDTO blog = blogService.getBlogById(id);
        blogService.incrementViewCount(id);

        model.addAttribute("blog", blog);
        model.addAttribute("recentBlogs", blogService.getLatestBlogs(5));

        return "blog/view";
    }
}
