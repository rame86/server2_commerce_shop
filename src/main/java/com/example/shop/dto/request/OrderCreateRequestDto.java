package com.example.shop.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

// 주문 생성 시 클라이언트로부터 받는 요청 데이터
@Getter @Setter
public class OrderCreateRequestDTO {

    // 배송지 주소
    private String shippingAddress;

    // 수령인 이름
    private String recipientName;

    // 수령인 연락처
    private String recipientPhone;

    // 주문 항목 리스트
    private List<OrderItemDTO> items;
}