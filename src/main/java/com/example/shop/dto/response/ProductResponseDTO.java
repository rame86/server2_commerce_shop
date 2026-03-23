package com.example.shop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.shop.entity.Approval;
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
    private BigDecimal basePrice;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ 기존 상품 엔티티 변환
    public static ProductResponseDTO fromEntity(Product product) {
        String imageUrl = product.getImageUrl();
        // 내부 경로(/images/...)인 경우 외부 접근을 위한 프록시 접두사(/msa/shop) 추가
        if (imageUrl != null && imageUrl.startsWith("/images/")) {
            imageUrl = "/msa/shop" + imageUrl;
        }

        return ProductResponseDTO.builder()
                .productId(product.getProductId())
                .sellerId(product.getSellerId())
                .sellerType(product.getSellerType() != null ? product.getSellerType().name() : null)
                .category(product.getCategory() != null ? product.getCategory().name() : null)
                .title(product.getTitle())
                .description(product.getDescription())
                .imageUrl(imageUrl)
                .basePrice(product.getBasePrice())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    // ✅ 승인 요청 엔티티 변환 (Enum 타입을 String으로 변환 로직 추가)
    public static ProductResponseDTO fromApproval(Approval approval) {
        String imageUrl = approval.getImageUrl();
        if (imageUrl != null && imageUrl.startsWith("/images/")) {
            imageUrl = "/msa/shop" + imageUrl;
        }

        return ProductResponseDTO.builder()
                .productId(null)
                .sellerId(approval.getRequesterId())
                // .name()을 사용하여 Enum -> String 변환 오류 해결
                .sellerType(approval.getGoodsType() != null ? approval.getGoodsType().name() : null)
                .category(approval.getGoodsType() != null ? approval.getGoodsType().name() : null)
                .title(approval.getGoodsName())
                .description(approval.getDescription())
                .imageUrl(imageUrl)
                .basePrice(approval.getPrice())
                .isActive(false)
                .createdAt(approval.getCreatedAt())
                .updatedAt(approval.getUpdatedAt())
                .build();
    }
}