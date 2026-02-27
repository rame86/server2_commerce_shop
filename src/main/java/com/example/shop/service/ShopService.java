package com.example.shop.service;

import java.util.List;

import com.example.shop.dto.request.OrderCreateRequestDto;
import com.example.shop.dto.request.ProductCreateRequestDto;
import com.example.shop.dto.response.OrderResponseDto;
import com.example.shop.dto.response.ProductResponseDto;

public interface ShopService {
    List<ProductResponseDto> getProducts(String category, Long sellerId, int page, int size);
    ProductResponseDto getProduct(String productId);
    ProductResponseDto createProduct(Long memberId, String role, ProductCreateRequestDto requestDto);
    void deleteProduct(Long memberId, String productId);
    OrderResponseDto createOrder(Long memberId, OrderCreateRequestDto requestDto);
    List<OrderResponseDto> getMyOrders(Long memberId, int page, int size);
    OrderResponseDto getOrder(Long memberId, String orderId);
    OrderResponseDto cancelOrder(Long memberId, String orderId);
}
