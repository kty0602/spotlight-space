package com.spotlightspace.core.admin.repository;

import com.spotlightspace.core.admin.domain.Admin;
import com.spotlightspace.core.attachment.domain.Attachment;
import com.spotlightspace.core.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long>, AdminQueryRepository {
    Optional<Coupon> findCouponById(Long id);

}
