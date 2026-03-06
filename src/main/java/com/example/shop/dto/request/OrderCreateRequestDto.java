package com.example.shop.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class OrderCreateRequestDto {
    @NotEmpty private List<OrderItemDto> items;
    private String shippingAddress;
}


