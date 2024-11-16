package com.spotlightspace.core.data;

import com.spotlightspace.core.user.dto.response.GetCouponResponseDto;
import java.time.LocalDate;

public class UserCouponData {

    public static GetCouponResponseDto getCouponResponse() {
        GetCouponResponseDto coupon = GetCouponResponseDto.of(1L, LocalDate.now(), 10);
        return coupon;
    }
}
