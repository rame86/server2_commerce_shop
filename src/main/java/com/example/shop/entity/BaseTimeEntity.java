package com.example.shop.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

// 공통 생성/수정일자 관리 엔티티 (DB의 created_at, updated_at 자동 매핑)
@Getter
@MappedSuperclass // 테이블로 생성되지 않고 자식 엔티티에게 매핑 정보만 제공함
@EntityListeners(AuditingEntityListener.class) // JPA Auditing(자동 시간 갱신) 활성화
public abstract class BaseTimeEntity {

    @CreatedDate // 데이터 생성 시점의 시간 자동 저장
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // 데이터 수정 시점의 시간 자동 갱신
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}