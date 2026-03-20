package com.example.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.shop.entity.Approval;
import com.example.shop.entity.ApprovalStatus;

/**
 * 상품 등록 승인 요청 처리를 위한 Repository
 */
@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    // 특정 신청자가 요청한 승인 목록 조회
    List<Approval> findByRequesterId(Long requesterId);

    // 2. ✅ 추가: 관리자 페이지용 - 특정 상태(예: PENDING)의 요청을 최신순으로 조회
    // 메소드 이름을 통해 'status' 필드로 필터링하고 'createdAt' 필드 기준으로 내림차순 정렬합니다.
    List<Approval> findByStatusOrderByCreatedAtDesc(ApprovalStatus status);

    // 특정 상품 ID와 연결된 승인 요청이 있는지 확인 (수정 요청 시 활용)
    List<Approval> findByProductId(Long productId);


    
}