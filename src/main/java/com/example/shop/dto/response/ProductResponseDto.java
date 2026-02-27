package com.example.shop.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProductResponseDto {
    private String productId;
    private Long sellerId;
    private String sellerType;
    private String category;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean isActive;
    private OffsetDateTime createdAt;
}
