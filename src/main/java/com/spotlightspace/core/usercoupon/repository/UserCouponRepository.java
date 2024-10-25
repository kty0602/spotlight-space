package com.spotlightspace.core.usercoupon.repository;

import com.spotlightspace.core.usercoupon.domain.UserCoupon;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCouponRepository extends JpaRepository<UserCoupon,Long>{

    List<UserCoupon> findAllByUserId(long userId);
}

