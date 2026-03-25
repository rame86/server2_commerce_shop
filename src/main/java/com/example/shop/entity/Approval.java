package com.example.shop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.shop.entity.enums.ProductCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_approvals", schema = "shop")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_id")
    private Long approvalId;

    @Column(name = "product_id")
    private Long productId;

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

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "size", length = 50)
    private String size;

    @Builder.Default
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Builder.Default
    @Column(name = "shipping_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "item_category", length = 100)
    private String itemCategory;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // JPA Auditing 대신 PrePersist/PreUpdate 사용 (DB 기본값과 동기화)
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ================= 비즈니스 로직 메서드 =================
    
    // 상품 ID 연결
    public void linkProduct(Long productId) {
        this.productId = productId;
    }

    // 승인 상태 및 거절 사유 업데이트
    public void updateStatus(ApprovalStatus status, String rejectionReason) {
        this.status = status;
        if (status == ApprovalStatus.FAILED) {
            this.rejectionReason = rejectionReason;
        }
    }
}