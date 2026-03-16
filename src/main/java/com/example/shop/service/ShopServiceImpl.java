package com.example.shop.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.shop.common.exception.BusinessException;
import com.example.shop.common.exception.ErrorCode;
import com.example.shop.dto.request.OrderCreateRequestDTO;
import com.example.shop.dto.request.OrderItemDTO;
import com.example.shop.dto.request.ProductCreateRequestDTO;
import com.example.shop.dto.response.CartResponseDTO;
import com.example.shop.dto.response.OrderResponseDTO;
import com.example.shop.dto.response.ProductResponseDTO;
import com.example.shop.dto.response.WishlistResponseDTO;
import com.example.shop.entity.Cart;
import com.example.shop.entity.CartItem;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.OrderStatus;
import com.example.shop.entity.Product;
import com.example.shop.entity.ProductVariant;
import com.example.shop.entity.Wishlist;
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
    public ProductResponseDTO createProduct(Long memberId, String role, ProductCreateRequestDTO requestDto, MultipartFile imageFile) {
        String imagePath = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                File dest = new File(uploadDir, fileName);
                imageFile.transferTo(dest);
                imagePath = fileName;
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }

        Product product = Product.builder()
                .title(requestDto.getGoodsName())
                .description(requestDto.getDescription())
                .basePrice(requestDto.getPrice()) // ✅ price → basePrice
                .imageUrl(imagePath)
                .isActive(true)                   // ✅ status 제거 → isActive 기본값 true
                .build();

        return ProductResponseDTO.fromEntity(productRepository.save(product));
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
                    .variant(variant)           // ✅ product → variant
                    .quantity(itemDto.getQuantity())
                    .unitPrice(unitPrice)       // ✅ Price → unitPrice (필드명 수정)
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