package com.example.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shop.entity.Approval;

public interface ShopApprovalRepository extends JpaRepository<Approval, Long> {
}