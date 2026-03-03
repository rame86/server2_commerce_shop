package com.example.shop.controller;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Update("/updateOffical")
    public String updateOffical() {
        return "공식굿즈 등록";
    }

    @Update("/updateSecondhand")
    public String updateSecondhand() {
        return "중고굿즈 등록";
    }

    @Update("/updateFanmade")
    public String updateFanmade() {
        return "팬메이드굿즈 등록";
    }

    @Delete("/deleteOffical")
    public String deleteOffical() {
        return "공식굿즈 삭제";
    }

    @Delete("/deleteSecondhand")
    public String deleteSecondhand() {
        return "중고굿즈 삭제";
    }

    @Delete("/deleteFanmade")
    public String deleteFanmade() {
        return "팬메이드굿즈 삭제";
    }

}
