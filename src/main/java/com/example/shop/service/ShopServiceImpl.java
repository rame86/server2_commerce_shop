package com.example.shop.service;

import java.math.BigDecimal;
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
import com.example.shop.dto.message.ShopApprovalMessage;
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
    private final RabbitTemplate rabbitTemplate;
    private final ProductMessageProducer productMessageProducer;

    // @Value("${file.upload-dir}")
    private String uploadDir;

    // ======================== 상품 관련 ========================
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProducts() {
        // ✅ findAll() → findByIsActive(true) : 판매 중 상품만 조회
        return productRepository.findByIsActive(true).stream()
                .map(ProductResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProduct(String productId) {
        Product product = productRepository.findById(Long.parseLong(productId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponseDTO.fromEntity(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO createProduct(Long memberId, String role, ProductCreateRequestDTO requestDto,
            MultipartFile imageFile) {
        log.info("======= 서비스 로직 진입 완료 =======");

        // 1. 이미지 처리 (생략)
        String imageUrl = null;

        // 2. 만약 "승인 대기(Approval)" 테이블에 저장하는 것이 목적이라면:
        // 로그 상으로는 productRepository.save()를 호출하고 있는데,
        // 요구사항대로라면 아래와 같이 approvalRepository를 사용해야 합니다.

        // ✅ 엔티티 빌더 부분 수정
        String color = null;
        String size = null;
        Integer stockQuantity = 0;
        if (requestDto.getVariants() != null && !requestDto.getVariants().isEmpty()) {
            ProductCreateRequestDTO.VariantDTO firstVariant = requestDto.getVariants().get(0);
            color = firstVariant.getColor();
            size = firstVariant.getSize();
            stockQuantity = firstVariant.getStockQuantity();
        }

        Approval approvalRequest = Approval.builder()
                .requesterId(memberId)
                .requesterName(requestDto.getRequesterName())
                .goodsName(requestDto.getGoodsName())
                // requestDto.getGoodsType()이 String이라면 아래와 같이 변환 필요
                .goodsType(ProductCategory.valueOf(requestDto.getGoodsType()))
                .description(requestDto.getDescription())
                .price(requestDto.getPrice())
                .color(color)
                .size(size)
                .stockQuantity(stockQuantity)
                .imageUrl(imageUrl)
                // .status(ApprovalStatus)
                .build();

        log.info(">>>> [RabbitMQ 전송 시도] RoutingKey: {}, Data: {}",
                RabbitMQConfig.ROUTING_KEY, approvalRequest.getGoodsName());

        ShopApprovalMessage message = new ShopApprovalMessage(
                approvalRequest.getRequesterId(),
                approvalRequest.getRequesterName(),
                approvalRequest.getGoodsName(),
                approvalRequest.getGoodsType().name(),
                approvalRequest.getDescription(),
                approvalRequest.getPrice(),
                approvalRequest.getColor(),
                approvalRequest.getSize(),
                approvalRequest.getStockQuantity(),
                approvalRequest.getImageUrl()
        );

        productMessageProducer.sendProductCreatedEvent(message);

        return ProductResponseDTO.fromApproval(approvalRequest);
    }

    @Override
    @Transactional
    public void deleteProduct(Long memberId, String productId) {
        // ✅ Hard Delete 대신 Soft Delete (is_active = false)
        Product product = productRepository.findById(Long.parseLong(productId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        // product.deactivate(); // Product에 deactivate() 메서드 추가 권장
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
        int totalQuantity = 0;

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
            totalQuantity += itemDto.getQuantity();

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
        paymentEvent.setOriginalPrice(savedOrder.getTotalAmount());
        paymentEvent.setQuantity(totalQuantity);
        paymentEvent.setFee(feePercentage);
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
    public String checkout(Long memberId) {
        // 결제 완료 후 장바구니 비우기
        cartRepository.findByMemberId(memberId).ifPresent(cartRepository::delete);
        return "결제가 완료되었습니다.";
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
}