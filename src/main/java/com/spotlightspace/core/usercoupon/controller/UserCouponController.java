package com.spotlightspace.core.usercoupon.controller;

import com.spotlightspace.core.usercoupon.dto.request.UserCouponIssueRequestDto;
import com.spotlightspace.core.usercoupon.dto.response.UserCouponIssueResponseDto;
import com.spotlightspace.core.usercoupon.service.UserCouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-coupons")
public class UserCouponController {

    private final UserCouponService userCouponService;

    /**
     *
     * @param requestDto 쿠폰 발급에 필요한 정보가 포함된 요청 객체
     * @return 발급된 쿠폰에 대한 세부 정보를 담고 있는 ResponseEntity
     */
    @PostMapping("/issue")
    public ResponseEntity<UserCouponIssueResponseDto> issueCoupon(@Valid @RequestBody UserCouponIssueRequestDto requestDto) {
        UserCouponIssueResponseDto responseDto = userCouponService.issueCoupon(requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
