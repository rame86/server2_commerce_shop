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
    private String category;
    private String title;
    private String description;
    private String imageUrl;
    private BigDecimal basePrice;   // ✅ price → basePrice (DB: base_price)
    private Boolean isActive;       // ✅ status(String) → isActive(Boolean) (DB: is_active)
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
                .imageUrl(product.getImageUrl() != null ? product.getImageUrl() : null)
                .basePrice(product.getBasePrice())      // ✅ getPrice() → getBasePrice()
                .isActive(product.getIsActive())        // ✅ getStatus() → getIsActive()
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}