package com.example.shop.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import java.util.List;

@Getter
public class OrderCreateRequestDto {
    @NotEmpty private List<OrderItemDto> items;
    private String shippingAddress;
}
