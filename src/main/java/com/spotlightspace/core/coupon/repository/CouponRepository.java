package com.spotlightspace.core.coupon.repository;

import com.spotlightspace.core.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponQueryRepository {
    Optional<Coupon> findCouponById(Long id);

}
