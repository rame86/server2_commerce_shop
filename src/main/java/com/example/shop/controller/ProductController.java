package com.example.shop.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.shop.dto.request.ProductCreateRequestDto;
import com.example.shop.dto.response.ProductResponseDto;
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
    @GetMapping("/")
    public String product() {
        return "굿즈 전체목록 받음";
    }

    /**
     * [공식 굿즈 목록 조회]
     * GET /product/official
     */
    @GetMapping("/official")
    public List<ProductResponseDto> official() {
        // category를 "official"로 필터링하여 서비스에 상품 목록을 요청함
        return shopService.getProducts("official", null, 0, 10);
    }

    @GetMapping("/secondhand")
    public List<ProductResponseDto> secondhand() {
        return shopService.getProducts("secondhand", null, 0, 10);
    }

    @GetMapping("/unofficial")
    public List<ProductResponseDto> fanmade() {
        return shopService.getProducts("unofficial", null, 0, 10);
    }

    /*************************************************************/
    // 관리자
    /*************************************************************/
    /**
     * [공식 굿즈 등록 - 관리자 전용]
     * POST /product/official
     * consumes = MediaType.MULTIPART_FORM_DATA_VALUE: JSON 데이터와 파일을 동시에 받기 위한 설정
     */
    @PostMapping(value = "/official", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponseDto createOfficial(
            @RequestPart("data") ProductCreateRequestDto requestDto, // 텍스트/JSON 데이터 부분
            @RequestPart(value = "image", required = false) MultipartFile image) { // 업로드된 이미지 파일 부분
        
        // 실제 운영 환경에서는 로그인 정보(SecurityContext)에서 memberId를 뽑아야 하지만, 현재는 테스트용으로 1L(관리자) 전달
        return shopService.createProduct(1L, "ADMIN", requestDto, image);
    }
    /**
     * [공식 굿즈 삭제]
     * DELETE /product/official/{productId}
     */
    @DeleteMapping("/official/{productId}")
    public void deleteOfficial(@PathVariable String productId) {
        // 경로에 들어온 {productId}를 읽어서 해당 상품을 삭제 처리함
        shopService.deleteProduct(1L, productId);
    }

    /*************************************************************/
    // 유저
    /*************************************************************/
    /**
     * [중고 굿즈 등록 - 일반 유저]
     * POST /product/secondhand
     */
    @PostMapping(value = "/secondhand", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponseDto createSecondhand(
            @RequestPart("data") ProductCreateRequestDto requestDto,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        
        // 테스트용으로 일반 사용자(2L, USER) 권한으로 상품을 등록함
        return shopService.createProduct(2L, "USER", requestDto, image);
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
