package com.example.shop.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "products", schema = "shop") // 스키마 명시
@Getter // Lombok 사용 권장
public class Product {

    @Id
    @Column(name = "product_id") // DB의 uuid 컬럼과 매핑
    private UUID productId; // 변수명 낙타표기법 권장

    @Column(name = "seller_id")
    private Long sellerId;

    @Column(name = "seller_type")
    private String sellerType;

    private String category;
    private String title;
    private String description;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    
    // DB에 없는 stock_quantity는 우선 제외하거나 @Transient 처리
}