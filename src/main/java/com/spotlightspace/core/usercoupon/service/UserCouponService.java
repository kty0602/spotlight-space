package com.spotlightspace.core.usercoupon.service;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.coupon.domain.Coupon;
import com.spotlightspace.core.coupon.repository.CouponRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.dto.response.GetCouponResponseDto;
import com.spotlightspace.core.user.repository.UserRepository;
import com.spotlightspace.core.usercoupon.domain.UserCoupon;
import com.spotlightspace.core.usercoupon.dto.request.UserCouponIssueRequestDto;
import com.spotlightspace.core.usercoupon.dto.response.UserCouponIssueResponseDto;
import com.spotlightspace.core.usercoupon.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.spotlightspace.common.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCouponService {

    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    public Coupon getCouponOrElseThrow(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));
    }

    public List<GetCouponResponseDto> getUserCouponByUserId(long userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findAllByUserId(userId);

        return userCoupons.stream()
                .map(userCoupon -> GetCouponResponseDto.of(userCoupon.getId(), userCoupon.getCoupon().getExpiredAt(),
                        userCoupon.getCoupon().getDiscountAmount()))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserCouponIssueResponseDto issueCoupon(UserCouponIssueRequestDto requestDto) {
        // 유저 조회
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ApplicationException(USER_NOT_FOUND));

        // 쿠폰 조회
        Coupon coupon = couponRepository.findById(requestDto.getCouponId())
                .orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));

        // 쿠폰 유효성 검사
        if (coupon.isExpired()) {
            throw new ApplicationException(COUPON_EXPIRED);
        }
        if (coupon.getCount() <= 0) {
            throw new ApplicationException(COUPON_COUNT_EXHAUSTED);
        }
        if (coupon.getIsDeleted()) {
            throw new ApplicationException(COUPON_NOT_FOUND);
        }

        // 유저가 이미 해당 쿠폰을 가지고 있는지 검사
        boolean userHasCoupon = userCouponRepository.existsByUserAndCoupon(user, coupon);
        if (userHasCoupon) {
            throw new ApplicationException(COUPON_ALREADY_ISSUED);
        }

        // 쿠폰 발급
        coupon.decreaseCount();
        UserCoupon userCoupon = UserCoupon.of(user, coupon);
        UserCoupon savedUserCoupon = userCouponRepository.save(userCoupon);

        return UserCouponIssueResponseDto.of(savedUserCoupon);
    }

}
