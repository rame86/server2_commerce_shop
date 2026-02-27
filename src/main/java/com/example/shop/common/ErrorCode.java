package com.example.shop.common;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    PRODUCT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "판매 중지된 상품입니다."),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    CANNOT_CANCEL_ORDER(HttpStatus.BAD_REQUEST, "취소할 수 없는 주문입니다. (PENDING 상태만 취소 가능)");

    private final HttpStatus status;
    private final String message;
    ErrorCode(HttpStatus s, String m) { status=s; message=m; }
}
