package com.spotlightspace.core.coupon.repository;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static com.spotlightspace.common.exception.ErrorCode.COUPON_NOT_FOUND;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponQueryRepository {
    Optional<Coupon> findCouponById(Long id);
    default Coupon findByIdOrElseThrow(Long couponId) {
        return findById(couponId)
                .orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));
    }

    Optional<Coupon> findByIdAndIsDeletedFalse(Long couponId);

}
