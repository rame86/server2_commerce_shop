package com.example.shop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.shop.entity.Product;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponseDto {


    private Long productId;
    private Long sellerId;
    private String sellerType;
    private Long requesterId;
    private String goodsType;
    private String goodsName;
    private String category;
    private String title;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private Boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponseDto fromEntity(Product product) {
        return ProductResponseDto.builder()
                .productId(product.getProductId())
                .requesterId(product.getRequesterId())
                .sellerType(product.getSellerType() != null ? product.getSellerType().name() : null)
                .goodsType(product.getGoodsType() != null ? product.getGoodsType().name() : null)
                .goodsName(product.getGoodsName())
                .description(product.getDescription())
                // 이미지 경로 처리
                .imageUrl(product.getImageUrl() != null ? "/images/" + product.getImageUrl() : null)
                .price(product.getPrice())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}