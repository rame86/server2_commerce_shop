package com.example.shop.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.shop.dto.response.CheckoutResponse;
import com.example.shop.dto.response.ShopListResponseDTO;
import com.example.shop.dto.response.ShopResponseDTO;
import com.example.shop.dto.response.WishlistResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/shop")
@Slf4j
@RequiredArgsConstructor
public class ShopController {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * [GET] 전체 상품 목록 조회 O
     * 반환값: JSON 배열을 포함한 객체0
     */
    @GetMapping("/")
    // <Map<String, Object>>를 <ShopListResponseDTO>로 수정
    public ResponseEntity<ShopListResponseDTO> shoplist() {
        List<Map<String, Object>> products = List.of(
                Map.of("id", 1, "name", "상품A", "price", 10000),
                Map.of("id", 2, "name", "상품B", "price", 20000));

        ShopListResponseDTO response = ShopListResponseDTO.builder()
                .items(products)
                .total(products.size())
                .build();

        return ResponseEntity.ok(response);
    }

    // [GET] 상품 상세 정보 조회 (JSONB 데이터 포함)O
    @GetMapping("/detail/{productId}")
    public ResponseEntity<ShopResponseDTO> getProductDetail(
            @PathVariable Long productId,
            @RequestHeader(value = "X-User-Id", required = false) String memberId) {

        log.info("상품 상세 조회 - 상품ID: {}, 요청자: {}", productId, memberId);

        // JSONB 데이터 예시: DB 컬럼 타입이 JSONB일 경우 MyBatis TypeHandler를 거쳐 Map으로 변환됨
        Map<String, Object> productMetadata = Map.of(
                "color", "red",
                "size", "L",
                "origin", "Korea");

        ShopResponseDTO response = ShopResponseDTO.builder()
                .productId(productId)
                .productName("상세 상품명")
                .productDetail(productMetadata) // JSONB 매핑 필드
                .build();

        return ResponseEntity.ok(response);
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

    // [POST] 찜 목록 업데이트 O
    @PostMapping("/updateWishlist")
    public ResponseEntity<WishlistResponseDTO> updateWishlist(
            @RequestBody(required = false) Map<String, Object> wishlistData) {

        // 1. 요청 데이터 로그 기록 (MSA 환경에서 추적용)
        log.info("찜 목록 업데이트 요청 수신: {}", wishlistData);

        // [비즈니스 로직 영역]
        // 예: redisTemplate.opsForSet().add("wishlist:" + memberId, productId);

        // 2. Builder 패턴을 사용하여 응답 객체 생성
        // 기존 Map.of 방식보다 필드 추가 및 가독성 측면에서 훨씬 유리합니다.
        WishlistResponseDTO response = WishlistResponseDTO.builder()
                .status("success")
                .message("찜 목록 업데이트 완료")
                .build();

        // 3. ResponseEntity를 통해 HTTP 200 상태코드와 함께 DTO 반환
        // Spring이 내부적으로 이 DTO를 JSON {"status":"success", "message":"..."}로 변환합니다.
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wishlist")
    public String wishlist() {
        return "찜목록 요청받음";
    }

    // [POST] 장바구니 담기
    @PostMapping("/updateCart")
    public ResponseEntity<Map<String, Object>> updateCart(@RequestBody Map<String, Object> cartData) {
        // 예외 처리: 요청 본문이 비어있는 경우 체크 (Self-Review 적용)
        if (cartData == null || cartData.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "장바구니 데이터가 없습니다."));
        }

        log.info("장바구니 추가 데이터: {}", cartData);
        return ResponseEntity.ok(Map.of("status", "200", "message", "장바구니 추가 성공"));
    }

    @GetMapping("/cartlist")
    public String cartlist() {
        return "장바구니목록 요청받음";
    }

    @PostMapping("/order")
    public String order() {
        return "주문추가 요청받음";
    }

    @GetMapping("/orderlist")
    public String orderlist() {
        return "주문목록 요청받음";
    }

    // O
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@RequestBody Map<String, Object> requestData) {

        // 1. 비즈니스 로직 수행 (Service 호출 등)
        log.info("결제 요청 데이터: {}", requestData);

        // 2. JSONB 데이터 준비 (실제로는 Mapper를 통해 DB에서 가져온 Map 객체)
        Map<String, Object> paymentDetails = Map.of(
                "method", "card",
                "card_type", "visa",
                "is_success", true);

        // 3. 응답 객체 생성 및 반환
        CheckoutResponse response = CheckoutResponse.builder()
                .status("200")
                .orderId(1004L)
                .paymentDetails(paymentDetails) // 이 부분이 JSON 내부의 중괄호{}로 들어감
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deletecart")
    public String deletecart() {
        return "장바구니 상품 삭제";
    }

}
