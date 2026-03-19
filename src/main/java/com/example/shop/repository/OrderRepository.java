package com.example.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shop.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> { // ✅ UUID → Long (DB: order_id BIGINT)
    List<Order> findByMemberId(Long memberId);
}