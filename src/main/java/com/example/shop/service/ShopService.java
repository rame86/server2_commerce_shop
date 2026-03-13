package com.example.shop.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.shop.dto.request.OrderCreateRequestDTO;
import com.example.shop.dto.request.ProductCreateRequestDTO;
import com.example.shop.dto.response.CartResponseDTO;
import com.example.shop.dto.response.OrderResponseDTO;
import com.example.shop.dto.response.ProductResponseDTO;
import com.example.shop.dto.response.WishlistResponseDTO;

public interface ShopService {

    // ======================== 상품 관련 ========================

    List<ProductResponseDTO> getProducts();

    ProductResponseDTO getProduct(String productId);

    ProductResponseDTO createProduct(Long memberId, String role, ProductCreateRequestDTO requestDto, MultipartFile imageFile);

    void deleteProduct(Long memberId, String productId);

    // ======================== 주문 관련 ========================

    OrderResponseDTO createOrder(Long memberId, OrderCreateRequestDTO requestDto);

    List<OrderResponseDTO> getMyOrders(Long memberId, int page, int size);

    OrderResponseDTO getOrder(Long memberId, String orderId);

    OrderResponseDTO cancelOrder(Long memberId, String orderId);

    // ======================== 장바구니 관련 ========================

    CartResponseDTO getCart(Long memberId);

    CartResponseDTO addToCart(Long memberId, Long productId, int quantity);

    CartResponseDTO removeFromCart(Long memberId, Long cartItemId);

    // ======================== 찜목록 관련 ========================

    List<WishlistResponseDTO> getWishlist(Long memberId);

    WishlistResponseDTO addToWishlist(Long memberId, Long productId);

    void removeFromWishlist(Long memberId, Long productId);
}