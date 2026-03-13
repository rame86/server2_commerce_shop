package com.example.shop.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.shop.dto.request.OrderCreateRequestDto;
import com.example.shop.dto.request.ProductCreateRequestDto;
import com.example.shop.dto.response.CartResponseDto;
import com.example.shop.dto.response.OrderResponseDto;
import com.example.shop.dto.response.ProductResponseDto;
import com.example.shop.dto.response.WishlistResponseDto;

public interface ShopService {

    // ======================== 상품 관련 ========================

    List<ProductResponseDto> getProducts(String goodsType, Long requesterId, int page, int size);

    ProductResponseDto getProduct(String productId);

    ProductResponseDto createProduct(Long memberId, String role, ProductCreateRequestDto requestDto, MultipartFile imageFile);

    void deleteProduct(Long memberId, String productId);

    // ======================== 주문 관련 ========================

    OrderResponseDto createOrder(Long memberId, OrderCreateRequestDto requestDto);

    List<OrderResponseDto> getMyOrders(Long memberId, int page, int size);

    OrderResponseDto getOrder(Long memberId, String orderId);

    OrderResponseDto cancelOrder(Long memberId, String orderId);

    // ======================== 장바구니 관련 ========================

    CartResponseDto getCart(Long memberId);

    CartResponseDto addToCart(Long memberId, Long productId, int quantity);

    CartResponseDto removeFromCart(Long memberId, Long cartItemId);

    // ======================== 찜목록 관련 ========================

    List<WishlistResponseDto> getWishlist(Long memberId);

    WishlistResponseDto addToWishlist(Long memberId, Long productId);

    void removeFromWishlist(Long memberId, Long productId);
}