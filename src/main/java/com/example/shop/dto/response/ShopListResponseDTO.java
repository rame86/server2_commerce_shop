package com.example.shop.dto.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopListResponseDTO {
    private List<Map<String, Object>> items; // 상품 목록
    private int total;                       // 총 개수
}