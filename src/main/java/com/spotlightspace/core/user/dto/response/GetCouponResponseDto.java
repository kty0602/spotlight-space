package com.spotlightspace.core.user.dto.response;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetCouponResponseDto {

    private long id;
    private LocalDate expiredAt;
    private int discountAmount;

    public static GetCouponResponseDto of(long id, LocalDate expiredAt, int discountAmount) {
        return new GetCouponResponseDto(id, expiredAt, discountAmount);
    }
}
