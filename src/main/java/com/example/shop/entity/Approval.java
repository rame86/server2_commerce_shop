package com.example.shop.entity;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "shop_approvals", schema = "shop")
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_id")
    private Long approvalId;

    @Column(name = "goods_id")
    private Long goodsId;

    @Column(name = "requester_id")
    private Long requesterId;

    @Column(name = "requester_name", length = 100)
    private String requesterName;

    @Column(name = "goods_name", length = 255)
    private String goodsName;

    @Column(name = "goods_type", length = 50)
    private String goodsType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Integer price;

    @Column
    private Integer stock;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Builder
    public Approval(Long goodsId, Long requesterId, String requesterName,
            String goodsName, String goodsType, String description,
            Integer price, Integer stock, String imageUrl) {
        this.goodsId = goodsId;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.goodsName = goodsName;
        this.goodsType = goodsType;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.status = "PENDING";
        this.createdAt = ZonedDateTime.now();
    }

    public void updateStatus(String status, String rejectionReason) {
        this.status = status;
        this.rejectionReason = rejectionReason;
    }
}