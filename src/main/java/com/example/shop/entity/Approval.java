package com.example.shop.entity;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "product_approvals", schema = "shop")
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_id")
    private Long approvalId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "requester_name", nullable = false, length = 100)
    private String requesterName;

    @Column(name = "goods_name", nullable = false, length = 255)
    private String goodsName;

    @Enumerated(EnumType.STRING)
    @Column(name = "goods_type", nullable = false)
    private ProductCategory goodsType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;  // @Builder.Default 제거

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "status")
    private String status;  // @Builder.Default 제거

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Builder
    public Approval(Long requesterId, String requesterName,
            String goodsName, ProductCategory goodsType, String description,
            Integer price, Integer stockQuantity, String imageUrl) {
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.goodsName = goodsName;
        this.goodsType = goodsType;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0;
        this.imageUrl = imageUrl;
        this.productId = null;
        this.status = "PENDING";
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    public void updateStatus(String status, String rejectionReason) {
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.updatedAt = ZonedDateTime.now();
    }

    public void linkProduct(UUID productId) {
        this.productId = productId;
        this.updatedAt = ZonedDateTime.now();
    }
}