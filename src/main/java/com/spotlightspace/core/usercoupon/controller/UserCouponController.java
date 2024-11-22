package com.spotlightspace.core.usercoupon.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.usercoupon.dto.request.UserCouponIssueRequestDto;
import com.spotlightspace.core.usercoupon.dto.response.UserCouponIssueResponseDto;
import com.spotlightspace.core.usercoupon.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-coupons")
public class UserCouponController {

    private final UserCouponService userCouponService;

    /**
     * 기본 쿠폰 발급
     *
     * @param authUser 인증된 사용자 정보
     * @param couponId 발급받을 쿠폰의 ID
     * @return 발급된 쿠폰에 대한 응답 객체
     */
    @PostMapping("/issue/basic/{couponId}")
    public ResponseEntity<UserCouponIssueResponseDto> issueCouponBasic(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long couponId
    ) {
        UserCouponIssueRequestDto requestDto = UserCouponIssueRequestDto.of(authUser.getUserId(), couponId);
        UserCouponIssueResponseDto responseDto = userCouponService.issueCouponBasic(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 비관적 락 + Redis 대기열을 사용한 쿠폰 발급
     *
     * @param authUser 인증된 사용자 정보
     * @param couponId 발급받을 쿠폰의 ID
     * @return 발급된 쿠폰에 대한 응답 객체
     */
    @PostMapping("/issue/pessimistic/{couponId}")
    public ResponseEntity<UserCouponIssueResponseDto> issueCouponWithPessimisticLockAndQueue(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long couponId
    ) {
        UserCouponIssueRequestDto requestDto = UserCouponIssueRequestDto.of(authUser.getUserId(), couponId);
        UserCouponIssueResponseDto responseDto = userCouponService.issueCouponWithPessimisticLockAndQueue(requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
