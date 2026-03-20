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

@Entity
@Table(name = "product_variants", schema = "shop")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductVariant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "variant_id", columnDefinition = "uuid")
    private UUID variantId;

    // 부모 상품 참조 (지연 로딩)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 색상 옵션
    @Column(name = "color", length = 50)
    private String color;

    // 사이즈 옵션
    @Column(name = "size", length = 50)
    private String size;

    // 추가 금액
    @Column(name = "additional_price", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal additionalPrice = BigDecimal.ZERO;

    // 재고 수량
    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    // 재고 식별 코드
    @Column(name = "sku_code", length = 100, unique = true)
    private String skuCode;

    // 연관관계 편의 메서드
    public void setProduct(Product product) {
        this.product = product;
    }
}