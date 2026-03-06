package com.example.shop.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderItemResponseDto {
    private String orderItemId;
    private String productId;
    private String productTitle;
    private Integer quantity;
    private BigDecimal unitPrice;  // 주문 당시 가격 스냅샷
}
