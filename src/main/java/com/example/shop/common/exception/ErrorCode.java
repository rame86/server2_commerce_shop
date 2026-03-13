package com.example.shop.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 500: 서버 내부에서 처리 못 한 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    // 403: 로그인 권한이나 접근 권한 없을 때
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    // 404: 없는 데이터 찾을 때
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    PRODUCT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "판매 중지된 상품입니다."),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    CANNOT_CANCEL_ORDER(HttpStatus.BAD_REQUEST, "취소할 수 없는 주문입니다."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 상품을 찾을 수 없습니다."),
    WISHLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "찜목록 항목을 찾을 수 없습니다."),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus s, String m) {
        this.status = s;
        this.message = m;
    }
}