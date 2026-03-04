package com.example.shop.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shop.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    // 기본 CRUD 메서드 자동 제공
}