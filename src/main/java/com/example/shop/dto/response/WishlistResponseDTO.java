package com.example.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 찜 목록 업데이트 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponseDTO {
    private String status;   // 응답 상태 (예: success, fail)
    private String message;  // 클라이언트에게 보여줄 메시지
}