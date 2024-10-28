package com.example.lucky_social_network.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/blog")
public class Blog {
    @GetMapping
    public String blog() {
        return "blog/BlogPageTemplate";

    }

}
