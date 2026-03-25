package com.example.shop.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import com.example.shop.entity.ApprovalStatus;
import com.example.shop.entity.Cart;
import com.example.shop.entity.CartItem;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.OrderStatus;
import com.example.shop.entity.Product;
import com.example.shop.entity.ProductVariant;
import com.example.shop.entity.Wishlist;
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
    private final ProductVariantRepository productVariantRepository; // ✅ variant 조회용
    private final CartRepository cartRepository;
    private final CartitemRepository cartitemRepository;
    private final OrderRepository orderRepository;
    private final WishlistRepository wishlistRepository;
    private final ApprovalRepository approvalRepository;
    private final com.example.shop.repository.ReviewRepository reviewRepository; // ✅ 추가
    private final RabbitTemplate rabbitTemplate;
    private final ProductMessageProducer productMessageProducer;

    // ======================== 상품 관련 ========================
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProducts() {
        // ✅ findAll() → findByIsActive(true) : 판매 중 상품만 조회
        return productRepository.findByIsActive(true).stream()
                .map(this::toProductResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProduct(String productId) {
        Product product = productRepository.findById(Long.parseLong(productId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return toProductResponseDTO(product);
    }

    // Product 엔티티를 DTO로 변환하면서 리뷰 정보 추가
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
        return dto;
    }

    @Override
    @Transactional
    public ProductResponseDTO createProduct(Long memberId, String role, ProductCreateRequestDTO requestDto,
            MultipartFile imageFile) {
        log.info("======= 서비스 로직 진입 완료 =======");

        // [수정] 물리적 파일 저장 로직 제거
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            // 실제 파일을 저장하지 않고, DB 기록을 위해 파일명만 생성하여 유지합니다.
            imageUrl = "/images/shop/" + UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        }

        // ✅ 엔티티 빌더 부분
        String color = requestDto.getColor();
        String size = requestDto.getSize();
        String itemCategory = requestDto.getItemCategory();
        Integer stockQuantity = 0;

        if (requestDto.getVariants() != null && !requestDto.getVariants().isEmpty()) {
            ProductCreateRequestDTO.VariantDTO firstVariant = requestDto.getVariants().get(0);
            if (color == null)
                color = firstVariant.getColor();
            if (size == null)
                size = firstVariant.getSize();
            stockQuantity = firstVariant.getStockQuantity();
        }

        // [추가] 1. Product 엔티티 생성 및 저장
        // role이 ADMIN이나 ARTIST이면 ARTIST, 아니면 USER로 설정
        SellerType sellerType = "ADMIN".equalsIgnoreCase(role) || "ARTIST".equalsIgnoreCase(role) ? SellerType.ARTIST
                : SellerType.USER;

        Product product = Product.builder()
                .sellerId(memberId)
                .sellerType(sellerType)
                .category(ProductCategory.valueOf(requestDto.getGoodsType()))
                .title(requestDto.getGoodsName())
                .description(requestDto.getDescription())
                .imageUrl(imageUrl)
                .basePrice(requestDto.getPrice())
                .itemCategory(itemCategory)
                .color(color)
                .size(size)
                .isActive(true) // 등록 즉시 활성화하여 조회 가능하도록 설정
                .build();

        product = productRepository.save(product);

        // [추가] 2. ProductVariant 엔티티 생성 및 저장 (기본 옵션 등록)
        // 상품 주문을 위해서는 최소 하나 이상의 variant가 필요합니다.
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .color(color)
                .size(size)
                .additionalPrice(BigDecimal.ZERO)
                .stockQuantity(stockQuantity != null ? stockQuantity : 0)
                .skuCode("SKU-" + product.getProductId() + "-" + System.currentTimeMillis())
                .build();

        productVariantRepository.save(variant);

        // 3. Approval 엔티티 생성 및 저장 (기존 로직 유지하며 productId 연결)
        Approval approvalRequest = Approval.builder()
                .requesterId(memberId)
                .requesterName(requestDto.getRequesterName())
                .goodsName(requestDto.getGoodsName())
                .goodsType(ProductCategory.valueOf(requestDto.getGoodsType()))
                .description(requestDto.getDescription())
                .price(requestDto.getPrice())
                .color(color)
                .size(size)
                .itemCategory(itemCategory)
                .stockQuantity(stockQuantity)
                .imageUrl(imageUrl)
                .build();

        approvalRequest.linkProduct(product.getProductId()); // 생성된 상품 ID 연결
        approvalRequest.updateStatus(ApprovalStatus.CONFIRMED, "자동 승인 완료"); // CONFIRMED 상태로 즉시 변경

        approvalRequest = approvalRepository.save(approvalRequest);

        log.info(">>>> [상품 등록 완료] Product ID: {}, Approval ID: {}", product.getProductId(),
                approvalRequest.getApprovalId());

        return ProductResponseDTO.fromEntity(product); // 생성된 Product 정보를 기반으로 DTO 반환
    }

    @Override
    @Transactional
    public void deleteProduct(Long memberId, String productId) {
        // ✅ 존재 여부 확인 후 삭제
        if (!productRepository.existsById(Long.parseLong(productId))) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(Long.parseLong(productId));
    }

    @Transactional(readOnly = true)
    public List<Approval> getPendingApprovals() {
        // PENDING 상태인 데이터만 최신순으로 가져옴
        return approvalRepository.findByStatusOrderByCreatedAtDesc(ApprovalStatus.PENDING);
    }

    // ======================== 주문 관련 ========================
    @Override
    @Transactional
    public OrderResponseDTO createOrder(Long memberId, OrderCreateRequestDTO requestDto) {

        // 1. 주문 기본 엔티티 생성 (DB: orders 테이블)
        Order order = Order.builder()
                .memberId(memberId)
                .shippingAddress(requestDto.getShippingAddress())
                .status(OrderStatus.PENDING)
                .build();

        BigDecimal feePercentage = new BigDecimal("0.10"); // 기본 10% (OFFICIAL)
        String eventTitle = "";
        Long sellerId = null; // ✅ artistId -> sellerId로 변경

        // 2. 주문 상품 상세 처리 (DB: order_items 테이블)
        for (int i = 0; i < requestDto.getItems().size(); i++) {
            OrderItemDTO itemDto = requestDto.getItems().get(i);

            UUID variantUUID = UUID.fromString(itemDto.getVariantId());
            ProductVariant variant = productVariantRepository.findById(variantUUID)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
            Product product = variant.getProduct();

            // 수수료 판별: OFFICIAL 외(UNOFFICIAL, SECONDHAND) 상품이 하나라도 있으면 15% 적용
            String category = product.getCategory().name();
            if ("UNOFFICIAL".equals(category) || "SECONDHAND".equals(category)) {
                feePercentage = new BigDecimal("0.15");
            }

            // 단가 계산 (기본가 + 옵션가)
            BigDecimal unitPrice = product.getBasePrice().add(variant.getAdditionalPrice());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .variant(variant)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(unitPrice)
                    .build();

            order.addOrderItem(orderItem);

            if (i == 0) {
                eventTitle = product.getTitle()
                        + (requestDto.getItems().size() > 1 ? " 외 " + (requestDto.getItems().size() - 1) + "건" : "");
                sellerId = product.getSellerId(); // ✅ getArtistId() -> getSellerId()로 수정
            }
        }

        // 3. 주문 정보 저장
        Order savedOrder = orderRepository.save(order);

        // 4. 결제 서비스로 보낼 이벤트 생성
        PaymentEventDTO paymentEvent = new PaymentEventDTO();
        paymentEvent.setType("PAYMENT");
        paymentEvent.setOrderId(savedOrder.getOrderId().toString());
        paymentEvent.setMemberId(memberId);
        paymentEvent.setArtistId(sellerId); // ✅ DTO의 필드명도 sellerId로 맞추는 것을 권장합니다.
        paymentEvent.setAmount(savedOrder.getTotalAmount());

        // 🌟 [중요] SettlementService(결제 서버)는 originalPrice * quantity 로 정산액을 계산함.
        // 여러 품목이 섞인 SHOP 주문의 경우, 전체 합계(totalAmount)를 originalPrice로 보내고
        // quantity를 1로 고정하여 정산액이 꼬이지 않게 함 (예매 서비스 패턴 참고).
        paymentEvent.setOriginalPrice(savedOrder.getTotalAmount());
        paymentEvent.setQuantity(1);

        // 🌟 [수정] 수수료는 0.10이 아니라 10(%) 처럼 정수로 보내야 결제 서버에서 정확히 계산됨 (divide(100) 대응)
        paymentEvent.setFee(feePercentage.multiply(new BigDecimal("100")));

        paymentEvent.setEventTitle(eventTitle);
        paymentEvent.setReplyRoutingKey(RabbitMQConfig.SHOP_PAY_REPLY_ROUTING_KEY);

        // 5. RabbitMQ 메시지 전송
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "pay.request", paymentEvent);

        log.info(">>>> [주문 생성 완료] ID: {}, 판매자: {}, 적용 수수료: {}", savedOrder.getOrderId(), sellerId, feePercentage);

        return OrderResponseDTO.fromEntity(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyOrders(Long memberId, int page, int size) {
        // 기존 페이징 로직 유지
        return orderRepository.findByMemberId(memberId).stream()
                .map(OrderResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(Long memberId, String orderId) {
        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return OrderResponseDTO.fromEntity(order);
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
        return "결제가 요청되었습니다. 주문번호: " + orderResponse.getOrderId();
    }

    // ======================== 장바구니 관련 ========================
    @Override
    @Transactional
    public CartResponseDTO getCart(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> cartRepository.save(Cart.builder().memberId(memberId).build()));
        return CartResponseDTO.fromEntity(cart);
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
        return CartResponseDTO.fromEntity(cart);
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

        return CartResponseDTO.fromEntity(cart);
    }

    // ======================== 찜목록 관련 ========================
    @Override
    @Transactional(readOnly = true)
    public List<WishlistResponseDTO> getWishlist(Long memberId) {
        return wishlistRepository.findByMemberId(memberId).stream()
                .map(WishlistResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WishlistResponseDTO addToWishlist(Long memberId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        return wishlistRepository.findByMemberIdAndProduct_ProductId(memberId, productId)
                .map(WishlistResponseDTO::fromEntity)
                .orElseGet(() -> {
                    Wishlist wishlist = Wishlist.builder()
                            .memberId(memberId)
                            .product(product)
                            .build();
                    return WishlistResponseDTO.fromEntity(wishlistRepository.save(wishlist));
                });
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long memberId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByMemberIdAndProduct_ProductId(memberId, productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WISHLIST_NOT_FOUND));
        wishlistRepository.delete(wishlist);
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
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
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
    }
}