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
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    // 이미지 저장 기본 경로 (보안 점검: 운영 환경에서는 별도의 스토리지나 설정 파일 주입 권장)
    private final String uploadPath = "D:/shop/images/";

    // 조건별 상품 목록을 조회하는 메서드
    @Override
    @Transactional(readOnly = true) // 데이터 변경이 없으므로 성능을 위해 읽기 전용 트랜잭션 적용
    public List<ProductResponseDto> getProducts(String category, Long sellerId, int page, int size) {
        if (category != null && !category.isEmpty()) {
            ProductCategory productCategory = ProductCategory.valueOf(category.toUpperCase());
            return productRepository.findByCategoryAndIsActiveTrue(productCategory).stream()
                    .map(ProductResponseDto::fromEntity)
                    .collect(Collectors.toList());
        }
        return productRepository.findAll().stream()
                .filter(Product::getIsActive) // is_active = true인 상품만 필터링
                .map(ProductResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 단일 상품에 대한 정보를 조회하는 메서드
    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(String productId) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND)); // UUID 변환 및 예외 처리
        return ProductResponseDto.fromEntity(product);
    }

    // 핵심 로직: 이미지와 상품의 상세 옵션(Variants)을 하나의 트랜잭션으로 묶어서 저장
    @Override
    @Transactional
    public ProductResponseDto createProduct(Long memberId, String role, ProductCreateRequestDto requestDto, MultipartFile imageFile) {
        String savedFileName = null;

        // 1. 첨부된 이미지 파일이 있을 경우 물리적 경로에 파일 저장 수행
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String originalFilename = imageFile.getOriginalFilename();
                // 파일명 중복 방지를 위한 UUID 부착
                savedFileName = UUID.randomUUID().toString() + "_" + originalFilename;

                File saveDir = new File(uploadPath);
                if (!saveDir.exists()) saveDir.mkdirs(); // 디렉토리가 없으면 생성

                imageFile.transferTo(new File(uploadPath + savedFileName));
            } catch (IOException e) {
                // I/O 예외 발생 시 비즈니스 커스텀 예외로 래핑하여 던짐 (트랜잭션 롤백 유도)
                throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }

        // 2. 권한 정보(role)를 분석하여 ProductCategory 및 DB에 매핑될 SellerType 결정
        ProductCategory productCategory = role.equals("ADMIN") ? ProductCategory.OFFICIAL : ProductCategory.SECONDHAND;
        SellerType sellerType = role.equals("ARTIST") ? SellerType.ARTIST : SellerType.USER; // 권한에 따른 아티스트/유저 분기

        // 3. 빌더 패턴을 이용해 부모 엔티티인 Product 객체 초기화
        Product product = Product.builder()
                .category(productCategory)
                .sellerType(sellerType) 
                .sellerId(memberId)
                .title(requestDto.getProductName())
                .description(requestDto.getProductDetail())
                .price(requestDto.getPrice())
                .imageUrl(savedFileName)
                .isActive(true)
                .build();

        // 4. 요청 DTO에 상세 옵션(Variants) 데이터가 포함되어 있다면 추출하여 엔티티로 변환
        if (requestDto.getVariants() != null && !requestDto.getVariants().isEmpty()) {
            requestDto.getVariants().forEach(vDto -> {
                ProductVariant variant = ProductVariant.builder()
                        .color(vDto.getColor())
                        .size(vDto.getSize())
                        .additionalPrice(vDto.getAdditionalPrice())
                        .stockQuantity(vDto.getStockQuantity())
                        .skuCode(vDto.getSkuCode())
                        .build();
                // 5. 생성된 옵션 객체를 부모 상품의 리스트에 추가 (양방향 연관관계 편의 메서드 호출)
                product.addVariant(variant); 
            });
        }

        // 6. DB에 최종 저장 (Product의 Cascade 설정에 의해 리스트에 담긴 Variant들도 함께 INSERT 됨)
        return ProductResponseDto.fromEntity(productRepository.save(product));
    }

    // 판매자가 본인의 상품을 삭제(비활성화) 처리하는 메서드
    @Override
    @Transactional
    public void deleteProduct(Long memberId, String productId) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        
        // 데이터 소유자 검증 처리
        if (!product.getSellerId().equals(memberId))
            throw new BusinessException(ErrorCode.FORBIDDEN);
            
        product.softDelete(); // DB의 is_active 컬럼 값을 false로 변경 (추후 조회 조건에서 제외됨)
    }

    /*==========================================================
     * 주문(Order) 관련 메서드 영역 (보내주신 원본 코드 로직 그대로 유지)
     *==========================================================*/
    
    @Override
    @Transactional
    public OrderResponseDto createOrder(Long memberId, OrderCreateRequestDto requestDto) {
        Order order = Order.builder()
                .memberId(memberId)
                .shippingAddress(requestDto.getShippingAddress())
                .status(OrderStatus.PENDING)
                .build();

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

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getMyOrders(Long memberId, int page, int size) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getMemberId().equals(memberId))
                .map(OrderResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrder(Long memberId, String orderId) {
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getMemberId().equals(memberId))
            throw new BusinessException(ErrorCode.FORBIDDEN);
        return OrderResponseDto.fromEntity(order);
    }

    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long memberId, String orderId) {
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getMemberId().equals(memberId))
            throw new BusinessException(ErrorCode.FORBIDDEN);
        return OrderResponseDto.fromEntity(order);
    }
}