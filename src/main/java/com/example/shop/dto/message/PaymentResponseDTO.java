package com.example.shop.dto.message;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 서비스에서 응답으로 보내주는 DTO (PaymentEventProducer.sendDataResponse 기반)
 */
@Getter
@NoArgsConstructor
public class PaymentResponseDTO {

    private String orderId;   // 주문 ID
    private String status;    // COMPLETE, FAIL 등
    private String message;   // 결과 메시지
    private String type;      // 요청 타입 (e.g. PAYMENT)
    private Object payload;   // 부가 데이터 (사용 안 함)
}
