package com.example.shop.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.dto.request.OrderCreateRequestDto;
import com.example.shop.dto.request.OrderItemDto;
import com.example.shop.dto.request.ProductCreateRequestDto;
import com.example.shop.dto.response.OrderResponseDto;
import com.example.shop.dto.response.ProductResponseDto;
import com.example.shop.repository.ShopMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 쇼핑몰 서비스 구현체
 * 상품 관리 및 주문 처리 로직 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopMapper shopMapper;

    /**
     * 상품 목록 조회
     * 
     * @param category 카테고리 필터
     * @param sellerId 판매자 식별자 필터
     * @param page     페이지 번호
     * @param size     페이지당 항목 수
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProducts(String category, Long sellerId, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("category", category);
        params.put("sellerId", sellerId);
        params.put("offset", page * size); // 페이징 시작점 계산
        params.put("limit", size);

        return shopMapper.findProducts(params);
    }

    /**
     * 상품 상세 조회
     * 
     * @throws BusinessException PRODUCT_NOT_FOUND (상품이 없을 경우)
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(String productId) {
        ProductResponseDto product = shopMapper.findProductById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    /**
     * 신규 상품 등록
     * 
     * @param role 사용자 권한 (ARTIST 여부 확인용)
     */
    @Override
    @Transactional
    public ProductResponseDto createProduct(Long memberId, String role, ProductCreateRequestDto requestDto) {
        Map<String, Object> params = new HashMap<>();
        params.put("sellerId", memberId);
        // 권한에 따른 판매자 타입 구분
        params.put("sellerType", "ARTIST".equals(role) ? "ARTIST" : "USER");
        params.put("category", requestDto.getCategory());
        params.put("title", requestDto.getTitle());
        params.put("description", requestDto.getDescription());
        params.put("price", requestDto.getPrice());
        params.put("stockQuantity", requestDto.getStockQuantity());

        // MyBatis에서 selectKey 등을 통해 생성된 productId가 params에 담긴다고 가정
        shopMapper.insertProduct(params);
        return shopMapper.findProductById((String) params.get("productId"));
    }

    /**
     * 상품 삭제 (논리 삭제)
     * 
     * @throws BusinessException FORBIDDEN (본인 상품이 아닐 경우)
     */
    @Override
    @Transactional
    public void deleteProduct(Long memberId, String productId) {
        ProductResponseDto product = getProduct(productId);

        // 소유권 확인: 보안 요소 검토
        if (!product.getSellerId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        shopMapper.softDeleteProduct(productId);
    }

    /**
     * 주문 생성 및 재고 관리
     * 1. 재고 및 활성화 여부 검증
     * 2. 총 주문 금액 계산
     * 3. 주문 마스터 삽입
     * 4. 주문 상세 삽입 및 실시간 재고 차감
     */
    @Override
    @Transactional
    public OrderResponseDto createOrder(Long memberId, OrderCreateRequestDto requestDto) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 1단계: 유효성 검증 및 합계 계산
        for (OrderItemDto item : requestDto.getItems()) {
            ProductResponseDto product = getProduct(item.getProductId());

            // 판매 중지 상태 확인
            if (!product.getIsActive()) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
            }
            // 재고 부족 확인
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK);
            }

            // 총 금액 누적 (수량 * 단가)
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 2단계: 주문(Order) 마스터 정보 저장
        Map<String, Object> orderParams = new HashMap<>();
        orderParams.put("memberId", memberId);
        orderParams.put("totalAmount", totalAmount);
        orderParams.put("shippingAddress", requestDto.getShippingAddress());
        shopMapper.insertOrder(orderParams);

        String orderId = (String) orderParams.get("orderId");

        // 3단계: 주문 상세(OrderItem) 저장 및 재고 차감
        for (OrderItemDto item : requestDto.getItems()) {
            ProductResponseDto product = shopMapper.findProductById(item.getProductId());

            Map<String, Object> itemParams = new HashMap<>();
            itemParams.put("orderId", orderId);
            itemParams.put("productId", item.getProductId());
            itemParams.put("quantity", item.getQuantity());
            itemParams.put("unitPrice", product.getPrice()); // 구매 시점의 가격 스냅샷 저장
            shopMapper.insertOrderItem(itemParams);

            // 데이터베이스 레벨에서 재고 차감 (동시성 고려 필요)
            shopMapper.decreaseStock(item.getProductId(), item.getQuantity());
        }

        return shopMapper.findOrderById(orderId);
    }

    /**
     * 내 주문 목록 조회 (페이징)
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getMyOrders(Long memberId, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", memberId);
        params.put("offset", page * size);
        params.put("limit", size);

        return shopMapper.findOrdersByMemberId(params);
    }

    /**
     * 주문 상세 조회
     * 
     * @throws BusinessException FORBIDDEN (본인 주문이 아닐 경우)
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrder(Long memberId, String orderId) {
        OrderResponseDto order = shopMapper.findOrderById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 보안 검증: 주문자 본인 확인
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return order;
    }

    /**
     * 주문 취소
     * 
     * @throws BusinessException CANNOT_CANCEL_ORDER (PENDING 상태가 아닐 경우)
     */
    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long memberId, String orderId) {
        OrderResponseDto order = getOrder(memberId, orderId);

        // 취소 가능 상태 확인 (예: 배송 시작 전인 PENDING 상태만 가능)
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_ORDER);
        }

        shopMapper.updateOrderStatus(orderId, "CANCELLED");
        return shopMapper.findOrderById(orderId);
    }
}