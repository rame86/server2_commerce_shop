package com.example.shop.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
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

    // application.yml(또는 properties)에서 파일 경로 주입 권장
    // 예: file.upload.dir=/app/images/ (docker) 혹은 D:/shop/images/ (local)
    @Value("${file.upload.dir:D:/shop/images/}")
    private String uploadPath;

    // ======================== 상품 관련 ========================

    /**
     * 카테고리별 상품 목록 조회
     * goodsType이 있으면 해당 타입만, 없으면 전체 활성 상품 반환
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProducts(String goodsType, Long requesterId, int page, int size) {
        if (goodsType != null && !goodsType.isEmpty()) {
            ProductCategory productCategory = ProductCategory.valueOf(goodsType.toUpperCase());
            // [수정]: 수정된 레포지토리 메서드 호출
            return productRepository.findByGoodsTypeAndStatusTrue(productCategory).stream()
                    .map(ProductResponseDto::fromEntity)
                    .collect(Collectors.toList());
        }
        
        return productRepository.findAll().stream()
                .filter(Product::getStatus) // [수정]: getStatus() 사용
                .map(ProductResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    /**
     * 단일 상품 상세 조회
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(String productId) {
        Product product = productRepository.findById(Long.valueOf(productId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponseDto.fromEntity(product);
    }

    @Override
    @Transactional
    public ProductResponseDto createProduct(Long memberId, String role, ProductCreateRequestDto requestDto,
            MultipartFile imageFile) {
        String savedFileName = null;

        // 1. 이미지 파일 저장 로직 (예외 처리 강화)
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String originalFilename = imageFile.getOriginalFilename();
                savedFileName = UUID.randomUUID().toString() + "_" + originalFilename;
                File saveDir = new File(uploadPath);
                if (!saveDir.exists())
                    saveDir.mkdirs();

                // 파일 생성 및 저장
                imageFile.transferTo(new File(uploadPath + savedFileName));
            } catch (IOException e) {
                log.error("파일 업로드 실패: {}", e.getMessage());
                throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }

        // 2. 권한 검증 (NPE 방지)
        ProductCategory productCategory = "ADMIN".equals(role) ? ProductCategory.OFFICIAL : ProductCategory.SECONDHAND;
        SellerType sellerType = "ARTIST".equals(role) ? SellerType.ARTIST : SellerType.USER;

        // 3. 상품 엔티티 생성
       Product product = Product.builder()
                .goodsType("ADMIN".equals(role) ? ProductCategory.OFFICIAL : ProductCategory.SECONDHAND)
                .sellerType("ARTIST".equals(role) ? SellerType.ARTIST : SellerType.USER)
                .requesterId(memberId)
                .requesterName(requestDto.getRequesterName())
                .goodsName(requestDto.getGoodsName())
                .description(requestDto.getDescription())
                .price(requestDto.getPrice())
                .imageUrl(savedFileName)
                .status(true) 
                .build();

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

        // 1. 상품 DB 저장
        Product savedProduct = productRepository.save(product);
        log.info("상품 저장 완료 - productId: {}", savedProduct.getProductId());

        // 2. 재고 합산 (Java Stream 활용)
        int totalStock = savedProduct.getVariants().stream()
                .mapToInt(ProductVariant::getStockQuantity)
                .sum();

        // 3. RabbitMQ 승인 요청 전송 (ShopApprovalMessage 레코드 양식 준수)
        // [Self-Review]: savedProduct에 Long 타입의 고유 ID(예: getId())가 있다고 가정합니다.
        // 만약 UUID(productId)만 존재한다면, 레코드 타입을 UUID로 바꾸거나 식별자 설계를 변경해야 합니다.
        productMessageProducer.sendProductCreatedEvent(new ShopApprovalMessage(
                savedProduct.getProductId(), 
                savedProduct.getRequesterId(), 
                savedProduct.getRequesterName(), 
                savedProduct.getGoodsName(), 
                savedProduct.getGoodsType().name(), 
                savedProduct.getDescription(), 
                savedProduct.getPrice().intValue(), 
                totalStock, 
                savedProduct.getImageUrl() 
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
        Product product = productRepository.findById(Long.valueOf(productId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        
        if (!product.getRequesterId().equals(memberId))
            throw new BusinessException(ErrorCode.FORBIDDEN);

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
        Order order = Order.builder()
                .memberId(memberId)
                .shippingAddress(requestDto.getShippingAddress())
                .status(OrderStatus.PENDING)
                .build();

        for (OrderItemDto itemDto : requestDto.getItems()) {
            // 1. DB에서 Product 엔티티 객체를 조회하여 변수 'product'에 할당
            Product product = productRepository.findById(Long.valueOf(itemDto.getProductId()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

            // 2. OrderItem 빌더에는 'product' 객체 자체를 전달
            order.addOrderItem(OrderItem.builder()
                    .product(product) // product.getProductId()가 아닌 'product' 객체여야 함
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