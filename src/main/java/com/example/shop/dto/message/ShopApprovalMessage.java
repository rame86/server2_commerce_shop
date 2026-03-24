package com.example.shop.dto.message;

import java.math.BigDecimal;

public record ShopApprovalMessage(
        Long requesterId,       // 신청자 ID (goodsId 제거 — product_id는 승인 후 자동 생성)
        String requesterName,   // 신청자 닉네임
        String goodsName,       // 상품명
        String goodsType,       // 상품 카테고리 (ProductCategory enum 문자열)
        String description,     // 상품 설명
        BigDecimal price,       // ✅ Integer → BigDecimal (DB: NUMERIC(15,2))
        String color,           // ✅ 신규 (DB: color)
        String size,            // ✅ 신규 (DB: size)
        String itemCategory,    // ✅ 신규 (추가)
        Integer stockQuantity,  // ✅ stock → stockQuantity (DB: stock_quantity)
        String imageUrl         // 이미지 URL
) {}