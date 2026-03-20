package com.example.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shop.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // ✅ findByStatus(String) → findByIsActive(Boolean) (DB: is_active BOOLEAN)
    List<Product> findByIsActive(Boolean isActive);
}