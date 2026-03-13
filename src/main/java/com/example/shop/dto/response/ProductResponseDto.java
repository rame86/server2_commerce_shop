package com.example.shop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.shop.entity.Product;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponseDTO {

    private Long productId;
    private Long sellerId;
    private String sellerType;
    private String category;    // DB: category (product_category enum)
    private String title;       // DB: title
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponseDTO fromEntity(Product product) {
        return ProductResponseDTO.builder()
                .productId(product.getProductId())
                .sellerId(product.getSellerId())
                .sellerType(product.getSellerType() != null ? product.getSellerType().name() : null)
                .category(product.getCategory() != null ? product.getCategory().name() : null)
                .title(product.getTitle())
                .description(product.getDescription())
                // .imageUrl(product.getImageUrl() != null ? "/images/" + product.getImageUrl() : null) //배포시 해당 내용으로 수정
                .imageUrl(product.getImageUrl() != null ? "" + product.getImageUrl() : null)
                .price(product.getPrice())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}