package com.example.shop.dto.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CheckoutRequestDTO {
    private Long productId;
    private Integer quantity;
    private BigDecimal usePoint;
}
