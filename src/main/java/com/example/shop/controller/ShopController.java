package com.example.shop.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.shop.model.Product;
import com.example.shop.service.ShopService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;

    @GetMapping("/products")
    public List<Product> list() {
        return shopService.getActiveProducts();
    }

    @PostMapping("/products")
    public String register(@RequestBody Product product) {
        shopService.addProduct(product);
        return "OK";
    }
}
