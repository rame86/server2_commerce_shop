package com.example.shop.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
import lombok.Setter;

@Entity
@Table(name = "orders", schema = "shop")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    // 주문자 ID (Member 서비스 참조)
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    // 배송지 주소
    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    // 배송비
    @Column(name = "shipping_fee", precision = 15, scale = 2)
    private BigDecimal shippingFee;

    // 운송장번호
    @Column(name = "tracking_number")
    private String trackingNumber;

    // 주문 총 금액
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    // ✅ DB ENUM 기준: PENDING / PAID / SHIPPED / COMPLETED / CANCELLED
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // 👈 PostgreSQL의 Custom Enum 타입과 매핑하기 위한 핵심 코드
    @Column(name = "status", nullable = false, columnDefinition = "order_status") // length 속성 제거
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    // 주문 항목 리스트
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    // 주문 항목 추가 + 총 금액 자동 재계산
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
        this.totalAmount = this.orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 주문 상태 변경
    public void updateStatus(OrderStatus status) {
        this.status = status;
    }
}