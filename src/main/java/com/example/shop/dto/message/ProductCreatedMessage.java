package com.example.shop.dto.message;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 상품 생성 시 타 서비스(알림, 검색 등)로 전달할 최소 데이터셋
 */
public record ProductCreatedMessage(
    UUID productId,
    String title,
    BigDecimal price,
    String category,
    Long sellerId
) {}