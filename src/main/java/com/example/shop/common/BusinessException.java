package com.example.shop.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    public BusinessException(ErrorCode e) { super(e.getMessage()); errorCode=e; }
}
