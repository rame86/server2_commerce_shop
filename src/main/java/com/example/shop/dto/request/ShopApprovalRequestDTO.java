package com.example.shop.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ShopApprovalRequestDTO {

    // 수정 요청 시 기존 상품 ID (신규 등록은 null)
    private Long productId; // ✅ goodsId → productId (DB: product_id BIGINT, nullable)

    @NotNull(message = "신청자 ID는 필수입니다.")
    private Long requesterId;

    @NotBlank(message = "신청자 닉네임은 필수입니다.")
    private String requesterName;

    @NotBlank(message = "상품명은 필수입니다.")
    private String goodsName;

    @NotBlank(message = "상품 타입은 필수입니다.")
    private String goodsType;

    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private BigDecimal price; // ✅ Integer → BigDecimal (DB: NUMERIC(15,2))

    @NotNull(message = "재고는 필수입니다.")
    @Min(value = 1, message = "재고는 1 이상이어야 합니다.")
    private Integer stockQuantity; // ✅ stock → stockQuantity (DB: stock_quantity)

    private String color; // ✅ 신규 (DB: color)
    private String size;  // ✅ 신규 (DB: size)

    private String imageUrl;
}