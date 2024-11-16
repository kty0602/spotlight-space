package com.spotlightspace.core.usercoupon.dto.response;

import com.spotlightspace.core.usercoupon.domain.UserCoupon;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCouponIssueResponseDto {
    private Long userCouponId;
    private Long userId;
    private Long couponId;
    private boolean isUsed;

    public static UserCouponIssueResponseDto of(UserCoupon userCoupon) {
        return new UserCouponIssueResponseDto(
                userCoupon.getId(),
                userCoupon.getUser().getId(),
                userCoupon.getCoupon().getId(),
                userCoupon.isUsed()
        );
    }
}
