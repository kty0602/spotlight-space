package com.spotlightspace.core.usercoupon.domain;

import static org.assertj.core.api.Assertions.*;

import com.spotlightspace.core.coupon.domain.Coupon;
import com.spotlightspace.core.data.UserTestData;
import com.spotlightspace.core.user.domain.User;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserCouponTest {

    @Test
    @DisplayName("유저 쿠폰 생성 시 초기 상태는 사용하지 않은 상태이다.")
    void createUserCoupon() {
        // given
        User user = UserTestData.testUser();
        Coupon coupon = Coupon.of(1_000, LocalDate.now().plusMonths(1L), 10, "coupon");

        // when
        UserCoupon userCoupon = UserCoupon.of(user, coupon);

        // then
        assertThat(userCoupon.isUsed()).isFalse();
    }

    @Test
    @DisplayName("유저 쿠폰 사용 시 사용처리가 된다.")
    void useUserCoupon() {
        // given
        User user = UserTestData.testUser();
        Coupon coupon = Coupon.of(1_000, LocalDate.now().plusMonths(1L), 10, "coupon");
        UserCoupon userCoupon = UserCoupon.of(user, coupon);

        // when
        userCoupon.use();

        // then
        assertThat(userCoupon.isUsed()).isTrue();
    }

    @Test
    @DisplayName("할인 금액을 반환할 수 있다.")
    void getDiscountAmount() {
        // given
        User user = UserTestData.testUser();
        Coupon coupon = Coupon.of(1_000, LocalDate.now().plusMonths(1L), 10, "coupon");
        UserCoupon userCoupon = UserCoupon.of(user, coupon);

        // when & then
        assertThat(userCoupon.getDiscountAmount()).isEqualTo(coupon.getDiscountAmount());
    }

    @Test
    @DisplayName("사용을 취소할 수 있다.")
    void cancelUsage() {
        // given
        User user = UserTestData.testUser();
        Coupon coupon = Coupon.of(1_000, LocalDate.now().plusMonths(1L), 10, "coupon");
        UserCoupon userCoupon = UserCoupon.of(user, coupon);
        userCoupon.use();

        // when
        userCoupon.cancelUsage();

        // then
        assertThat(userCoupon.isUsed()).isFalse();
    }
}
