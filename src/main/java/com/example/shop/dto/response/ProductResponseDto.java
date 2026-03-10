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
    private String goodsName;
    private String description;
    private BigDecimal price;
    private String goodsType;
    private String requesterName;
    private String imageUrl;

    public static ProductResponseDto fromEntity(Product product) {
        return ProductResponseDto.builder()
                .productId(product.getProductId())
                .goodsName(product.getGoodsName())
                .description(product.getDescription())
                .price(product.getPrice())
                .goodsType(product.getGoodsType().name())
                .requesterName(product.getRequesterName())
                .imageUrl(product.getImageUrl() != null ? "/images/" + product.getImageUrl() : null)
                .build();
    }
}