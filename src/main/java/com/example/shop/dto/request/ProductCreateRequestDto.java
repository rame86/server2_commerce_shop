package com.example.shop.dto.request;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProductCreateRequestDto {
    private String productName;
    private String productDetail; // ← 이게 없어서 에러가 났던 겁니다. 추가하세요!
    private BigDecimal price;
    private String category;
}