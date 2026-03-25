package com.example.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.admin.dto.ProductApprovalDTO;
import com.example.shop.service.ShopService;
import com.example.shop.service.response.ShopApprovalService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductApprovalService {

    private final ShopApprovalService shopApprovalService;

    // ShopService를 호출하여 PENDING 목록 반환
    public List<ProductApprovalDTO.Response> getPendingApprovals() {
        List<ProductApprovalDTO.Response> list = shopApprovalService.getPendingApprovalsWithInactiveProducts();
        return list;
    }

    // ShopService를 호출하여 DB 상태 업데이트
    public void processApproval(ProductApprovalDTO.Request request) {
        shopApprovalService.updateProductApprovalStatus(request.getProductId(), request.isApproved());
    }
}