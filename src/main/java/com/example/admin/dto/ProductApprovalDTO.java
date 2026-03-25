// \src\main\java\com\example\admin\dto\ProductApprovalDTO.java 
package com.example.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ProductApprovalDTO {

    // 프론트엔드로 반환할 대기 목록 데이터
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long approvalId;
        private Long productId;
        private String requesterName;
        private String goodsName;
        private String status;
        private boolean isActive;
    }

    // 프론트엔드에서 전달받을 승인/거절 요청 데이터
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long productId;
        // JSON 역직렬화 시 "isApproved" 필드명을 명확히 매핑하도록 어노테이션
        @JsonProperty("isApproved")
        private boolean isApproved; // true: 수락(CONFIRMED), false: 거절(CANCELED)
    }
}