package com.example.shop.entity;

public enum ApprovalStatus {
    PENDING,   // 대기
    CONFIRMED, // 승인됨
    FAILED    // 거절됨 (컨트롤러 요구사항 철자에 맞춤)
}