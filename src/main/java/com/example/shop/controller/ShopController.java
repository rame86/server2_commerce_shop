package com.example.shop.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.shop.dto.request.CartRequestDTO;
import com.example.shop.dto.request.OrderCreateRequestDTO;
import com.example.shop.dto.request.WishlistRequestDTO;
import com.example.shop.dto.response.CartResponseDTO;
import com.example.shop.dto.response.OrderResponseDTO;
import com.example.shop.dto.response.ProductResponseDTO;
import com.example.shop.dto.response.WishlistResponseDTO;
import com.example.shop.service.ShopService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/shop")
@Slf4j
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService; // Repository 대신 Service 주입

    
    @GetMapping("/")
    public List<ProductResponseDTO> list() {
        log.info("----------> /shop/list 요청 받음");
        return shopService.getProducts();
    }

    @GetMapping("/detail/{productId}")
    public ProductResponseDTO detail(@PathVariable(name = "productId") String productId) { // name 명시
        log.info("상품 상세 조회 요청 - ID: {}", productId);
        return shopService.getProduct(productId);
    }

    @GetMapping("/userTestLua")
    public String userTestLua(
            @RequestHeader("X-User-Id") String member_id,
            @RequestHeader("X-Role") String role) {
        String user_info = ("상세보기 요청받음. member_id=" + member_id + " role=" + role);

        return user_info;
    }

    @GetMapping("/userTestRedis")
    public String userTestRedis() {
        String user_info = "";
        return user_info;
    }

    @PostMapping("/wishlist")
    public WishlistResponseDTO addToWishlist(
            @RequestHeader("X-User-Id") Long memberId,
            @RequestBody WishlistRequestDTO requestDto) {
        log.info("찜하기 요청 - 회원: {}, 상품: {}", memberId, requestDto.getProductId());

        return shopService.addToWishlist(memberId, requestDto.getProductId());
    }

    @GetMapping("/wishlist")
    public List<WishlistResponseDTO> getWishlist(
            @RequestHeader("X-User-Id") Long memberId) {
        return shopService.getWishlist(memberId);
    }

    @DeleteMapping("/wishlist/{productId}")
    public void removeFromWishlist(
            @RequestHeader("X-User-Id") Long memberId,
            @PathVariable(name = "productId") Long productId) {
        log.info("삭제 요청 - 회원: {}, 상품: {}", memberId, productId);
        shopService.removeFromWishlist(memberId, productId);
    }

    // 장바구니 상품 추가
    @PostMapping("/cart")
    public CartResponseDTO addToCart(
            @RequestHeader("X-User-Id") Long memberId,
            @RequestBody CartRequestDTO requestDto) {
        log.info("장바구니 추가 - 회원: {}, 상품: {}, 수량: {}",
                memberId, requestDto.getProductId(), requestDto.getQuantity());
        return shopService.addToCart(memberId, requestDto.getProductId(), requestDto.getQuantity());
    }

    // 장바구니 목록 조회
    @GetMapping("/cart")
    public CartResponseDTO getCart(@RequestHeader("X-User-Id") Long memberId) {
        log.info("장바구니 조회 요청 - 회원: {}", memberId);
        return shopService.getCart(memberId);
    }

    // 장바구니 상품 삭제
    @DeleteMapping("/cart/{cartItemId}")
    public CartResponseDTO removeFromCart(
            @RequestHeader("X-User-Id") Long memberId,
            @PathVariable(name = "cartItemId") Long cartItemId) {
        log.info("장바구니 삭제 - 회원: {}, 아이템 ID: {}", memberId, cartItemId);
        return shopService.removeFromCart(memberId, cartItemId);
    }

    // 주문 생성 (주문하기)
    @PostMapping("/order")
    public ResponseEntity<OrderResponseDTO> createOrder(
            @RequestHeader("X-User-Id") Long memberId,
            @RequestBody OrderCreateRequestDTO requestDto) {

        // 1. 필수 값 검증
        if (requestDto.getItems() == null || requestDto.getItems().isEmpty()) {
            throw new IllegalArgumentException("주문 항목이 비어 있습니다.");
        }

        // 2. 서비스 호출 및 DB 저장
        OrderResponseDTO response = shopService.createOrder(memberId, requestDto);

        return ResponseEntity.ok(response);
    }

    // 내 주문 목록 조회 (페이징 처리 포함)
    @GetMapping("/order")
    public List<OrderResponseDTO> getMyOrders(
            @RequestHeader("X-User-Id") Long memberId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("주문 목록 조회 - 회원: {}, 페이지: {}", memberId, page);
        return shopService.getMyOrders(memberId, page, size);
    }

    // 결제 프로세스 시작 (체크아웃)
    @PostMapping("/checkout")
    public String checkout(
            @RequestHeader("X-User-Id") Long memberId,
            @RequestBody com.example.shop.dto.request.CheckoutRequestDTO requestDto) {
        log.info("결제 요청 - 회원: {}, 상품: {}, 수량: {}", memberId, requestDto.getProductId(), requestDto.getQuantity());
        return shopService.checkout(memberId, requestDto);
    }

}
