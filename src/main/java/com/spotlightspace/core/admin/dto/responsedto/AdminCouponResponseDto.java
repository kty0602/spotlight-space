package com.spotlightspace.core.admin.dto.responsedto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminCouponResponseDto {

    private Long id;
    private int discountAmount;
    private LocalDate expiredAt;
    private String code;
    private boolean isUsed;

    public static AdminCouponResponseDto of(Long id, int discountAmount, LocalDate expiredAt, String code, Boolean isUsed) {
        return new AdminCouponResponseDto(id, discountAmount, expiredAt, code, isUsed);
    }
}
