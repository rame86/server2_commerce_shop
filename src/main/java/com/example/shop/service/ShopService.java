package com.example.shop.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.shop.dto.request.OrderCreateRequestDto;
import com.example.shop.dto.request.ProductCreateRequestDto;
import com.example.shop.dto.response.OrderResponseDto;
import com.example.shop.dto.response.ProductResponseDto;

// 샵 도메인의 주요 비즈니스 로직을 정의하는 서비스 인터페이스
public interface ShopService {

    // ======================== 상품 관련 ========================

    /**
     * 카테고리별 상품 목록 조회
     * @param goodsType 상품 타입 (OFFICIAL, UNOFFICIAL, SECONDHAND)
     * @param requesterId 판매자 ID (null이면 전체 조회)
     * @param page 페이지 번호
     * @param size 페이지 크기
     */
    List<ProductResponseDto> getProducts(String goodsType, Long requesterId, int page, int size);

    /**
     * 단일 상품 상세 조회
     * @param productId 상품 UUID
     */
    ProductResponseDto getProduct(String productId);

    /**
     * 상품 등록 (이미지 파일 포함)
     * @param memberId 등록 요청자 ID
     * @param role 요청자 권한 (ADMIN, USER, ARTIST)
     * @param requestDto 상품 등록 요청 데이터
     * @param imageFile 업로드 이미지 파일 (선택)
     */
    ProductResponseDto createProduct(Long memberId, String role, ProductCreateRequestDto requestDto, MultipartFile imageFile);

    /**
     * 상품 삭제 (실제 삭제가 아닌 비활성화 처리)
     * @param memberId 삭제 요청자 ID
     * @param productId 삭제할 상품 UUID
     */
    void deleteProduct(Long memberId, String productId);

    // ======================== 주문 관련 ========================

    /**
     * 주문 생성
     * @param memberId 주문자 ID
     * @param requestDto 주문 요청 데이터
     */
    OrderResponseDto createOrder(Long memberId, OrderCreateRequestDto requestDto);

    /**
     * 내 주문 목록 조회
     * @param memberId 조회할 회원 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     */
    List<OrderResponseDto> getMyOrders(Long memberId, int page, int size);

    /**
     * 단일 주문 상세 조회
     * @param memberId 조회 요청자 ID
     * @param orderId 주문 UUID
     */
    OrderResponseDto getOrder(Long memberId, String orderId);

    /**
     * 주문 취소
     * @param memberId 취소 요청자 ID
     * @param orderId 취소할 주문 UUID
     */
    OrderResponseDto cancelOrder(Long memberId, String orderId);
}