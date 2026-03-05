package com.example.shop.service;

import com.example.shop.entity.*;
import com.example.shop.repository.*;
import com.example.shop.dto.request.*;
import com.example.shop.dto.response.*;
import com.example.shop.common.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

   @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProducts(String category, Long sellerId, int page, int size) {
        // stream().map(ProductResponseDto::fromEntity) 호출 시 
        // ProductResponseDto에 static 메서드가 있어야 합니다.
        return productRepository.findAll().stream()
                .map(ProductResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(String productId) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponseDto.fromEntity(product);
    }

    @Override
    @Transactional
    public ProductResponseDto createProduct(Long memberId, String role, ProductCreateRequestDto requestDto) {
        // Product.builder() 사용 시 Product 엔티티에 @Builder가 있어야 합니다.
        Product product = Product.builder()
                .sellerId(memberId)
                .title(requestDto.getProductName())
                .description(requestDto.getProductDetail())
                .price(requestDto.getPrice())
                .isActive(true)
                .build();
        
        Product savedProduct = productRepository.save(product);
        return ProductResponseDto.fromEntity(savedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long memberId, String productId) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getSellerId().equals(memberId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        product.softDelete();
    }

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
        // 실제 운영 환경에서는 Repository에 findByMemberId를 만들어 호출해야 함
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
        if (!order.getMemberId().equals(memberId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        return OrderResponseDto.fromEntity(order);
    }

    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long memberId, String orderId) {
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getMemberId().equals(memberId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        
        // 간단한 취소 로직 (엔티티에 status 변경 로직 필요)
        return OrderResponseDto.fromEntity(order);
    }
}