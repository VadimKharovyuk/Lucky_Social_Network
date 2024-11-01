package com.example.lucky_social_network.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LudaController {

    @GetMapping("/luda")
    public String showLudaPage() {
        return "ludaV1";
    }
}
