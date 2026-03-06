package com.example.shop.dto.response;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckoutResponse {
    private String status;
    private Long orderId;
    // DB의 jsonb 컬럼과 매핑될 필드
    private Map<String, Object> paymentDetails; 
}