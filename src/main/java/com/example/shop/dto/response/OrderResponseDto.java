package com.example.shop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponseDTO {

    private String orderId;
    private Long memberId;
    private String shippingAddress;
    private String recipientName;
    private String recipientPhone;
    private BigDecimal totalPrice;
    private String status;
    private String trackingNumber;
    private String cancelReason;
    private List<OrderItemDto> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class OrderItemDto {
        private String orderItemId;
        private Long productId;
        private String title;
        private String imageUrl;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        public static OrderItemDto fromEntity(OrderItem item) {
            return OrderItemDto.builder()
                    .orderItemId(item.getOrderItemId().toString())
                    .productId(item.getProduct().getProductId())
                    .title(item.getProduct().getTitle())
                    .imageUrl(item.getProduct().getImageUrl() != null ? "/images/" + item.getProduct().getImageUrl() : null)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();
        }
    }

    public static OrderResponseDTO fromEntity(Order order) {
        return OrderResponseDTO.builder()
                .orderId(order.getOrderId().toString())
                .memberId(order.getMemberId())
                .shippingAddress(order.getShippingAddress())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .trackingNumber(order.getTrackingNumber())
                .cancelReason(order.getCancelReason())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemDto::fromEntity)
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}