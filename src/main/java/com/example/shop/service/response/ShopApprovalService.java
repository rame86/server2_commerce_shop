package com.example.shop.service.response;

import java.util.List;

import com.example.shop.admin.dto.ProductApprovalDTO;

public interface ShopApprovalService {
        // ======================== 관리자 승인 관련 ========================
    // 승인할 목록가져오기
    public List<ProductApprovalDTO.Response> getPendingApprovalsWithInactiveProducts();
    // 승인후 상태값 바꾸기
    public void updateProductApprovalStatus(Long productId, boolean isApproved);

}
