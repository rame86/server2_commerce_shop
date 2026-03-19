package com.example.shop.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.shop.dto.request.ProductCreateRequestDTO;
import com.example.shop.dto.response.ProductResponseDTO;
import com.example.shop.service.ShopService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ShopService shopService;

    /*************************************************************/
    // 공통사용
    /*************************************************************/

    /**
     * [공식 굿즈 목록 조회]
     * GET /product/official
     */
    @GetMapping("/official")
    public List<ProductResponseDTO> official() {
        // category를 "official"로 필터링하여 서비스에 상품 목록을 요청함
        return shopService.getProducts();
    }

    @GetMapping("/secondhand")
    public List<ProductResponseDTO> secondhand() {
        return shopService.getProducts();
    }

    @GetMapping("/unofficial")
    public List<ProductResponseDTO> fanmade() {
        return shopService.getProducts();
    }

    /*************************************************************/
    // 관리자
    /*************************************************************/
    /**
     * [공식 굿즈 등록 - 관리자 전용]
     * POST /product/official
     */
    @PostMapping("/official")
    public ProductResponseDTO createOfficial(@RequestBody ProductCreateRequestDTO requestDto) {
        log.info(">>>> [Controller] 공식 상품 등록 요청 시작: {}", requestDto.getGoodsName());

        // 1. 서비스 호출 (한 번만 호출하고 결과를 변수에 담음)
        // 현재 로그인 정보를 가져올 수 없는 상황이라면 임시로 1L, "ADMIN"을 직접 넘깁니다.
        ProductResponseDTO response = shopService.createProduct(1L, "ADMIN", requestDto, null);

        log.info(">>>> [Controller] 서비스 호출 완료, 반환 데이터 title: {}", response.getTitle());

        // 2. 결과 반환
        return response;
    }

    /** [공식 굿즈 삭제] */
    @DeleteMapping("/official/{productId}")
    public void deleteOfficial(@PathVariable String productId) {
        shopService.deleteProduct(1L, productId);
    }

    /*************************************************************/
    // 유저
    /*************************************************************/

    /** [중고 굿즈 등록] */
    @PostMapping("/secondhand")
    public ProductResponseDTO createSecondhand(@RequestBody ProductCreateRequestDTO requestDto) {
        log.info("중고 상품 등록 요청: {}", requestDto.getGoodsName());
        return shopService.createProduct(2L, "USER", requestDto, null);
    }

    @PostMapping("/unofficial")
    public String updateFanmade() {
        return "팬메이드굿즈 등록";
    }

    @DeleteMapping("/secondhand")
    public String deleteSecondhand() {
        return "중고굿즈 삭제";
    }

    @DeleteMapping("/unofficial")
    public String deleteFanmade() {
        return "팬메이드굿즈 삭제";
    }

}
