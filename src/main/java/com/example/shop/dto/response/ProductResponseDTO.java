package com.example.shop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.shop.entity.Approval;
import com.example.shop.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {

    private Long productId;
    private Long sellerId;
    private Long artistId;
    private String sellerType;
    private String category;
    private String title;
    private String description;
    private String imageUrl;
    private Double averageRating;
    private Long reviewCount;
    private BigDecimal basePrice;
    private Boolean isActive;
    private Integer stockQuantity;   // variant 재고 합산
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ 기존 상품 엔티티 변환
    public static ProductResponseDTO fromEntity(Product product) {
        String imageUrl = product.getImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // 1. 슬래시(/)와 백슬래시(\) 중 마지막 구분자의 위치를 찾습니다.
            int lastSlash = Math.max(imageUrl.lastIndexOf("/"), imageUrl.lastIndexOf("\\"));

            // 2. 구분자가 있다면 파일명만 추출하고, 없다면 전체를 파일명으로 간주합니다.
            String fileName = (lastSlash != -1) ? imageUrl.substring(lastSlash + 1) : imageUrl;

            // 3. 브라우저가 접근 가능한 웹 경로(/images/shop/...)로 강제 변환합니다.
            imageUrl = "/images/shop/" + fileName;
        }

        return ProductResponseDTO.builder()
                .productId(product.getProductId())
                .artistId(product.getArtistId())
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

        return ProductResponseDTO.builder()
                .productId(null)
                .sellerId(approval.getRequesterId())
                .artistId(approval.getArtistId())
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