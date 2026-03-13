package com.example.shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shop.entity.CartItem;

public interface CartitemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCart_CartIdAndProduct_ProductId(Long cartId, Long productId);
}