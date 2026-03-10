package com.example.shop.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shop.entity.Product;
import com.example.shop.entity.ProductCategory;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    // goodsType 기준으로 활성 상품 조회
    List<Product> findByGoodsTypeAndIsActiveTrue(ProductCategory goodsType);
}