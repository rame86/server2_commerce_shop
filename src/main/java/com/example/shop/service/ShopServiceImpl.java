package com.example.shop.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.dto.request.OrderCreateRequestDto;
import com.example.shop.dto.request.OrderItemDto;
import com.example.shop.dto.request.ProductCreateRequestDto;
import com.example.shop.dto.response.OrderResponseDto;
import com.example.shop.dto.response.ProductResponseDto;
import com.example.shop.repository.ShopMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopMapper shopMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProducts(String category, Long sellerId, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("category", category);
        params.put("sellerId", sellerId);
        params.put("offset", page * size);
        params.put("limit", size);
        return shopMapper.findProducts(params);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(String productId) {
        ProductResponseDto product = shopMapper.findProductById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    @Override
    @Transactional
    public ProductResponseDto createProduct(Long memberId, String role, ProductCreateRequestDto requestDto) {
        Map<String, Object> params = new HashMap<>();
        params.put("sellerId", memberId);
        params.put("sellerType", "ARTIST".equals(role) ? "ARTIST" : "USER");
        params.put("category", requestDto.getCategory());
        params.put("title", requestDto.getTitle());
        params.put("description", requestDto.getDescription());
        params.put("price", requestDto.getPrice());
        params.put("stockQuantity", requestDto.getStockQuantity());

        shopMapper.insertProduct(params);
        return shopMapper.findProductById((String) params.get("productId"));
    }

    @Override
    @Transactional
    public void deleteProduct(Long memberId, String productId) {
        ProductResponseDto product = getProduct(productId);
        if (!product.getSellerId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        shopMapper.softDeleteProduct(productId);
    }

    @Override
    @Transactional
    public OrderResponseDto createOrder(Long memberId, OrderCreateRequestDto requestDto) {
        // 총 금액 계산 및 재고 확인
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDto item : requestDto.getItems()) {
            ProductResponseDto product = getProduct(item.getProductId());
            if (!product.getIsActive()) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK);
            }
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 주문 생성
        Map<String, Object> orderParams = new HashMap<>();
        orderParams.put("memberId", memberId);
        orderParams.put("totalAmount", totalAmount);
        orderParams.put("shippingAddress", requestDto.getShippingAddress());
        shopMapper.insertOrder(orderParams);
        String orderId = (String) orderParams.get("orderId");

        // 주문 상세 및 재고 차감
        for (OrderItemDto item : requestDto.getItems()) {
            ProductResponseDto product = shopMapper.findProductById(item.getProductId());

            Map<String, Object> itemParams = new HashMap<>();
            itemParams.put("orderId", orderId);
            itemParams.put("productId", item.getProductId());
            itemParams.put("quantity", item.getQuantity());
            itemParams.put("unitPrice", product.getPrice()); // 주문 시점 가격 스냅샷
            shopMapper.insertOrderItem(itemParams);

            shopMapper.decreaseStock(item.getProductId(), item.getQuantity());
        }

        return shopMapper.findOrderById(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getMyOrders(Long memberId, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", memberId);
        params.put("offset", page * size);
        params.put("limit", size);
        return shopMapper.findOrdersByMemberId(params);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrder(Long memberId, String orderId) {
        OrderResponseDto order = shopMapper.findOrderById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return order;
    }

    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long memberId, String orderId) {
        OrderResponseDto order = getOrder(memberId, orderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_ORDER);
        }
        shopMapper.updateOrderStatus(orderId, "CANCELLED");
        return shopMapper.findOrderById(orderId);
    }
}
