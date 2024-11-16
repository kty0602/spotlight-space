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
public class AdminCouponUpdateRequestDto {

    @NotNull
    private int discountAmount;

    @NotNull
    private LocalDate expiredAt;

    public static AdminCouponUpdateRequestDto of(int discountAmount, LocalDate expiredAt) {
        return new AdminCouponUpdateRequestDto(discountAmount, expiredAt);
    }
}
