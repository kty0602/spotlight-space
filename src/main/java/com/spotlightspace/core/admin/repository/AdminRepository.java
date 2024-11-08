package com.spotlightspace.core.admin.repository;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.admin.domain.Admin;
import com.spotlightspace.core.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static com.spotlightspace.common.exception.ErrorCode.ADMIN_NOT_FOUND;

public interface AdminRepository extends JpaRepository<Admin, Long>, AdminQueryRepository {
    Optional<Coupon> findCouponById(Long id);

    Optional<Admin> findByEmail(String email);

    default Admin findByEmailOrElseThrow(String email) {
        return findByEmail(email).orElseThrow(() -> new ApplicationException(ADMIN_NOT_FOUND));
    }

}
