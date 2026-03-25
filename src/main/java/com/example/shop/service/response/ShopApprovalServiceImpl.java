package com.example.shop.service.response;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shop.admin.dto.ProductApprovalDTO;
import com.example.shop.common.exception.BusinessException;
import com.example.shop.common.exception.ErrorCode;
import com.example.shop.entity.Approval;
import com.example.shop.entity.Product;
import com.example.shop.entity.enums.ApprovalStatus;
import com.example.shop.repository.ApprovalRepository;
import com.example.shop.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShopApprovalServiceImpl implements ShopApprovalService {

    private final ApprovalRepository approvalRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductApprovalDTO.Response> getPendingApprovalsWithInactiveProducts() {
        // [수정] N+1 문제 해결을 위해 Repository에 정의된 JPQL Join 쿼리 사용
        List<Approval> approvals = approvalRepository.findPendingApprovalsWithInactiveProducts(ApprovalStatus.PENDING);
        
        return approvals.stream()
                .map(approval -> ProductApprovalDTO.Response.builder()
                        .approvalId(approval.getApprovalId())
                        .productId(approval.getProductId())
                        .requesterName(approval.getRequesterName())
                        .goodsName(approval.getGoodsName())
                        .status(approval.getStatus().name())
                        .isActive(false)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateProductApprovalStatus(Long productId, boolean isApproved) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Approval approval = approvalRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_NOT_FOUND));

        if (isApproved) {
            approval.updateStatus(ApprovalStatus.CONFIRMED, "승인 완료");
            // [수정] setIsActive() 대신 Product 엔티티에 정의된 비즈니스 메서드 사용
            product.updateActiveStatus(true);
            log.info(">>>> [상품 승인 완료] Product ID: {}", productId);
        } else {
            approval.updateStatus(ApprovalStatus.FAILED, "승인 거절. 관리자에게 문의하세요.");
            // [수정] setIsActive() 대신 Product 엔티티에 정의된 비즈니스 메서드 사용
            product.updateActiveStatus(false);
            log.info(">>>> [상품 승인 거절] Product ID: {}", productId);
        }
    }
}