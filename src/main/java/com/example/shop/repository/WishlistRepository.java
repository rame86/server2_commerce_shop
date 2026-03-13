package com.example.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shop.entity.Wishlist;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByMemberId(Long memberId);
    Optional<Wishlist> findByMemberIdAndProduct_ProductId(Long memberId, Long productId);
    boolean existsByMemberIdAndProduct_ProductId(Long memberId, Long productId);
}