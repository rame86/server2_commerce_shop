package com.example.shop.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.shop.dto.request.OrderCreateRequestDto;
import com.example.shop.dto.request.ProductCreateRequestDto;
import com.example.shop.dto.response.OrderResponseDto;
import com.example.shop.dto.response.ProductResponseDto;

// 상점 도메인의 주요 비즈니스 로직을 정의하는 서비스 인터페이스
public interface ShopService {
    // 상품 목록 조회
    List<ProductResponseDto> getProducts(String category, Long sellerId, int page, int size);
    
    // 이미지 파일을 포함하여 상품 및 상세 옵션(Variants)을 함께 등록하는 통합 메서드
    ProductResponseDto createProduct(Long memberId, String role, ProductCreateRequestDto requestDto, MultipartFile imageFile);

    // 단일 상품 상세 조회
    ProductResponseDto getProduct(String productId);
    
    // 상품 삭제 로직 (실제 DB 삭제가 아닌 비활성화 처리용)
    void deleteProduct(Long memberId, String productId);
    
    // 이하 주문 관련 메서드 (기존 원본 유지)
    OrderResponseDto createOrder(Long memberId, OrderCreateRequestDto requestDto);
    List<OrderResponseDto> getMyOrders(Long memberId, int page, int size);
    OrderResponseDto getOrder(Long memberId, String orderId);
    OrderResponseDto cancelOrder(Long memberId, String orderId);
}