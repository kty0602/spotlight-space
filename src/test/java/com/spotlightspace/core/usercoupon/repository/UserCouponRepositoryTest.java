package com.spotlightspace.core.usercoupon.repository;

import static com.spotlightspace.common.exception.ErrorCode.COUPON_NOT_FOUND;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserCouponRepositoryTest {

    @Autowired
    UserCouponRepository userCouponRepository;

    @Test
    @DisplayName("쿠폰 ID와 사용자 ID에 해당하는 사용자 쿠폰이 존재하지 않으면 예외가 발생한다.")
    void findByCouponIdAndUserIdOrElseThrow() {
        // given
        long invalidUserId = 1L;
        long invalidCouponId = 1L;

        // when & then
        Assertions.assertThatThrownBy(() -> userCouponRepository.findByCouponIdAndUserIdOrElseThrow(invalidCouponId, invalidUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(COUPON_NOT_FOUND.getMessage());
    }

}
