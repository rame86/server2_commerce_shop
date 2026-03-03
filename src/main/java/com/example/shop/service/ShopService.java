package com.example.shop.service;

import java.util.List;

import com.example.shop.dto.request.OrderCreateRequestDto;
import com.example.shop.dto.request.ProductCreateRequestDto;
import com.example.shop.dto.response.OrderResponseDto;
import com.example.shop.dto.response.ProductResponseDto;

public interface ShopService {

    // 상품 목록 조회 (필터링 및 페이징)
    List<ProductResponseDto> getProducts(String category, Long sellerId, int page, int size);

    // 상품 단건 상세 조회
    ProductResponseDto getProduct(String productId);

    // 신규 상품 등록
    ProductResponseDto createProduct(Long memberId, String role, ProductCreateRequestDto requestDto);

    // 상품 삭제 (논리 삭제 처리)
    void deleteProduct(Long memberId, String productId);

    // 주문 생성 및 결제 처리
    OrderResponseDto createOrder(Long memberId, OrderCreateRequestDto requestDto);

    // 로그인한 사용자의 주문 내역 조회
    List<OrderResponseDto> getMyOrders(Long memberId, int page, int size);

    // 주문 상세 내역 조회
    OrderResponseDto getOrder(Long memberId, String orderId);

    // 주문 취소
    OrderResponseDto cancelOrder(Long memberId, String orderId);
}