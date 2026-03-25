package com.example.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.shop.entity.Approval;
import com.example.shop.entity.enums.ApprovalStatus;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    // [조회] 특정 상품 ID에 대한 승인 정보 조회 (단건)
    Optional<Approval> findByProductId(Long productId);

    // [최적화] 상태가 PENDING(대기)이면서, Product의 isActive가 false인 데이터만 조인하여 한 번에 조회
    // N+1 쿼리 문제를 방지하고 DB 레벨에서 필터링하여 성능을 최적화합니다.
    @Query("SELECT a FROM Approval a " +
           "JOIN Product p ON a.productId = p.productId " +
           "WHERE a.status = :status AND p.isActive = false " +
           "ORDER BY a.createdAt DESC")
    List<Approval> findPendingApprovalsWithInactiveProducts(@Param("status") ApprovalStatus status);

    // [기본] 상태값 기반 최신순 조회 (필요시 사용)
    List<Approval> findByStatusOrderByCreatedAtDesc(ApprovalStatus status);
}