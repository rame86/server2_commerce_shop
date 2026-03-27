package com.example.shop.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.shop.common.exception.BusinessException;
import com.example.shop.common.exception.ErrorCode;
import com.example.shop.config.RabbitMQConfig;
import com.example.shop.dto.message.PaymentEventDTO;
import com.example.shop.dto.request.OrderCreateRequestDTO;
import com.example.shop.dto.request.OrderItemDTO;
import com.example.shop.dto.request.ProductCreateRequestDTO;
import com.example.shop.dto.response.CartResponseDTO;
import com.example.shop.dto.response.OrderResponseDTO;
import com.example.shop.dto.response.ProductResponseDTO;
import com.example.shop.dto.response.WishlistResponseDTO;
import com.example.shop.entity.Approval;
import com.example.shop.entity.Cart;
import com.example.shop.entity.CartItem;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.OrderStatus;
import com.example.shop.entity.Product;
import com.example.shop.entity.ProductVariant;
import com.example.shop.entity.Wishlist;
import com.example.shop.entity.enums.ApprovalStatus;
import com.example.shop.entity.enums.ProductCategory;
import com.example.shop.entity.enums.SellerType;
import com.example.shop.messaging.producer.ProductMessageProducer;
import com.example.shop.repository.ApprovalRepository;
import com.example.shop.repository.CartRepository;
import com.example.shop.repository.CartitemRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.ProductVariantRepository;
import com.example.shop.repository.WishlistRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository; // variant 조회용
    private final CartRepository cartRepository;
    private final CartitemRepository cartitemRepository;
    private final OrderRepository orderRepository;
    private final WishlistRepository wishlistRepository;
    private final ApprovalRepository approvalRepository;
    private final com.example.shop.repository.ReviewRepository reviewRepository; 
    private final RabbitTemplate rabbitTemplate;
    private final ProductMessageProducer productMessageProducer;

    @Value("${shop.image.upload-path}")
    private String uploadPath;

    // ======================== 상품 관련 ========================
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProducts() {
        List<ProductResponseDTO> result = productRepository.findByIsActive(true).stream()
                .map(this::toProductResponseDTO)
                .collect(Collectors.toList());
        
        
        log.info(">>>> [getProducts 리턴 데이터] 건수: {}, 데이터: {}", result.size(), result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProduct(String productId) {
        Product product = productRepository.findById(Long.parseLong(productId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        
        ProductResponseDTO result = toProductResponseDTO(product);
        
        
        log.info(">>>> [getProduct 리턴 데이터] 데이터: {}", result);
        return result;
    }

    // Product 엔티티를 DTO로 변환하면서 리뷰 정보 및 재고 수량 모집
    private ProductResponseDTO toProductResponseDTO(Product product) {
        ProductResponseDTO dto = ProductResponseDTO.fromEntity(product);
        List<com.example.shop.entity.Review> reviews = reviewRepository
                .findByProductIdOrderByCreatedAtDesc(product.getProductId());

        if (!reviews.isEmpty()) {
            double avg = reviews.stream()
                    .mapToInt(com.example.shop.entity.Review::getRating)
                    .average()
                    .orElse(0.0);
            dto.setAverageRating(Math.round(avg * 10.0) / 10.0);
            dto.setReviewCount((long) reviews.size());
        } else {
            dto.setAverageRating(0.0);
            dto.setReviewCount(0L);
        }

        // variant 안의 stock_quantity 합산
        List<ProductVariant> variants = productVariantRepository.findByProduct_ProductId(product.getProductId());
        int totalStock = variants.stream().mapToInt(ProductVariant::getStockQuantity).sum();
        dto.setStockQuantity(totalStock);

        return dto;
    }

    @Override
    @Transactional
    public ProductResponseDTO createProduct(Long memberId, String role, ProductCreateRequestDTO requestDto,
            MultipartFile imageFile) {
        log.info("======= 서비스 로직 진입 완료 =======");

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            // 1. [보안] 원본 파일명에서 경로 조작 문자(../ 등) 제거 및 순수 파일명 추출
            String originalFilename = StringUtils.cleanPath(imageFile.getOriginalFilename());
            String safeFilename = Paths.get(originalFilename).getFileName().toString();

            // 2. 고유 파일명 생성
            String uniqueFileName = UUID.randomUUID() + "_" + safeFilename;
            imageUrl = "/images/shop/" + uniqueFileName;

            // 3. 물리적 파일 저장 로직
            try {
                File uploadDir = new File(uploadPath);
                // 디렉토리가 존재하지 않으면 생성 (부모 디렉토리 포함)
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // 지정된 경로에 실제 파일 저장
                File saveFile = new File(uploadPath, uniqueFileName);
                imageFile.transferTo(saveFile);
                log.info(">>>> [파일 저장 완료] 경로: {}", saveFile.getAbsolutePath());
            } catch (IOException e) {
                log.error(">>>> [파일 저장 실패] 파일명: {}", uniqueFileName, e);
                // 파일 저장이 필수라면 여기서 예외를 던져 트랜잭션을 롤백시킵니다.
                throw new RuntimeException("이미지 파일 저장 중 오류가 발생했습니다.");
            }
        }

        // 엔티티 빌더 부분
        String color = requestDto.getColor();
        String size = requestDto.getSize();
        String itemCategory = requestDto.getItemCategory();
        Integer stockQuantity = requestDto.getStockQuantity() != null ? requestDto.getStockQuantity() : 0;

        if (requestDto.getVariants() != null && !requestDto.getVariants().isEmpty()) {
            ProductCreateRequestDTO.VariantDTO firstVariant = requestDto.getVariants().get(0);
            if (color == null)
                color = firstVariant.getColor();
            if (size == null)
                size = firstVariant.getSize();
        }

        // [추가] 1. Product 엔티티 생성 및 저장
        // role이 ADMIN이나 ARTIST이면 ARTIST, 아니면 USER로 설정
        SellerType sellerType = "ADMIN".equalsIgnoreCase(role) || "ARTIST".equalsIgnoreCase(role) ? SellerType.ARTIST
                : SellerType.USER;

        Product product = Product.builder()
                .sellerId(memberId)
                .artistId(requestDto.getArtistId())
                .sellerType(sellerType)
                .category(ProductCategory.valueOf(requestDto.getGoodsType().toUpperCase()))
                .title(requestDto.getGoodsName())
                .description(requestDto.getDescription())
                .imageUrl(imageUrl)
                .basePrice(requestDto.getPrice())
                .itemCategory(itemCategory)
                .color(color)
                .size(size)
                .isActive(false)
                .build();

        product = productRepository.save(product);

        // [추가] 2. ProductVariant 엔티티 생성 및 저장 (기본 옵션 등록)
        // 상품 주문을 위해서는 최소 하나 이상의 variant가 필요합니다.
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .color(color)
                .size(size)
                .additionalPrice(BigDecimal.ZERO)
                .stockQuantity(stockQuantity)
                .skuCode("SKU-" + product.getProductId() + "-" + System.currentTimeMillis())
                .build();

        productVariantRepository.save(variant);

        // 3. Approval 엔티티 생성 및 저장 (기존 로직 유지하며 productId 연결)
        Approval approvalRequest = Approval.builder()
                .requesterId(memberId)
                .requesterName(requestDto.getRequesterName())
                .artistId(requestDto.getArtistId())
                .goodsName(requestDto.getGoodsName())
                .goodsType(ProductCategory.valueOf(requestDto.getGoodsType().toUpperCase()))
                .description(requestDto.getDescription())
                .price(requestDto.getPrice())
                .color(color)
                .size(size)
                .itemCategory(itemCategory)
                .stockQuantity(stockQuantity)
                .imageUrl(imageUrl)
                .build();

        approvalRequest.linkProduct(product.getProductId()); // 생성된 상품 ID 연결
        approvalRequest.updateStatus(ApprovalStatus.PENDING, "승인 대기중");

        approvalRequest = approvalRepository.save(approvalRequest);

        log.info(">>>> [상품 등록 완료] Product ID: {}, Approval ID: {}", product.getProductId(),
                approvalRequest.getApprovalId());

        ProductResponseDTO result = ProductResponseDTO.fromEntity(product);
        
        
        log.info(">>>> [createProduct 리턴 데이터] 데이터: {}", result);
        return result;
    }

    @Override
    @Transactional
    public void deleteProduct(Long memberId, String productId) {
       
        if (!productRepository.existsById(Long.parseLong(productId))) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(Long.parseLong(productId));
        log.info(">>>> [deleteProduct 완료] 상품 삭제됨 ID: {}", productId);
    }

    @Transactional(readOnly = true)
    public List<Approval> getPendingApprovals() {
        // PENDING 상태인 데이터만 최신순으로 가져옴
        List<Approval> result = approvalRepository.findByStatusOrderByCreatedAtDesc(ApprovalStatus.PENDING);
        
       
        log.info(">>>> [getPendingApprovals 리턴 데이터] 건수: {}, 데이터: {}", result.size(), result);
        return result;
    }

    // ======================== 주문 관련 ========================
    @Override
    @Transactional
    public OrderResponseDTO createOrder(Long memberId, OrderCreateRequestDTO requestDto) {

        // 1. 운송장 번호 자동 생성 (TRK + 타임스탬프 + UUID 일부 조합)
        String generatedTrackingNumber = "TRK" + System.currentTimeMillis()
                + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        // 2. 배송비 설정 (DTO에서 넘어온 값이 있으면 사용, 없으면 기본값 3000원 설정)
        BigDecimal shippingFee = requestDto.getShippingFee() != null ? requestDto.getShippingFee()
                : new BigDecimal("3000");

        // 3. 주문 기본 엔티티 생성 (배송비와 운송장 번호 필드 추가)
        Order order = Order.builder()
                .memberId(memberId)
                .shippingAddress(requestDto.getShippingAddress())
                .shippingFee(shippingFee)
                .trackingNumber(generatedTrackingNumber)
                .status(OrderStatus.PENDING)
                .build();

        BigDecimal feePercentage = new BigDecimal("0.10");
        String eventTitle = "";
        Long artistId = null;
        BigDecimal firstItemUnitPrice = BigDecimal.ZERO;
        int totalQuantity = 0;

        // 상품 합계 금액 계산용 (배송비 제외)
        BigDecimal itemsTotalAmount = BigDecimal.ZERO;

        // 4. 주문 상품 상세 처리
        for (int i = 0; i < requestDto.getItems().size(); i++) {
            OrderItemDTO itemDto = requestDto.getItems().get(i);

            UUID variantUUID = UUID.fromString(itemDto.getVariantId());
            ProductVariant variant = productVariantRepository.findById(variantUUID)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
            Product product = variant.getProduct();

            // 수수료 판별
            String category = product.getCategory().name();
            if ("UNOFFICIAL".equals(category) || "SECONDHAND".equals(category)) {
                feePercentage = new BigDecimal("0.15");
            }

            // 재고 확인
            if (variant.getStockQuantity() < itemDto.getQuantity()) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND); // 재고 부족
            }

            // 단가 및 소계 계산
            BigDecimal unitPrice = product.getBasePrice().add(variant.getAdditionalPrice());
            BigDecimal itemSubtotal = unitPrice.multiply(new BigDecimal(itemDto.getQuantity()));
            itemsTotalAmount = itemsTotalAmount.add(itemSubtotal);

            // 재고 차감 (JPQL update 대신 엔티티 직접 수정)
            variant.decreaseStock(itemDto.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .variant(variant)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(unitPrice)
                    .build();

            order.addOrderItem(orderItem);

            totalQuantity += itemDto.getQuantity();

            if (i == 0) {
                eventTitle = product.getTitle()
                        + (requestDto.getItems().size() > 1 ? " 외 " + (requestDto.getItems().size() - 1) + "건" : "");
                artistId = product.getArtistId();
                firstItemUnitPrice = unitPrice;
            }
        }

        // 5. 최종 결제 금액 설정 (상품 합계 + 배송비)
        // Order 엔티티 내에 totalAmount를 계산하는 로직이 없다면 아래와 같이 직접 세팅해야 합니다.
        order.setTotalAmount(itemsTotalAmount.add(shippingFee));

        // 6. 주문 정보 저장
        Order savedOrder = orderRepository.save(order);

        // 7. 결제 서비스로 보낼 이벤트 생성
        PaymentEventDTO paymentEvent = new PaymentEventDTO();
        paymentEvent.setType("PAYMENT");
        paymentEvent.setOrderId(savedOrder.getOrderId().toString());
        paymentEvent.setMemberId(memberId);
        paymentEvent.setArtistId(artistId);
        paymentEvent.setAmount(savedOrder.getTotalAmount()); // 배송비가 포함된 총액 전송

        paymentEvent.setOriginalPrice(firstItemUnitPrice);
        paymentEvent.setQuantity(totalQuantity);
        paymentEvent.setShippingFee(shippingFee);

        // 수수료 정수 변환 (10% -> 10)
        paymentEvent.setFee(feePercentage.multiply(new BigDecimal("100")));

        paymentEvent.setEventTitle(eventTitle);
        paymentEvent.setReplyRoutingKey(RabbitMQConfig.SHOP_PAY_REPLY_ROUTING_KEY);

        // 8. RabbitMQ 메시지 전송
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "pay.request", paymentEvent);

        log.info(">>>> [주문 생성 완료] ID: {}, 운송장: {}, 배송비: {}, 총액: {}",
                savedOrder.getOrderId(), generatedTrackingNumber, shippingFee, savedOrder.getTotalAmount());

        OrderResponseDTO result = OrderResponseDTO.fromEntity(savedOrder);
        
       
        log.info(">>>> [createOrder 리턴 데이터] 데이터: {}", result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyOrders(Long memberId, int page, int size) {
        // 기존 페이징 로직 유지
        List<OrderResponseDTO> result = orderRepository.findByMemberId(memberId).stream()
                .map(OrderResponseDTO::fromEntity)
                .collect(Collectors.toList());
                
       
        log.info(">>>> [getMyOrders 리턴 데이터] 건수: {}, 데이터: {}", result.size(), result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(Long memberId, String orderId) {
        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
                
        OrderResponseDTO result = OrderResponseDTO.fromEntity(order);
        
        
        log.info(">>>> [getOrder 리턴 데이터] 데이터: {}", result);
        return result;
    }

    @Override
    @Transactional
    public String checkout(Long memberId, com.example.shop.dto.request.CheckoutRequestDTO requestDto) {
        log.info(">>>> [체크아웃 시작] 회원: {}, 상품: {}, 수량: {}", memberId, requestDto.getProductId(),
                requestDto.getQuantity());

        // 1. 해당 상품의 기본 Variant 정보를 찾아야 함 (현재 스키마상 Variant ID를 모르므로 첫 번째 활성 Variant 사용)
        productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        List<ProductVariant> variants = productVariantRepository.findByProduct_ProductId(requestDto.getProductId());

        if (variants == null || variants.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        ProductVariant variant = variants.get(0); // 기본 옵션 선택

        // 2. OrderCreateRequestDTO 조립
        OrderItemDTO itemDto = new OrderItemDTO();
        itemDto.setVariantId(variant.getVariantId().toString());
        itemDto.setQuantity(requestDto.getQuantity());

        OrderCreateRequestDTO orderReq = new OrderCreateRequestDTO();
        orderReq.setItems(Collections.singletonList(itemDto));
        orderReq.setShippingAddress("기본 배송지 (체크아웃)"); // 필요시 프론트에서 받아야 함

        // 3. 주문 생성 (여기서 결제 요청 MQ까지 발송됨)
        OrderResponseDTO orderResponse = createOrder(memberId, orderReq);

        log.info(">>>> [체크아웃 완료] 주문번호: {}", orderResponse.getOrderId());
        
        String result = "결제가 요청되었습니다. 주문번호: " + orderResponse.getOrderId();
        
        
        log.info(">>>> [checkout 리턴 데이터] 데이터: {}", result);
        return result;
    }

    // ======================== 장바구니 관련 ========================
    @Override
    @Transactional
    public CartResponseDTO getCart(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> cartRepository.save(Cart.builder().memberId(memberId).build()));
                
        CartResponseDTO result = CartResponseDTO.fromEntity(cart);
        
       
        log.info(">>>> [getCart 리턴 데이터] 데이터: {}", result);
        return result;
    }

    @Override
    @Transactional
    public CartResponseDTO addToCart(Long memberId, Long productId, int quantity) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> cartRepository.save(Cart.builder().memberId(memberId).build()));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Optional<CartItem> existingItem = cartitemRepository
                .findByCart_CartIdAndProduct_ProductId(cart.getCartId(), productId);

        if (existingItem.isPresent()) {
            // 이미 담긴 상품이면 수량만 증가
            existingItem.get().updateQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cart.addItem(newItem);
            cartitemRepository.save(newItem);
        }
        
        CartResponseDTO result = CartResponseDTO.fromEntity(cart);
        
      
        log.info(">>>> [addToCart 리턴 데이터] 데이터: {}", result);
        return result;
    }

    @Override
    @Transactional
    public CartResponseDTO removeFromCart(Long memberId, Long cartItemId) {
        CartItem item = cartitemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (!item.getCart().getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        Cart cart = item.getCart();
        cart.removeItem(item);
        cartitemRepository.delete(item);

        CartResponseDTO result = CartResponseDTO.fromEntity(cart);
        
       
        log.info(">>>> [removeFromCart 리턴 데이터] 데이터: {}", result);
        return result;
    }

    @Override
    @Transactional
    public CartResponseDTO clearCart(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        cartitemRepository.deleteByCart_CartId(cart.getCartId());
        cart.getCartItems().clear();

        CartResponseDTO result = CartResponseDTO.fromEntity(cart);
        log.info(">>>> [clearCart 완료] 회원: {}", memberId);
        return result;
    }

    // ======================== 찜목록 관련 ========================
    @Override
    @Transactional(readOnly = true)
    public List<WishlistResponseDTO> getWishlist(Long memberId) {
        List<WishlistResponseDTO> result = wishlistRepository.findByMemberId(memberId).stream()
                .map(WishlistResponseDTO::fromEntity)
                .collect(Collectors.toList());
                
    
        log.info(">>>> [getWishlist 리턴 데이터] 건수: {}, 데이터: {}", result.size(), result);
        return result;
    }

    @Override
    @Transactional
    public WishlistResponseDTO addToWishlist(Long memberId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        WishlistResponseDTO result = wishlistRepository.findByMemberIdAndProduct_ProductId(memberId, productId)
                .map(WishlistResponseDTO::fromEntity)
                .orElseGet(() -> {
                    Wishlist wishlist = Wishlist.builder()
                            .memberId(memberId)
                            .product(product)
                            .build();
                    return WishlistResponseDTO.fromEntity(wishlistRepository.save(wishlist));
                });
                
   
        log.info(">>>> [addToWishlist 리턴 데이터] 데이터: {}", result);
        return result;
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long memberId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByMemberIdAndProduct_ProductId(memberId, productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WISHLIST_NOT_FOUND));
        wishlistRepository.delete(wishlist);
        log.info(">>>> [removeFromWishlist 완료] 찜목록 삭제됨 상품 ID: {}", productId);
    }

    // ======================== 추가 기능 구현 ========================
    @Override
    @Transactional
    public void deleteOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        orderRepository.delete(order);
        log.info(">>>> [주문 삭제 완료] 주문번호: {}, 회원: {}", orderId, memberId);
    }

    // ======================== 리뷰 관련 ========================
    @Override
    @Transactional
    public void createReview(Long memberId, Long productId, Integer rating, String comment, MultipartFile reviewImage) {
        productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // [수정] 물리적 파일 저장 로직 제거
        String imageUrl = null;
        if (reviewImage != null && !reviewImage.isEmpty()) {
            // 실제 저장 없이 경로 문자열만 생성
            imageUrl = "/images/shop/reviews/" + UUID.randomUUID() + "_" + reviewImage.getOriginalFilename();
        }

        com.example.shop.entity.Review review = com.example.shop.entity.Review.builder()
                .memberId(memberId)
                .productId(productId)
                .rating(rating)
                .comment(comment)
                .imageUrl(imageUrl)
                .build();

        reviewRepository.save(review);
        log.info(">>>> [리뷰 작성 완료] 상품: {}, 회원: {}, 이미지 URL만 기록: {}", productId, memberId, imageUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.example.shop.dto.response.ReviewResponseDTO> getProductReviews(Long productId) {
        List<com.example.shop.dto.response.ReviewResponseDTO> result = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(r -> com.example.shop.dto.response.ReviewResponseDTO.builder()
                        .reviewId(r.getReviewId())
                        .memberId(r.getMemberId())
                        .productId(r.getProductId())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .imageUrl(r.getImageUrl())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
                
        // 리턴 데이터 로그 추가
        log.info(">>>> [getProductReviews 리턴 데이터] 건수: {}, 데이터: {}", result.size(), result);
        return result;
    }

}