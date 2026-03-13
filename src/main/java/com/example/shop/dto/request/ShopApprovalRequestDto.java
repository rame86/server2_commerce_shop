package com.example.shop.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ShopApprovalRequestDto {

    @NotNull(message = "굿즈상품 ID는 필수입니다.")
    private Long goodsId;

    @NotNull(message = "신청자 ID는 필수입니다.")
    private Long requesterId;

    @NotBlank(message = "신청자 닉네임은 필수입니다.")
    private String requesterName;

    @NotBlank(message = "상품명은 필수입니다.")
    private String goodsName;

    @NotBlank(message = "상품 타입은 필수입니다.")
    private String goodsType;

    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "재고는 필수입니다.")
    @Min(value = 1, message = "재고는 1 이상이어야 합니다.")
    private Integer stock;

    // 이미지는 컨트롤러에서 MultipartFile로 따로 받아서 처리할 경우 null일 수 있으므로 검증 생략
    private String imageUrl; 
}