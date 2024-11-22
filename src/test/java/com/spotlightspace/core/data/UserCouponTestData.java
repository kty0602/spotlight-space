package com.spotlightspace.core.data;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.coupon.domain.Coupon;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.domain.UserRole;
import com.spotlightspace.core.usercoupon.domain.UserCoupon;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserCouponTestData {


    public static AuthUser testAuthUser() {
        return new AuthUser(1L, "test@example.com", UserRole.ROLE_USER);
    }

    public static User testUserWithId() {
        User user = User.create(
                "password",
                UserTestData.testSignupUserRequestDto()
        );
        ReflectionTestUtils.setField(user, "id", 1L); // ID 설정
        return user;
    }

    public static Coupon testCoupon() {
        return Coupon.of(5000, LocalDate.now().plusDays(5), 10, "TESTCODE1234");
    }

    public static Coupon testCouponWithId() {
        Coupon coupon = Coupon.of(
                1000,
                LocalDate.now().plusDays(30),
                10,
                "ABC-123-DEF"
        );
        ReflectionTestUtils.setField(coupon, "id", 1L); // ID 설정
        return coupon;
    }

    public static UserCoupon testUserCoupon() {
        User user = testUserWithId();
        Coupon coupon = testCouponWithId();
        UserCoupon userCoupon = UserCoupon.of(user, coupon);
        ReflectionTestUtils.setField(userCoupon, "id", 1L); // ID 설정
        return userCoupon;
    }

    public static List<UserCoupon> testUserCouponsWithId(User user) {
        Coupon coupon = testCouponWithId();
        List<UserCoupon> userCoupons = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            UserCoupon userCoupon = UserCoupon.of(user, coupon);
            ReflectionTestUtils.setField(userCoupon, "id", (long) (i + 1)); // 각 UserCoupon에 고유 ID 설정
            userCoupons.add(userCoupon);
        }
        return userCoupons;
    }
}
