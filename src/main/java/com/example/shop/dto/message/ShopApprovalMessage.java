package com.example.shop.dto.message;

public record ShopApprovalMessage(
    Long goodsId,
    Long requesterId,
    String requesterName,
    String goodsName,
    String goodsType,
    String description,
    Integer price,
    Integer stock,
    String imageUrl
) {}