package com.example.shop.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// DB 스키마의 shop.product_variants 테이블과 매핑되는 상품 옵션(SKU) 엔티티
@Entity
@Table(name = "product_variants", schema = "shop")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductVariant extends BaseTimeEntity {

    @Id // 옵션 고유 ID
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "variant_id")
    private UUID variantId;

    @ManyToOne(fetch = FetchType.LAZY) // N:1 연관관계 설정 (지연 로딩으로 성능 최적화)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 부모 상품 데이터 참조

    @Column(name = "color", length = 50)
    private String color; // 색상 옵션

    @Column(name = "size", length = 50)
    private String size; // 사이즈 옵션

    @Column(name = "additional_price", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal additionalPrice = BigDecimal.ZERO; // 기본가 외 추가로 붙는 금액

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0; // 해당 옵션의 현재 재고 수량

    @Column(name = "sku_code", length = 100, unique = true)
    private String skuCode; // 자체 관리용 재고 식별 코드

    // 연관관계 편의 메서드를 위한 Setter
    public void setProduct(Product product) {
        this.product = product;
    }
}