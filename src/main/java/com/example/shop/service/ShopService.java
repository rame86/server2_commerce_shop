package com.example.shop.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.shop.dto.request.OrderCreateRequestDTO;
import com.example.shop.dto.request.ProductCreateRequestDTO;
import com.example.shop.dto.response.CartResponseDTO;
import com.example.shop.dto.response.OrderResponseDTO;
import com.example.shop.dto.response.ProductResponseDTO;
import com.example.shop.dto.response.WishlistResponseDTO;
import com.example.shop.entity.Approval;

public interface ShopService {

    // ======================== 상품 관련 ========================
    List<ProductResponseDTO> getProducts();

    ProductResponseDTO getProduct(String productId);

    ProductResponseDTO createProduct(Long memberId, String role, ProductCreateRequestDTO requestDto,
            MultipartFile imageFile);

    void deleteProduct(Long memberId, String productId);

    List<Approval> getPendingApprovals();

    // ======================== 주문 관련 ========================
    // 주문 생성 및 결제 큐 전송
    OrderResponseDTO createOrder(Long memberId, OrderCreateRequestDTO requestDto);

    // 내 주문 목록 조회 (페이징)
    List<OrderResponseDTO> getMyOrders(Long memberId, int page, int size);

    // 주문 상세 조회
    OrderResponseDTO getOrder(Long memberId, String orderId);

    // 결제 처리 (체크아웃)
    String checkout(Long memberId, com.example.shop.dto.request.CheckoutRequestDTO requestDto);

    // ======================== 장바구니 관련 ========================
    // 장바구니 조회
    CartResponseDTO getCart(Long memberId);

    // 장바구니 아이템 추가
    CartResponseDTO addToCart(Long memberId, Long productId, int quantity);

    // 장바구니 아이템 삭제 (cartItemId 기준)
    CartResponseDTO removeFromCart(Long memberId, Long cartItemId);

    // ======================== 찜목록 관련 ========================
    List<WishlistResponseDTO> getWishlist(Long memberId);

    WishlistResponseDTO addToWishlist(Long memberId, Long productId);

    void removeFromWishlist(Long memberId, Long productId);

    // ======================== 추가 기능(주문내역삭제, 리뷰등록/조회 ) ========================
    void deleteOrder(Long memberId, Long orderId);

    void createReview(Long memberId, Long productId, Integer rating, String comment, MultipartFile reviewImage);

    List<com.example.shop.dto.response.ReviewResponseDTO> getProductReviews(Long productId);
}