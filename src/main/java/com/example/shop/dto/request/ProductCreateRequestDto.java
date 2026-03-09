package com.example.shop.dto.request;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

// 클라이언트로부터 상품 생성 시 받는 요청 데이터 DTO
@Getter @Setter
public class ProductCreateRequestDto {
    private String productName; // Product의 title에 매핑됨
    private String productDetail; // 에러가 났던 부분 (Product의 description에 매핑됨)
    private BigDecimal price; // Product의 basePrice에 매핑됨
    private String category; // ProductCategory 설정용
    
    // 추가됨: 상품 등록 시 함께 받을 하위 옵션(Variant) 리스트
    private List<VariantDto> variants;

    // 상품 상세 옵션 정보를 담는 내부 정적 클래스
    @Getter @Setter
    public static class VariantDto {
        private String color; // 옵션 색상
        private String size; // 옵션 사이즈
        private BigDecimal additionalPrice; // 기본가 외 추가 금액
        private Integer stockQuantity; // 해당 옵션의 재고
        private String skuCode; // 자체 재고 식별 코드
    }
}