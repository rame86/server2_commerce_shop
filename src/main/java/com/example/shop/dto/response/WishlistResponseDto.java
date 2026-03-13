package com.example.shop.dto.response;

import java.math.BigDecimal;

import com.example.shop.entity.Wishlist;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WishlistResponseDto {

    private Long wishlistId;
    private Long memberId;
    private Long productId;
    private String title;
    private String imageUrl;
    private BigDecimal price;

    public static WishlistResponseDto fromEntity(Wishlist wishlist) {
        return WishlistResponseDto.builder()
                .wishlistId(wishlist.getWishlistId())
                .memberId(wishlist.getMemberId())
                .productId(wishlist.getProduct().getProductId())
                .title(wishlist.getProduct().getTitle())
                .imageUrl(wishlist.getProduct().getImageUrl() != null ? "/images/" + wishlist.getProduct().getImageUrl() : null)
                .price(wishlist.getProduct().getPrice())
                .build();
    }
}