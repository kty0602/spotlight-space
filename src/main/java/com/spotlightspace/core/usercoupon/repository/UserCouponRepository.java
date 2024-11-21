package com.spotlightspace.core.usercoupon.repository;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.coupon.domain.Coupon;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.usercoupon.domain.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static com.spotlightspace.common.exception.ErrorCode.COUPON_NOT_FOUND;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    List<UserCoupon> findAllByUserId(long userId);

    @Query("select u from UserCoupon u "
            + "join fetch u.coupon c "
            + "where c.id = :couponId and u.user.id = :userId"
    )
    Optional<UserCoupon> findByIdAndUserId(@Param("couponId") long couponId, @Param("userId") long userId);

    default UserCoupon findByCouponIdAndUserIdOrElseThrow(long couponId, long userId) {
        return findByIdAndUserId(couponId, userId).orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));
    }


    boolean existsByUserAndCoupon(User user, Coupon coupon);
}
