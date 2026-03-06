package com.example.shop.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.shop.entity.Product;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponseDto {
    private UUID productId;
    private String title;
    private String description;
    private BigDecimal price;
    private String imageUrl; 

    // Entity(DB 데이터)를 Dto(화면용 데이터)로 변환하는 핵심 메서드
    public static ProductResponseDto fromEntity(Product product) {
        return ProductResponseDto.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                // [중요] DB의 파일 이름 앞에 웹 경로(/images/)를 붙여서 브라우저가 찾을 수 있게 함
                .imageUrl(product.getImageUrl() != null ? "/images/" + product.getImageUrl() : null)
                .build();
    }
}