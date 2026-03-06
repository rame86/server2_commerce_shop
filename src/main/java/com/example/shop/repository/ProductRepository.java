package com.example.shop.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shop.entity.Product;
import com.example.shop.entity.ProductCategory;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * 카테고리별로 검색하되, 판매 중인(isActive=true) 상품만 리스트로 가져옴
     * Spring Data JPA가 메서드 이름만 보고 알아서 SQL 쿼리를 만들어줌
     */
    List<Product> findByCategoryAndIsActiveTrue(ProductCategory category);
}