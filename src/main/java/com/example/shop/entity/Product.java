package com.example.shop.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    // DB: seller_id (bigint)
    @Column(name = "seller_id")
    private Long sellerId;

    // DB: seller_type (enum)
    @Enumerated(EnumType.STRING)
    @Column(name = "seller_type", nullable = false)
    private SellerType sellerType;

    // DB: category (enum product_category)
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ProductCategory category;

    // DB: title (varchar 255)
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    // DB: description (text)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // DB: image_url (varchar 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // DB: price (numeric 15,2)
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    // DB: status (boolean, default true)
    @Column(name = "status")
    @Builder.Default
    private Boolean status = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    public void addVariant(ProductVariant variant) {
        this.variants.add(variant);
        variant.setProduct(this);
    }

    public void softDelete() {
        this.status = false;
    }
}