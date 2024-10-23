package com.spotlightspace.core.coupon.repository;

import com.spotlightspace.core.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponQueryRepository {

}
