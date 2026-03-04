package com.example.shop.common.exception; 

import lombok.Getter; // 1. ErrorCode 위치에 따른 import 추가

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public BusinessException(ErrorCode e) { 
        super(e.getMessage()); 
        this.errorCode = e; 
    }
}