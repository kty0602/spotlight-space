package com.spotlightspace.core.admin.dto.requestdto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminCouponCreateRequestDto {

    @NotNull
    private int discountAmount;

    @NotNull
    private LocalDate expiredAt;

    @NotNull
    private Integer count;

    public static AdminCouponCreateRequestDto of(int discountAmount, LocalDate expiredAt, Integer count) {
        return new AdminCouponCreateRequestDto(discountAmount, expiredAt, count);
    }
}
