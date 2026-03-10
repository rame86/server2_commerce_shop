package com.example.shop.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.shop.common.exception.BusinessException;
import com.example.shop.common.exception.ErrorCode;
import com.example.shop.dto.message.ShopApprovalMessage;
import com.example.shop.dto.request.OrderCreateRequestDto;
import com.example.shop.dto.request.OrderItemDto;
import com.example.shop.dto.request.ProductCreateRequestDto;
import com.example.shop.dto.response.OrderResponseDto;
import com.example.shop.dto.response.ProductResponseDto;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.OrderStatus;
import com.example.shop.entity.Product;
import com.example.shop.entity.ProductCategory;
import com.example.shop.entity.ProductVariant;
import com.example.shop.entity.SellerType;
import com.example.shop.messaging.producer.ProductMessageProducer;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ProductMessageProducer productMessageProducer;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    // 이미지 저장 기본 경로 (운영 환경에서는 설정 파일로 주입 권장)
    private final String uploadPath = "D:/shop/images/";

    // ======================== 상품 관련 ========================

    /**
     * 카테고리별 상품 목록 조회
     * goodsType이 있으면 해당 타입만, 없으면 전체 활성 상품 반환
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProducts(String goodsType, Long requesterId, int page, int size) {
        if (goodsType != null && !goodsType.isEmpty()) {
            // 카테고리 enum 변환 후 해당 카테고리 활성 상품만 조회
            ProductCategory productCategory = ProductCategory.valueOf(goodsType.toUpperCase());
            return productRepository.findByGoodsTypeAndIsActiveTrue(productCategory).stream()
                    .map(ProductResponseDto::fromEntity)
                    .collect(Collectors.toList());
        }
        // 카테고리 없으면 전체 활성 상품 조회
        return productRepository.findAll().stream()
                .filter(Product::getIsActive)
                .map(ProductResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 단일 상품 상세 조회
     * UUID 변환 실패 또는 상품 없으면 예외 발생
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(String productId) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponseDto.fromEntity(product);
    }

    /**
     * 상품 등록
     * 1. 이미지 저장
     * 2. 상품 엔티티 생성 및 옵션 추가
     * 3. DB 저장
     * 4. RabbitMQ로 승인 요청 메시지 발행
     */
    @Override
    @Transactional
    public ProductResponseDto createProduct(Long memberId, String role, ProductCreateRequestDto requestDto, MultipartFile imageFile) {
        String savedFileName = null;

        // 1. 이미지 파일이 있으면 서버에 저장
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String originalFilename = imageFile.getOriginalFilename();
                savedFileName = UUID.randomUUID().toString() + "_" + originalFilename;
                File saveDir = new File(uploadPath);
                if (!saveDir.exists()) saveDir.mkdirs();
                imageFile.transferTo(new File(uploadPath + savedFileName));
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }

        // 2. 권한에 따라 상품 카테고리 및 판매자 타입 결정
        // ADMIN이면 공식 굿즈, 아니면 중고로 등록
        ProductCategory productCategory = role.equals("ADMIN") ? ProductCategory.OFFICIAL : ProductCategory.SECONDHAND;
        // ARTIST이면 아티스트 타입, 아니면 일반 유저 타입
        SellerType sellerType = role.equals("ARTIST") ? SellerType.ARTIST : SellerType.USER;

        // 3. 상품 엔티티 생성
        Product product = Product.builder()
                .goodsType(productCategory)
                .sellerType(sellerType)
                .requesterId(memberId)
                .requesterName(requestDto.getRequesterName())
                .goodsName(requestDto.getGoodsName())
                .description(requestDto.getDescription())
                .price(requestDto.getPrice())
                .imageUrl(savedFileName)
                .isActive(true)
                .build();

        // 상품 옵션(Variant)이 있으면 함께 추가
        if (requestDto.getVariants() != null) {
            requestDto.getVariants().forEach(vDto -> {
                product.addVariant(ProductVariant.builder()
                        .color(vDto.getColor())
                        .size(vDto.getSize())
                        .additionalPrice(vDto.getAdditionalPrice())
                        .stockQuantity(vDto.getStockQuantity())
                        .skuCode(vDto.getSkuCode())
                        .build());
            });
        }

        // 4. DB 저장
        Product savedProduct = productRepository.save(product);
        log.info("상품 저장 완료 - productId: {}", savedProduct.getProductId());

        // 5. RabbitMQ로 승인 요청 메시지 발행
        // Variant 재고 합산하여 전달
        productMessageProducer.sendProductCreatedEvent(new ShopApprovalMessage(
                null,                                                                        // goodsId (현재 없음)
                savedProduct.getRequesterId(),                                               // requesterId
                savedProduct.getRequesterName(),                                             // requesterName
                savedProduct.getGoodsName(),                                                 // goodsName
                savedProduct.getGoodsType().name(),                                          // goodsType
                savedProduct.getDescription(),                                               // description
                savedProduct.getPrice().intValue(),                                          // price
                savedProduct.getVariants().stream().mapToInt(v -> v.getStockQuantity()).sum(), // stock 합산
                savedProduct.getImageUrl()                                                   // imageUrl
        ));

        return ProductResponseDto.fromEntity(savedProduct);
    }

    /**
     * 상품 비활성화 처리 (Soft Delete)
     * 본인 상품이 아니면 FORBIDDEN 예외 발생
     */
    @Override
    @Transactional
    public void deleteProduct(Long memberId, String productId) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 요청자가 상품 소유자인지 검증
        if (!product.getRequesterId().equals(memberId))
            throw new BusinessException(ErrorCode.FORBIDDEN);

        // is_active를 false로 변경 (실제 DB에서 삭제하지 않음)
        product.softDelete();
    }

    // ======================== 주문 관련 ========================

    /**
     * 주문 생성
     * 주문 항목의 상품 존재 여부 확인 후 주문 저장
     */
    @Override
    @Transactional
    public OrderResponseDto createOrder(Long memberId, OrderCreateRequestDto requestDto) {
        // 주문 기본 정보 생성
        Order order = Order.builder()
                .memberId(memberId)
                .shippingAddress(requestDto.getShippingAddress())
                .status(OrderStatus.PENDING)
                .build();

        // 주문 항목별 상품 조회 후 주문에 추가
        for (OrderItemDto itemDto : requestDto.getItems()) {
            Product product = productRepository.findById(UUID.fromString(itemDto.getProductId()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
            order.addOrderItem(OrderItem.builder()
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(product.getPrice())
                    .build());
        }
        return OrderResponseDto.fromEntity(orderRepository.save(order));
    }

    /**
     * 내 주문 목록 조회
     * 전체 주문에서 해당 회원의 주문만 필터링
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getMyOrders(Long memberId, int page, int size) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getMemberId().equals(memberId))
                .map(OrderResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 단일 주문 상세 조회
     * 본인 주문이 아니면 FORBIDDEN 예외 발생
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrder(Long memberId, String orderId) {
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getMemberId().equals(memberId))
            throw new BusinessException(ErrorCode.FORBIDDEN);
        return OrderResponseDto.fromEntity(order);
    }

    /**
     * 주문 취소
     * 본인 주문이 아니면 FORBIDDEN 예외 발생
     */
    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long memberId, String orderId) {
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getMemberId().equals(memberId))
            throw new BusinessException(ErrorCode.FORBIDDEN);
        // 주문 상태를 CANCELLED로 변경 (추후 구현)
        return OrderResponseDto.fromEntity(order);
    }
}