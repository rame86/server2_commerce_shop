package com.example.shop.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class Product {
    private UUID productId;      // DB의 UUID 타입
    private UUID sellerId;
    private String sellerType;   // DB의 ENUM은 일단 String으로 받으면 편합니다.
    private String category;
    private String title;
    private String description;
    private BigDecimal price;    // DB의 NUMERIC 타입
    private int stockQuantity;
    private boolean isActive;
    private OffsetDateTime createdAt;
}