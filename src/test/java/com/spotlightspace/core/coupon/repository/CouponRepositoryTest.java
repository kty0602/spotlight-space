package com.spotlightspace.core.coupon.repository;

import static com.spotlightspace.common.exception.ErrorCode.COUPON_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spotlightspace.common.exception.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CouponRepositoryTest {

    @Autowired
    CouponRepository couponRepository;

    @Test
    @DisplayName("쿠폰 ID에 해당하는 쿠폰이 없는 경우 예외가 발생한다.")
    void findByIdOrElseThrow() {
        // given
        long nonExistentCouponId = 1L;

        // when & then
        assertThatThrownBy(() -> couponRepository.findByIdOrElseThrow(nonExistentCouponId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(COUPON_NOT_FOUND.getMessage());
    }

}
