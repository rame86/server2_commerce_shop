package com.example.shop.entity;

public enum OrderStatus {
    PENDING,    // 주문 대기
    PAID,       // ✅ 결제 완료 (DB: PAID)
    SHIPPED,    // ✅ 배송 중   (DB: SHIPPED)
    COMPLETED,  // ✅ 배송 완료 (DB: COMPLETED)
    CANCELLED   // 주문 취소
}