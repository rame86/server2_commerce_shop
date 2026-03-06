package com.example.shop.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductDetailResponseDto {
    private Long productId;
    private String name;
    private int price;
    private String description;
    // 필요한 데이터 필드 추가
}