package com.example.shop.controller;

import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.shop.dto.response.ProductResponseDto;
import com.example.shop.service.ShopService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/shop")
@Slf4j
@RequiredArgsConstructor
public class ShopController {

    private final StringRedisTemplate stringRedisTemplate;

    private final ShopService shopService; // Repository 대신 Service 주입

    @GetMapping("/")
    public List<ProductResponseDto> list() {
        // 기본값으로 0페이지, 10개씩 조회하도록 수정
        return shopService.getProducts(null, null, 0, 10);
    }

    @GetMapping("/detail")
    public ProductResponseDto detail(
            @RequestParam(value = "product_id", required = false) String product_id) {
        if (product_id == null) {
            // 파라미터가 없을 때의 예외 처리 로직 (예: 기본 상품 반환 또는 커스텀 에러)
            log.warn("product_id가 전달되지 않았습니다.");
            return null;
        }
        return shopService.getProduct(product_id);
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

    @PostMapping("/Wishlist")
    public String updateWishlist() {
        return "찜 목록 추가";
    }

    @GetMapping("/wishlist")
    public String wishlist() {
        return "찜목록 요청받음";
    }

    @PostMapping("/Cart")
    public String updateCart() {
        return "장바구니 목록 추가";

    }

    @GetMapping("/cart")
    public String cartlist() {
        return "장바구니목록 요청받음";
    }
    
    @DeleteMapping("/Cart")
    public String deleteCart() {
        return "장바구니 상품 삭제";
    }

    @PostMapping("/order")
    public String order() {
        return "주문추가 요청받음";
    }

    @GetMapping("/order")
    public String orderlist() {
        return "주문목록 요청받음";
    }

    @PostMapping("/checkout")
    public String checkout() {
        return "결제 요청";
    }

}
