package com.example.shop.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    @GetMapping("/")
    public String product() {
        return "굿즈 전체목록 받음";
    }

    @GetMapping("/official")
    public String officialMerchandise() {
        return "공식굿즈 목록 받음";
    }

    @GetMapping("/secondhand")
    public String secondhand() {
        return "중고굿즈 목록 받음";
    }

    @GetMapping("/fanmade")
    public String fanmade() {
        return "팬메이드굿즈 목록 받음";
    }

    @PostMapping("/updateOffical")
    public String updateOffical() {
        return "공식굿즈 등록";
    }

    @PostMapping("/updateSecondhand")
    public String updateSecondhand() {
        return "중고굿즈 등록";
    }

    @PostMapping("/updateFanmade")
    public String updateFanmade() {
        return "팬메이드굿즈 등록";
    }

    @DeleteMapping("/deleteOffical")
    public String deleteOffical() {
        return "공식굿즈 삭제";
    }

    @DeleteMapping("/deleteSecondhand")
    public String deleteSecondhand() {
        return "중고굿즈 삭제";
    }

    @DeleteMapping("/deleteFanmade")
    public String deleteFanmade() {
        return "팬메이드굿즈 삭제";
    }

}
