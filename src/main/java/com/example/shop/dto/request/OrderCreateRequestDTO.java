package com.example.shop.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

// 주문 생성 시 클라이언트로부터 받는 요청 데이터
@Getter
@Setter
public class OrderCreateRequestDTO {

    // 배송지 주소
    private String shippingAddress;

    // 주문 항목 리스트 (variantId + quantity)
    private List<OrderItemDTO> items;

    // 배송비
    private BigDecimal shippingFee;

    @Column(name = "tracking_number")
    private String trackingNumber;

    // 클라이언트가 보내는 총액
    private java.math.BigDecimal totalAmount;

    
}