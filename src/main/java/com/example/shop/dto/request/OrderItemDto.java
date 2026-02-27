package com.example.shop.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class OrderItemDto {
    @NotBlank private String productId;
    @Min(1) private int quantity;
}
