package com.example.shop.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shop.entity.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}