
package com.example.shop.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shop.entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    // 특정 상품의 모든 옵션 조회
    java.util.List<ProductVariant> findByProduct_ProductId(Long productId);
}