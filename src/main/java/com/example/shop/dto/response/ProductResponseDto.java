package com.example.shop.dto.response;

import java.math.BigDecimal;

import com.example.shop.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@Builder // 이 어노테이션이 있어야 .productName() 같은 빌더 메서드가 생성됩니다.
@NoArgsConstructor 
@AllArgsConstructor
public class ProductResponseDto {
    private String productId;
    private String productName;  // ← 서비스에서 찾고 있는 이름
    private String productDetail; // ← 서비스에서 찾고 있는 이름
    private BigDecimal price;

    // 서비스에서 map(ProductResponseDto::fromEntity)를 쓸 수 있게 추가
    public static ProductResponseDto fromEntity(Product p) {
        return ProductResponseDto.builder()
                .productId(p.getProductId().toString())
                .productName(p.getTitle())       // 엔티티의 title을 DTO의 productName으로 매핑
                .productDetail(p.getDescription()) // 엔티티의 description을 DTO의 productDetail로 매핑
                .price(p.getPrice())
                .build();
    }
}