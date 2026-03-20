package com.example.shop.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDTO {

    // ✅ productId → variantId (DB: order_items.variant_id UUID)
    private String variantId;

    // 주문 수량
    private Integer quantity;
}