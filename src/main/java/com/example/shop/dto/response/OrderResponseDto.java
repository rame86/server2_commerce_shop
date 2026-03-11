package com.example.shop.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.Product;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponseDto {

    private UUID orderId;
    private Long memberId;
    private String shippingAddress;
    private String recipientName;
    private String recipientPhone;
    private BigDecimal totalPrice;
    private String status;
    private String trackingNumber;
    private String cancelReason;
    private List<OrderItemResponseDto> items;

    public static OrderResponseDto fromEntity(Order order) {
        return OrderResponseDto.builder()
                .orderId(order.getOrderId())
                .memberId(order.getMemberId())
                .shippingAddress(order.getShippingAddress())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .trackingNumber(order.getTrackingNumber())
                .cancelReason(order.getCancelReason())
                .items(order.getOrderItems().stream()
                        .map(OrderItemResponseDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    @Getter
    @Builder
    public static class OrderItemResponseDto {
        private UUID orderItemId;
        private Long productId;
        private String title;       // goodsName → title (DB 기준)
        private Integer quantity;
        private BigDecimal unitPrice;

        public static OrderItemResponseDto fromEntity(OrderItem item) {
            Product product = item.getProduct();
            return OrderItemResponseDto.builder()
                    .orderItemId(item.getOrderItemId())
                    .productId(product.getProductId())
                    .title(product.getTitle())  // getGoodsName() → getTitle()
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .build();
        }
    }
}