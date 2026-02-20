package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShopController {
    
    @GetMapping("/shop")
    public String hello() {
        return "서버가 정상적으로 실행!";
    }
}
