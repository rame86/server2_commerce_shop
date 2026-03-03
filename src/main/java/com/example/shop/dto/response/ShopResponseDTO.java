package com.example.shop.dto.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder // 이 애노테이션이 있어야 .builder() 호출이 가능합니다.
@NoArgsConstructor // MyBatis나 Jackson 직렬화에 필요
@AllArgsConstructor // Builder를 사용하기 위해 모든 필드를 인자로 받는 생성자 필요
public class ShopResponseDTO {
    private Long productId;
    private String productName;
    // DB의 JSONB 컬럼과 매핑될 필드
    private Map<String, Object> productDetail; 
}