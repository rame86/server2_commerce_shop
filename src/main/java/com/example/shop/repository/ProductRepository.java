package com.example.shop.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shop.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    // 필요한 경우 findByIsActiveTrue() 등 추가 가능
}