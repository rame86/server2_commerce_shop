package com.example.shop.dto.request;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProductCreateRequestDto {
    private String goodsName;
    private String description;
    private BigDecimal price;
    private String goodsType;
    private String requesterName;
    private List<VariantDto> variants;

    @Getter @Setter
    public static class VariantDto {
        private String color;
        private String size;
        private BigDecimal additionalPrice;
        private Integer stockQuantity;
        private String skuCode;
    }
}