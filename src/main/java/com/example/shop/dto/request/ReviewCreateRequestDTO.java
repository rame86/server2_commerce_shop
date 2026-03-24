package com.example.shop.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewCreateRequestDTO {
    private Long productId;
    private Integer rating;
    private String comment;
}
