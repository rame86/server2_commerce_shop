package com.example.shop.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    private String color;
    private String size;
    private String itemCategory;
    
    @NotNull(message = "아티스트를 선택해주세요.")
    private Long artistId;

    @Min(value = 0, message = "수량은 0 이상이어야 합니다.")
    private Integer stockQuantity;
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