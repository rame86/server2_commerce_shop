package com.example.shop.dto.request;

import lombok.Getter;
import lombok.Setter;

// 주문 항목 데이터
@Getter @Setter
public class OrderItemDto {

    // 주문할 상품 UUID
    private Long productId;

    // 주문 수량
    private Integer quantity;
}