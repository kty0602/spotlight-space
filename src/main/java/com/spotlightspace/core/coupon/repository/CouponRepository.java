package com.spotlightspace.core.coupon.repository;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.coupon.domain.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

import static com.spotlightspace.common.exception.ErrorCode.COUPON_NOT_FOUND;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponQueryRepository {
    Optional<Coupon> findCouponById(Long id);
    default Coupon findByIdOrElseThrow(Long couponId) {
        return findById(couponId)
                .orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));
    }

    Optional<Coupon> findByIdAndIsDeletedFalse(Long couponId);

    default Coupon findByIdOrElseThrow(Long couponId) {
        return findById(couponId)
                .orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));
    }


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :couponId")
    Optional<Coupon> findByIdWithPessimisticLock(Long couponId);

}
