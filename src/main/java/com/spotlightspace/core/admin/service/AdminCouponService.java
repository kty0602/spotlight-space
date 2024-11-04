package com.spotlightspace.core.admin.service;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.admin.dto.requestdto.AdminCouponCreateRequestDto;
import com.spotlightspace.core.admin.dto.requestdto.AdminCouponUpdateRequestDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminCouponResponseDto;
import com.spotlightspace.core.admin.repository.AdminQueryRepository;
import com.spotlightspace.core.coupon.CouponCodeGenerator;
import com.spotlightspace.core.coupon.domain.Coupon;
import com.spotlightspace.core.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.spotlightspace.common.exception.ErrorCode.COUPON_NOT_FOUND;


@Service
@RequiredArgsConstructor
@Transactional
public class AdminCouponService {

    private final CouponRepository couponRepository;
    private final AdminQueryRepository adminRepository;

    public AdminCouponResponseDto createCoupon(AdminCouponCreateRequestDto requestDto) {
        String code = CouponCodeGenerator.generateCode();
        Coupon coupon = Coupon.of(requestDto.getDiscountAmount(), requestDto.getExpiredAt(), requestDto.getCount(), code);
        Coupon savedCoupon = couponRepository.save(coupon);
        return AdminCouponResponseDto.of(
                savedCoupon.getId(),
                savedCoupon.getDiscountAmount(),
                savedCoupon.getExpiredAt(),
                savedCoupon.getCode(),
                savedCoupon.getIsUsed()
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminCouponResponseDto> getAdminCoupons(int page, int size, String keyword, String sortField, String sortOrder) {
        Sort sort = Sort.by(sortField);
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        return adminRepository.getAdminCoupons(keyword, pageable);
    }

    public void updateCoupon(Long couponId, AdminCouponUpdateRequestDto requestDto) {
        //todo : or els throw 말고 defalut로
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));
        coupon.update(requestDto.getDiscountAmount(), requestDto.getExpiredAt());
    }

    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));
        coupon.setAsUnusable();
    }
}
