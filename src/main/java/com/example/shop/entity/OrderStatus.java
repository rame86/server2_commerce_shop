package com.example.shop.entity;

public enum OrderStatus {
    PENDING,    // 주문 대기
    CONFIRMED,  // 주문 확정
    SHIPPING,   // 배송 중
    DELIVERED,  // 배송 완료
    CANCELLED   // 주문 취소
}