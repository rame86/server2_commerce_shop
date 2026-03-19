package com.example.shop.service;

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
        Approval approvalRequest = Approval.builder()
                .requesterId(memberId)
                .requesterName(requestDto.getRequesterName())
                .goodsName(requestDto.getGoodsName())
                // requestDto.getGoodsType()이 String이라면 아래와 같이 변환 필요
                .goodsType(ProductCategory.valueOf(requestDto.getGoodsType()))
                .description(requestDto.getDescription())
                .price(requestDto.getPrice())
                .imageUrl(imageUrl)
                // .status(ApprovalStatus)
                .build();

        // [중요] 만약 현재 코드에서 productRepository.save()를 호출 중이라면
        // 이 부분을 approvalRepository.save()로 변경하거나,
        // Product 엔티티를 쓸 거라면 아래 3번처럼 모든 필드를 채워야 합니다.
        Approval saved = approvalRepository.save(approvalRequest);
        
        log.info(">>>> [RabbitMQ 전송 시도] RoutingKey: {}, Data: {}", 
             RabbitMQConfig.ROUTING_KEY, saved.getGoodsName());

    try {
        log.info(">>>> RabbitMQ로 던지기 직전!");
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                saved // 혹은 전달하고자 하는 DTO
        );
        log.info(">>>> [RabbitMQ 전송 완료] 상품명: {}", saved.getGoodsName());
    } catch (Exception e) {
        log.error(">>>> [RabbitMQ 전송 실패] 에러: {}", e.getMessage());
    }

        return ProductResponseDTO.fromApproval(saved);
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
        Order order = Order.builder()
                .memberId(memberId)
                .shippingAddress(requestDto.getShippingAddress())
                .totalAmount(requestDto.getTotalAmount()) // ✅ getTotalamount() → getTotalAmount()
                .status(OrderStatus.PENDING)
                .build();

        for (OrderItemDTO itemDto : requestDto.getItems()) {
            // variantId null/형식 검증
            if (itemDto.getVariantId() == null || itemDto.getVariantId().isBlank()) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            UUID variantUUID;
            try {
                variantUUID = UUID.fromString(itemDto.getVariantId());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            // ✅ product 조회 → variant 조회로 변경 (DB: order_items.variant_id UUID)
            ProductVariant variant = productVariantRepository
                    .findById(variantUUID)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

            // 단가 = 상품 기본가 + 옵션 추가가
            java.math.BigDecimal unitPrice = variant.getProduct().getBasePrice()
                    .add(variant.getAdditionalPrice()); // ✅ getPrice() → getBasePrice()

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .variant(variant) // ✅ product → variant
                    .quantity(itemDto.getQuantity())
                    .unitPrice(unitPrice) // ✅ Price → unitPrice (필드명 수정)
                    .build();
            order.addOrderItem(orderItem);
        }

        return OrderResponseDTO.fromEntity(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyOrders(Long memberId, int page, int size) {
        // ✅ findAll().filter() → findByMemberId + 페이징 적용
        return orderRepository.findByMemberId(memberId).stream()
                .skip((long) page * size)
                .limit(size)
                .map(OrderResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(Long memberId, String orderId) {
        // ✅ UUID.fromString() → Long.parseLong() (DB: order_id BIGINT)
        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
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