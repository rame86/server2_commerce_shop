package com.example.shop.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import com.example.shop.entity.enums.ProductCategory;
import com.example.shop.entity.enums.SellerType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products", schema = "shop")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    // 판매자 ID (Member 서비스 참조)
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    // 판매자 유형 (아티스트 / 일반유저)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "seller_type", nullable = false)
    private SellerType sellerType;

    // 상품 분류
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "category", nullable = false)
    private ProductCategory category;

    // 상품명
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    // 상품 상세 설명
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 대표 이미지 URL
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // ✅ base_price 로 컬럼명 변경 (DB 기준)
    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;
    
    @Column(name = "item_category")
    private String itemCategory;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "size", length = 50)
    private String size;

    // ✅ status(String) → is_active(Boolean) 으로 변경 (DB 기준 Soft Delete)
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // 상품 승인 시 (isActive false -> true) 업데이트 로직
    public void activateProduct() {
        this.isActive = true;
    }
}