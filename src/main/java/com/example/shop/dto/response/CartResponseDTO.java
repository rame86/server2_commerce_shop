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
public class CartResponseDTO {

    private Long cartId;
    private Long memberId;
    private List<CartItemDto> items;
    private BigDecimal totalPrice;

    @Getter
    @Builder
    public static class CartItemDto {
        private Long cartItemId;
        private Long productId;
        private Long artistId; // 추가: 아티스트 식별자
        private String title;
        private String imageUrl;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal subtotal;

        public static CartItemDto fromEntity(CartItem item) {
            BigDecimal price = item.getProduct().getBasePrice(); // ✅ getPrice() → getBasePrice()
            return CartItemDto.builder()
                    .cartItemId(item.getCartItemId())
                    .productId(item.getProduct().getProductId())
                    .artistId(item.getProduct().getArtistId()) // 추가
                    .title(item.getProduct().getTitle())
                    .imageUrl(item.getProduct().getImageUrl())
                    .unitPrice(price)
                    .quantity(item.getQuantity())
                    .subtotal(price.multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();
        }
    }

    public static CartResponseDTO fromEntity(Cart cart) {
        List<CartItemDto> items = cart.getCartItems().stream()
                .map(CartItemDto::fromEntity)
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponseDTO.builder()
                .cartId(cart.getCartId())
                .memberId(cart.getMemberId())
                .items(items)
                .totalPrice(total)
                .build();
    }
}