package com.dita.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminHeaderController {

    @GetMapping("/adminHeader")
    public String adminHeaderPage() {
        return "admin/adminHeader"; 
    }
}