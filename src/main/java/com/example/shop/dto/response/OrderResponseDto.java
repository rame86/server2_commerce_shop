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

// 주문 응답 데이터 DTO
@Getter
@Builder
public class OrderResponseDto {

    private UUID orderId; // 주문 ID
    private Long memberId; // 주문자 ID
    private String shippingAddress; // 배송지 주소
    private String recipientName; // 수령인 이름
    private String recipientPhone; // 수령인 연락처
    private BigDecimal totalPrice; // 주문 총 금액
    private String status; // 주문 상태
    private String trackingNumber; // 배송 추적 번호
    private String cancelReason; // 취소 사유
    private List<OrderItemResponseDto> items; // 주문 항목 리스트

    // Order 엔티티를 OrderResponseDto로 변환
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

    // 주문 항목 응답 내부 DTO
    @Getter
    @Builder
    public static class OrderItemResponseDto {
        private UUID orderItemId; // 주문 항목 ID
        private Long productId; // 상품 ID
        private String goodsName; // 상품명
        private Integer quantity; // 수량
        private BigDecimal unitPrice; // 단가

        // OrderItem 엔티티를 OrderItemResponseDto로 변환
        public static OrderItemResponseDto fromEntity(OrderItem item) {
            Product product = item.getProduct();
            
            return OrderItemResponseDto.builder()
                    .orderItemId(item.getOrderItemId())
                    .productId(product.getProductId()) 
                    .goodsName(product.getGoodsName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .build();
        }
    }
}