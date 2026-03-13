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
    private BigDecimal price;

    public static WishlistResponseDTO fromEntity(Wishlist wishlist) {
        return WishlistResponseDTO.builder()
                .wishlistId(wishlist.getWishlistId())
                .memberId(wishlist.getMemberId())
                .productId(wishlist.getProduct().getProductId())
                .title(wishlist.getProduct().getTitle())
                // .imageUrl(wishlist.getProduct().getImageUrl() != null ? "/images/" + wishlist.getProduct().getImageUrl() : null) //배포시 해당 내용으로 변경
                .imageUrl(wishlist.getProduct().getImageUrl() != null ? "" + wishlist.getProduct().getImageUrl() : null)
                .price(wishlist.getProduct().getPrice())
                .build();
    }
}