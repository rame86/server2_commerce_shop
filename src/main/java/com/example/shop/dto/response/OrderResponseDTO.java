package com.example.shop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponseDTO {

    private String orderId;
    private Long memberId;
    private String shippingAddress;
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemDto> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class OrderItemDto {
        private String orderItemId;
        private String variantId;       // ✅ productId → variantId (DB: variant_id UUID)
        private String color;           // ✅ variant 정보 추가
        private String size;            // ✅ variant 정보 추가
        private String title;           // variant → product.title 조인
        private String imageUrl;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        public static OrderItemDto fromEntity(OrderItem item) {
            BigDecimal unitPrice = item.getUnitPrice(); // ✅ getPrice() → getUnitPrice()
            
            String imageUrl = item.getVariant().getProduct().getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                int lastSlash = Math.max(imageUrl.lastIndexOf("/"), imageUrl.lastIndexOf("\\"));
                String fileName = (lastSlash != -1) ? imageUrl.substring(lastSlash + 1) : imageUrl;
                imageUrl = "/images/" + fileName;
            }

            return OrderItemDto.builder()
                    .orderItemId(item.getOrderItemId().toString())
                    .variantId(item.getVariant().getVariantId().toString()) // ✅ variant FK 기준
                    .color(item.getVariant().getColor())
                    .size(item.getVariant().getSize())
                    .title(item.getVariant().getProduct().getTitle()) // ✅ variant→product 조인
                    .imageUrl(imageUrl)
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();
        }
    }

    public static OrderResponseDTO fromEntity(Order order) {
        return OrderResponseDTO.builder()
                .orderId(order.getOrderId().toString())
                .memberId(order.getMemberId())
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemDto::fromEntity)
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}