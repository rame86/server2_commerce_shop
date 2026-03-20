package com.example.shop.dto.response;

import java.math.BigDecimal;

import com.example.shop.entity.Wishlist;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WishlistResponseDTO {

    private Long wishlistId;
    private Long memberId;
    private Long productId;
    private String title;
    private String imageUrl;
    private BigDecimal basePrice; // ✅ price → basePrice (DB: base_price)

    public static WishlistResponseDTO fromEntity(Wishlist wishlist) {
        return WishlistResponseDTO.builder()
                .wishlistId(wishlist.getWishlistId())
                .memberId(wishlist.getMemberId())
                .productId(wishlist.getProduct().getProductId())
                .title(wishlist.getProduct().getTitle())
                .imageUrl(wishlist.getProduct().getImageUrl() != null
                        ? wishlist.getProduct().getImageUrl() : null)
                .basePrice(wishlist.getProduct().getBasePrice()) // ✅ getPrice() → getBasePrice()
                .build();
    }
}