package com.spotlightspace.core.usercoupon.service;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.coupon.domain.Coupon;
import com.spotlightspace.core.coupon.repository.CouponRepository;
import com.spotlightspace.core.event.service.RedissonLockService;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.dto.response.GetCouponResponseDto;
import com.spotlightspace.core.user.repository.UserRepository;
import com.spotlightspace.core.usercoupon.domain.UserCoupon;
import com.spotlightspace.core.usercoupon.dto.request.UserCouponIssueRequestDto;
import com.spotlightspace.core.usercoupon.dto.response.UserCouponIssueResponseDto;
import com.spotlightspace.core.usercoupon.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.spotlightspace.common.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCouponService {

    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final RedissonLockService redissonLockService;
    private static final String COUPON_LOCK_KEY = "lock:coupon:";

    /**
     * 기본 쿠폰 발급 로직
     */
    @Transactional
    public UserCouponIssueResponseDto issueCouponBasic(UserCouponIssueRequestDto requestDto) {
        User user = userRepository.findByIdOrElseThrow(requestDto.getUserId());
        Coupon coupon = couponRepository.findByIdOrElseThrow(requestDto.getCouponId());

        validateCoupon(coupon);
        validateUserHasCoupon(user, coupon);

        coupon.decreaseCount();
        UserCoupon userCoupon = UserCoupon.of(user, coupon);
        UserCoupon savedUserCoupon = userCouponRepository.save(userCoupon);

        return UserCouponIssueResponseDto.from(savedUserCoupon);
    }

    /**
     * 비관적 락 + Redis 대기열 + Redis 분산 락을 사용한 쿠폰 발급 로직
     */
    @Transactional
    public UserCouponIssueResponseDto issueCouponWithPessimisticLockAndQueue(UserCouponIssueRequestDto requestDto) {
        String lockKey = COUPON_LOCK_KEY + requestDto.getCouponId();
        RLock redisLock = redissonLockService.lock(lockKey);
        boolean isQueued;

        try {
            // Redis 대기열 대기
            isQueued = redisLock.tryLock(3, 5, TimeUnit.SECONDS);

            if (!isQueued) {
                throw new ApplicationException(LOCK_NOT_ACQUIRED);
            }

            // 비관적 락으로 DB 접근 제어
            User user = userRepository.findByIdOrElseThrow(requestDto.getUserId());
            Coupon coupon = couponRepository.findByIdWithPessimisticLock(requestDto.getCouponId())
                    .orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));

            validateCoupon(coupon);
            validateUserHasCoupon(user, coupon);

            coupon.decreaseCount();
            UserCoupon userCoupon = UserCoupon.of(user, coupon);
            UserCoupon savedUserCoupon = userCouponRepository.save(userCoupon);

            return UserCouponIssueResponseDto.from(savedUserCoupon);

        } catch (InterruptedException e) {
            throw new ApplicationException(LOCK_NOT_ACQUIRED);
        } finally {
            // Redis 락 해제
            if (redisLock.isLocked() && redisLock.isHeldByCurrentThread()) {
                redisLock.unlock();
            }
        }
    }

    /**
     * 쿠폰 유효성 검사
     */
    private void validateCoupon(Coupon coupon) {
        if (coupon == null || coupon.isDeleted()) {
            throw new ApplicationException(COUPON_NOT_FOUND);
        }
        if (coupon.isExpired()) {
            throw new ApplicationException(COUPON_EXPIRED);
        }
        if (coupon.getCount() <= 0) {
            throw new ApplicationException(COUPON_COUNT_EXHAUSTED);
        }
    }

    /**
     * 사용자가 이미 쿠폰을 가지고 있는지 검사
     */
    private void validateUserHasCoupon(User user, Coupon coupon) {
        boolean userHasCoupon = userCouponRepository.existsByUserAndCoupon(user, coupon);
        if (userHasCoupon) {
            throw new ApplicationException(COUPON_ALREADY_ISSUED);
        }
    }

    /**
     * 사용자의 쿠폰 조회
     */
    public List<GetCouponResponseDto> getUserCouponByUserId(long userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findAllByUserId(userId);
        return userCoupons.stream()
                .map(userCoupon -> GetCouponResponseDto.of(userCoupon.getId(), userCoupon.getCoupon().getExpiredAt(),
                        userCoupon.getCoupon().getDiscountAmount()))
                .collect(Collectors.toList());
    }

}
