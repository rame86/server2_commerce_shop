package com.example.shop.entity;

// DB의 goods_type 컬럼과 매핑되는 상품 카테고리 enum
public enum ProductCategory {
    OFFICIAL,    // 공식 굿즈
    UNOFFICIAL,  // 팬메이드 굿즈
    SECONDHAND;   // 중고 굿즈
}