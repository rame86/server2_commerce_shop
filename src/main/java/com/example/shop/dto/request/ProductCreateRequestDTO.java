package com.example.shop.dto.request;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCreateRequestDTO {
    private String goodsName;
    private String description;
    private BigDecimal price;
    private String goodsType;
    private String requesterName;
    private List<VariantDTO> variants;

    @Getter
    @Setter
    public static class VariantDTO {
        private String color;
        private String size;
        private BigDecimal additionalPrice;
        private Integer stockQuantity;
        private String skuCode;
    }
}