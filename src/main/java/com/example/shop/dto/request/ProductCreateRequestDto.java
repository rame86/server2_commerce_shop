package com.example.shop.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class ProductCreateRequestDto {
    @NotBlank private String category;  // OFFICIAL, UNOFFICIAL, SECONDHAND
    @NotBlank private String title;
    private String description;
    @NotNull @DecimalMin("0") private BigDecimal price;
    @Min(0) private int stockQuantity;
}
