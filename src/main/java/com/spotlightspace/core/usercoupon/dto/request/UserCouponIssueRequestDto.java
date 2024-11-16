package com.spotlightspace.core.usercoupon.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCouponIssueRequestDto {
    private Long userId;
    private Long couponId;

    public static UserCouponIssueRequestDto of(Long userId, Long couponId) {
        return new UserCouponIssueRequestDto(userId, couponId);
    }
}
