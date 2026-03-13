package com.example.shop.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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
import com.example.shop.dto.response.CartResponseDto;
import com.example.shop.dto.response.OrderResponseDto;
import com.example.shop.dto.response.ProductResponseDto;
import com.example.shop.dto.response.WishlistResponseDto;
import com.example.shop.entity.Approval;
import com.example.shop.entity.Cart;
import com.example.shop.entity.CartItem;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.OrderStatus;
import com.example.shop.entity.Product;
import com.example.shop.entity.ProductCategory;
import com.example.shop.entity.Wishlist;
import com.example.shop.messaging.producer.ProductMessageProducer;
import com.example.shop.repository.CartItemRepository;
import com.example.shop.repository.CartRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.ShopApprovalRepository;
import com.example.shop.repository.WishlistRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ProductMessageProducer productMessageProducer;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ShopApprovalRepository shopApprovalRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistRepository wishlistRepository;

    @Value("${file.upload.dir:D:/shop/images/}")
    private String uploadPath;

    // ======================== 상품 관련 ========================

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProducts(String goodsType, Long requesterId, int page, int size) {
        if (goodsType != null && !goodsType.isEmpty()) {
            ProductCategory productCategory = ProductCategory.valueOf(goodsType.toUpperCase());
            // status가 String이므로 category + status 조건으로 조회
            return productRepository.findByCategoryAndStatus(productCategory, "ACTIVE").stream()
                    .map(ProductResponseDto::fromEntity)
                    .collect(Collectors.toList());
        }

        return productRepository.findAll().stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))  // PENDING → ACTIVE 수정
                .map(ProductResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

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

        // 1. 이미지 파일 저장
        String savedFileName = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String originalFilename = imageFile.getOriginalFilename();
                savedFileName = UUID.randomUUID().toString() + "_" + originalFilename;
                File saveDir = new File(uploadPath);
                if (!saveDir.exists())
                    saveDir.mkdirs();
                imageFile.transferTo(new File(uploadPath + savedFileName));
            } catch (IOException e) {
                log.error("파일 업로드 실패: {}", e.getMessage());
                throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }

        // 2. 재고 합산
        int totalStock = 0;
        if (requestDto.getVariants() != null) {
            totalStock = requestDto.getVariants().stream()
                    .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                    .sum();
        }

        // 3. goodsType String → ProductCategory enum 변환
        ProductCategory goodsType = ProductCategory.valueOf(requestDto.getGoodsType().toUpperCase());

        // 4. Approval 엔티티 생성 → product_approvals 저장
        Approval approval = Approval.builder()
                .requesterId(memberId)
                .requesterName(requestDto.getRequesterName())
                .goodsName(requestDto.getGoodsName())
                .goodsType(goodsType)
                .description(requestDto.getDescription())
                .price(requestDto.getPrice().intValue())
                .stockQuantity(totalStock)
                .imageUrl(savedFileName)
                .build();

        Approval savedApproval = shopApprovalRepository.save(approval);
        log.info("승인 요청 저장 완료 - approvalId: {}", savedApproval.getApprovalId());

        // 5. RabbitMQ 승인 요청 전송
        productMessageProducer.sendProductCreatedEvent(new ShopApprovalMessage(
                savedApproval.getApprovalId(),
                savedApproval.getRequesterId(),
                savedApproval.getRequesterName(),
                savedApproval.getGoodsName(),
                savedApproval.getGoodsType().name(),
                savedApproval.getDescription(),
                savedApproval.getPrice(),
                totalStock,
                savedApproval.getImageUrl()));

        // 6. 임시 응답 반환 (승인 전이므로 PENDING)
        return ProductResponseDto.builder()
                .category(savedApproval.getGoodsType().name())
                .title(savedApproval.getGoodsName())
                .description(savedApproval.getDescription())
                .price(savedApproval.getPrice() != null ? BigDecimal.valueOf(savedApproval.getPrice()) : null)
                .imageUrl(savedApproval.getImageUrl() != null ? "/images/" + savedApproval.getImageUrl() : null)
                .status("PENDING")
                .build();
    }

    @Override
    @Transactional
    public void deleteProduct(Long memberId, String productId) {
        Product product = productRepository.findById(Long.valueOf(productId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.getSellerId().equals(memberId))
            throw new BusinessException(ErrorCode.FORBIDDEN);

        product.softDelete();
    }

    // ======================== 주문 관련 ========================

    @Override
    @Transactional
    public OrderResponseDto createOrder(Long memberId, OrderCreateRequestDto requestDto) {
        Order order = Order.builder()
                .memberId(memberId)
                .shippingAddress(requestDto.getShippingAddress())
                .recipientName(requestDto.getRecipientName())
                .recipientPhone(requestDto.getRecipientPhone())
                .status(OrderStatus.PENDING)
                .build();

        for (OrderItemDto itemDto : requestDto.getItems()) {
            Product product = productRepository.findById(Long.valueOf(itemDto.getProductId()))
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
        return orderRepository.findByMemberId(memberId).stream()
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
        order.cancel("사용자 요청으로 취소");
        return OrderResponseDto.fromEntity(orderRepository.save(order));
    }

    // ======================== 장바구니 관련 ========================

    @Override
    @Transactional(readOnly = true)
    public CartResponseDto getCart(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));
        return CartResponseDto.fromEntity(cart);
    }

    @Override
    @Transactional
    public CartResponseDto addToCart(Long memberId, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 장바구니 없으면 새로 생성
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> cartRepository.save(Cart.builder().memberId(memberId).build()));

        // 이미 담긴 상품이면 수량만 증가, 없으면 새 아이템 추가
        cartItemRepository.findByCart_CartIdAndProduct_ProductId(cart.getCartId(), productId)
                .ifPresentOrElse(
                        item -> item.updateQuantity(item.getQuantity() + quantity),
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .product(product)
                                    .quantity(quantity)
                                    .build();
                            cart.addItem(newItem);
                        });

        return CartResponseDto.fromEntity(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponseDto removeFromCart(Long memberId, Long cartItemId) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        cart.removeItem(item);
        return CartResponseDto.fromEntity(cartRepository.save(cart));
    }

    // ======================== 찜목록 관련 ========================

    @Override
    @Transactional(readOnly = true)
    public List<WishlistResponseDto> getWishlist(Long memberId) {
        return wishlistRepository.findByMemberId(memberId).stream()
                .map(WishlistResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WishlistResponseDto addToWishlist(Long memberId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 이미 찜한 상품이면 그대로 반환
        return wishlistRepository.findByMemberIdAndProduct_ProductId(memberId, productId)
                .map(WishlistResponseDto::fromEntity)
                .orElseGet(() -> {
                    Wishlist wishlist = Wishlist.builder()
                            .memberId(memberId)
                            .product(product)
                            .build();
                    return WishlistResponseDto.fromEntity(wishlistRepository.save(wishlist));
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