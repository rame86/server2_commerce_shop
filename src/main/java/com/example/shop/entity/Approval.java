package com.example.shop.entity;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import com.example.shop.entity.enums.ProductCategory;

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
public class Approval extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_id")
    private Long approvalId;

    // ✅ UUID → Long 으로 변경 (DB: product_id BIGINT)
    // 수정 요청 시 기존 상품 참조, 신규 등록은 null
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "requester_name", nullable = false, length = 100)
    private String requesterName;

    @Column(name = "goods_name", nullable = false, length = 255)
    private String goodsName;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "goods_type", nullable = false, length = 50)
    private ProductCategory goodsType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ✅ Integer → BigDecimal (DB: NUMERIC(15,2))
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal price;

    // ✅ 신규 추가 (DB: color VARCHAR(50))
    @Column(name = "color", length = 50)
    private String color;

    // ✅ 신규 추가 (DB: size VARCHAR(50))
    @Column(name = "size", length = 50)
    private String size;

    @Column(name = "item_category", length = 100)
    private String itemCategory;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // ✅ ApprovalStatus enum 사용 (DB: shop.approval_status)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "status", nullable = false, length = 20)
    private ApprovalStatus status;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Builder
    public Approval(Long requesterId, String requesterName,
            String goodsName, ProductCategory goodsType, String description,
            java.math.BigDecimal price, String color, String size, String itemCategory,
            Integer stockQuantity, String imageUrl) {
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.goodsName = goodsName;
        this.goodsType = goodsType;
        this.description = description;
        this.price = price;
        this.color = color;
        this.size = size;
        this.itemCategory = itemCategory;
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0;
        this.imageUrl = imageUrl;
        this.productId = null;
        this.status = (status != null) ? status : ApprovalStatus.PENDING; // 2. 전달받은 값 사용
    }

    // 승인/반려 상태 변경
    public void updateStatus(ApprovalStatus status, String rejectionReason) {
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    // 승인 후 생성된 product 연결
    public void linkProduct(Long productId) {
        this.productId = productId;
    }
}