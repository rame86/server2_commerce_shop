package com.example.shop.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// DB 스키마의 shop.products 테이블과 매핑되는 상품 기본 엔티티
@Entity
@Table(name = "products", schema = "shop")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product extends BaseTimeEntity { // BaseTimeEntity 상속으로 생성/수정시간 자동 처리

    @Id // 상품 고유 ID (PostgreSQL의 gen_random_uuid()와 호환되도록 UUID 설정)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id", columnDefinition = "uuid")
    private UUID productId;
    
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "category", nullable = false)
    private ProductCategory category; // 상품 분류

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "seller_type", nullable = false)
    private SellerType sellerType; // 추가됨: 스키마의 seller_type (아티스트/유저 구분)

    @Column(name = "seller_id", nullable = false)
    private Long sellerId; // 판매자 ID

    @Column(name = "title", nullable = false)
    private String title; // 기존 DTO의 productName이 저장되는 상품명

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 기존 DTO의 productDetail이 저장되는 상세 설명

    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price; // DB 스키마 base_price 컬럼과 매핑

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true; // 판매 활성화 여부 (Soft Delete를 위한 플래그)

    @Column(name = "image_url", length = 500)
    private String imageUrl; // 이미지 저장 경로

    // 추가됨: 상품 상세 옵션(ProductVariant) 리스트 양방향 연관관계
    // cascade = CascadeType.ALL 설정으로 Product 저장 시 하위 Variant들도 함께 DB에 INSERT됨
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    // 옵션 리스트에 데이터를 안전하게 추가하기 위한 연관관계 편의 메서드
    public void addVariant(ProductVariant variant) {
        this.variants.add(variant);
        variant.setProduct(this);
    }

    // 데이터 완전 삭제(Hard Delete) 대신 비활성화 처리하는 메서드
    public void softDelete() {
        this.isActive = false;
    }
}