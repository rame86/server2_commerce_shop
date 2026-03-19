package com.example.shop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.shop.entity.Approval;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ShopResultDTO {

    private Long approvalId;        // 승인요청 ID
    private Long productId;         // ✅ goodsId → productId (DB: product_id BIGINT)
    private Long requesterId;       // 신청자 ID
    private String requesterName;   // 닉네임

    private String goodsName;       // 상품명
    private String goodsType;       // 상품 카테고리
    private String description;     // 상품 설명
    private BigDecimal price;       // ✅ Integer → BigDecimal (DB: NUMERIC(15,2))
    private Integer stockQuantity;  // ✅ stock → stockQuantity (DB: stock_quantity)
    private String color;           // ✅ 신규 (DB: color)
    private String size;            // ✅ 신규 (DB: size)
    private String imageUrl;        // 이미지 URL

    private String status;          // PENDING / CONFIRMED / FAILED
    private String rejectionReason; // 반려 사유

    @JsonFormat(shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;       // 신청 일자

    public static ShopResultDTO fromEntity(Approval approval) {
        return ShopResultDTO.builder()
                .approvalId(approval.getApprovalId())
                .productId(approval.getProductId())
                .requesterId(approval.getRequesterId())
                .requesterName(approval.getRequesterName())
                .goodsName(approval.getGoodsName())
                .goodsType(approval.getGoodsType() != null ? approval.getGoodsType().name() : null)
                .description(approval.getDescription())
                .price(approval.getPrice())
                .stockQuantity(approval.getStockQuantity())
                .color(approval.getColor())
                .size(approval.getSize())
                .imageUrl(approval.getImageUrl())
                .status(approval.getStatus() != null ? approval.getStatus().name() : null)
                .rejectionReason(approval.getRejectionReason())
                .createdAt(approval.getCreatedAt())
                .build();
    }
}