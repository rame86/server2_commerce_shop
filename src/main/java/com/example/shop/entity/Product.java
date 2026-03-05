package com.example.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products", schema = "shop")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "product_id", columnDefinition = "uuid")
    private UUID productId;

    private Long sellerId;
    private String title;       // requestDto.getProductName()과 매핑
    private String description; // requestDto.getProductDetail()과 매핑

    @Column(name = "base_price")
    private BigDecimal price;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public void softDelete() {
        this.isActive = false;
    }
}