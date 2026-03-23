package com.example.shop.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    @PostMapping(value = "/official", consumes = { "multipart/form-data", "application/json" })
    public ProductResponseDTO createOfficial(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long memberId,
            @ModelAttribute ProductCreateRequestDTO requestDto,
            @RequestPart(name = "imageFile", required = false) MultipartFile imageFile) {
        log.info(">>>> [Controller] 공식 상품 등록 요청 시작: {}", requestDto.getGoodsName());
        ProductResponseDTO response = shopService.createProduct(memberId, "ADMIN", requestDto, imageFile);
        log.info(">>>> [Controller] 서비스 호출 완료, 반환 데이터 title: {}", response.getTitle());
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
    @PostMapping(value = "/secondhand", consumes = { "multipart/form-data", "application/json" })
    public ProductResponseDTO createSecondhand(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long memberId,
            @ModelAttribute ProductCreateRequestDTO requestDto,
            @RequestPart(name = "imageFile", required = false) MultipartFile imageFile) {
        log.info("중고 상품 등록 요청: {}", requestDto.getGoodsName());
        return shopService.createProduct(memberId, "USER", requestDto, imageFile);
    }

    @PostMapping(value = "/unofficial", consumes = { "multipart/form-data", "application/json" })
    public ProductResponseDTO createFanmade(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long memberId,
            @ModelAttribute ProductCreateRequestDTO requestDto,
            @RequestPart(name = "imageFile", required = false) MultipartFile imageFile) {
        log.info("팬메이드 상품 등록 요청: {}", requestDto.getGoodsName());
        return shopService.createProduct(memberId, "USER", requestDto, imageFile);
    }

    @DeleteMapping("/secondhand/{productId}")
    public void deleteSecondhand(@PathVariable String productId) {
        shopService.deleteProduct(1L, productId);
    }

    @DeleteMapping("/unofficial/{productId}")
    public void deleteFanmade(@PathVariable String productId) {
        shopService.deleteProduct(1L, productId);
    }

}
