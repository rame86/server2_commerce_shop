package com.example.shop.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.example.shop.entity.Cart;
import com.example.shop.entity.CartItem;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartResponseDto {

    private Long cartId;
    private Long memberId;
    private List<CartItemDto> items;
    private BigDecimal totalPrice;

    @Getter
    @Builder
    public static class CartItemDto {
        private Long cartItemId;
        private Long productId;
        private String title;
        private String imageUrl;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal subtotal;

        public static CartItemDto fromEntity(CartItem item) {
            return CartItemDto.builder()
                    .cartItemId(item.getCartItemId())
                    .productId(item.getProduct().getProductId())
                    .title(item.getProduct().getTitle())
                    .imageUrl(item.getProduct().getImageUrl() != null ? "/images/" + item.getProduct().getImageUrl() : null)
                    .unitPrice(item.getProduct().getPrice())
                    .quantity(item.getQuantity())
                    .subtotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();
        }
    }

    public static CartResponseDto fromEntity(Cart cart) {
        List<CartItemDto> items = cart.getCartItems().stream()
                .map(CartItemDto::fromEntity)
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponseDto.builder()
                .cartId(cart.getCartId())
                .memberId(cart.getMemberId())
                .items(items)
                .totalPrice(total)
                .build();
    }
}