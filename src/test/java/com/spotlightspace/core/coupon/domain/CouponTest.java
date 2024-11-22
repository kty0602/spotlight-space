package com.spotlightspace.core.coupon.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.common.exception.ErrorCode;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CouponTest {

    @Test
    @DisplayName("쿠폰 할인 가격과 사용기간을 수정할 수 있다.")
    void update() {
        // given
        int initialDiscountAmount = 1_000;
        LocalDate initialExpiredAt = LocalDate.of(2023, 1, 1);
        Coupon coupon = Coupon.of(initialDiscountAmount, initialExpiredAt, 10, "coupon");

        int newDiscountAmount = 2_000;
        LocalDate newExpiredAt = LocalDate.of(2024, 1, 1);

        // when
        coupon.update(newDiscountAmount, newExpiredAt);

        // then
        assertThat(coupon.getDiscountAmount()).isEqualTo(newDiscountAmount);
        assertThat(coupon.getExpiredAt()).isEqualTo(newExpiredAt);
    }

    @Test
    @DisplayName("쿠폰 개수는 0보다 작아질 수 없다.")
    void decreaseCount() {
        // given
        Coupon coupon = Coupon.of(1_000, LocalDate.of(2023, 1, 1), 0, "coupon");

        // when & then
        assertThatThrownBy(() -> coupon.decreaseCount())
                .isInstanceOf(ApplicationException.class)
                .hasMessage(ErrorCode.COUPON_COUNT_EXHAUSTED.getMessage());
    }

}
