package com.example.shop.dto.request;

import java.util.List;

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

    // ✅ totalamount → totalAmount (오타 수정)
    // ✅ 클라이언트가 보내는 총액 (서비스에서 재계산하므로 검증용으로만 사용)
    private java.math.BigDecimal totalAmount;

    // ※ recipientName, recipientPhone 제거
    //    Order 엔티티에 해당 컬럼 없음 — 필요 시 shippingAddress에 포함하거나 별도 컬럼 추가
}