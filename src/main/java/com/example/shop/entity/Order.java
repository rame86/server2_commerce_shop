package com.example.shop.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders", schema = "shop")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", columnDefinition = "uuid")
    private UUID orderId;

    // 주문자 ID
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    // 배송지 주소
    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    // 수령인 이름
    @Column(name = "recipient_name")
    private String recipientName;

    // 수령인 연락처
    @Column(name = "recipient_phone")
    private String recipientPhone;

    // 주문 총 금액
    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice;

    // 주문 상태 (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    // 배송 추적 번호
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    // 취소 사유
    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    // 주문 항목 리스트
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    // 주문 항목 추가 편의 메서드
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
        // 총 금액 재계산
        this.totalPrice = this.orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 주문 상태 변경 메서드
    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    // 배송 추적 번호 등록 메서드
    public void updateTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
        this.status = OrderStatus.SHIPPING;
    }

    // 주문 취소 메서드
    public void cancel(String cancelReason) {
        this.status = OrderStatus.CANCELLED;
        this.cancelReason = cancelReason;
    }
}