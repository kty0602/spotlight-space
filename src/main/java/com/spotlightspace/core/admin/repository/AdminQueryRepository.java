package com.spotlightspace.core.admin.repository;

import com.querydsl.core.Tuple;
import com.spotlightspace.core.admin.dto.responsedto.AdminCouponResponseDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminEventResponseDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminReviewResponseDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import com.spotlightspace.core.coupon.domain.Coupon;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


public interface AdminQueryRepository {
    Page<AdminUserResponseDto> getAdminUsers(String keyword, Pageable pageable);

    Page<AdminEventResponseDto> getAdminEvents(String keyword, Pageable pageable);

    Page<AdminReviewResponseDto> getAdminReviews(String keyword, Pageable pageable);

    Page<AdminCouponResponseDto> getAdminCoupons(String keyword, Pageable pageable);

    Optional<User> findUserById(Long id);

    Optional<Event> findEventById(Long id);

    Optional<Review> findReviewById(Long id);

    void saveCoupon(Coupon coupon);

    Optional<Coupon> findCouponById(Long id);

    List<Tuple> findCoupons(String keyword, Pageable pageable);

    long countCoupons(String keyword);


}
