package com.example.shop.common.exception;

import org.springframework.http.ResponseEntity; // 1. BusinessException 위치에 따른 import 추가
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.shop.common.util.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        // e.getErrorCode()를 통해 ErrorCode 객체에 접근하여 상태값 반환
        return ResponseEntity.status(e.getErrorCode().getStatus())
                             .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(ApiResponse.error("입력값 검증 실패"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled Exception: ", e); // 보안을 위해 에러 로그는 서버에만 남김
        return ResponseEntity.internalServerError().body(ApiResponse.error("서버 오류가 발생했습니다."));
    }
}