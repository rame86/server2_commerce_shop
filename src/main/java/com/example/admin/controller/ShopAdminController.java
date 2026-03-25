package com.example.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.admin.dto.ProductApprovalDTO;
import com.example.admin.service.ProductApprovalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ShopAdminController {

    private final ProductApprovalService productApprovalService;

    @GetMapping("/approval-list")
    public ResponseEntity<List<ProductApprovalDTO.Response>> getApprovalList() {
        // PENDING 상태이면서 상품이 비활성(is_active=false)인 목록 조회
        List<ProductApprovalDTO.Response> approvalList = productApprovalService.getPendingApprovals();
        return ResponseEntity.ok(approvalList);
    }

    @PostMapping("/product-approval")
    public ResponseEntity<String> decideApproval(@RequestBody ProductApprovalDTO.Request request) {
        // DTO의 boolean 값에 따라 승인(CONFIRMED) 또는 거절(CANCELED) 처리
        productApprovalService.processApproval(request);
        
        String resultMessage = request.isApproved() ? "승인 처리되었습니다." : "거절 처리되었습니다.";
        return ResponseEntity.ok(resultMessage);
    }
}