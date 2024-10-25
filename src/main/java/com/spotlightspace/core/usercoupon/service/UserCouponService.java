package com.spotlightspace.core.usercoupon.service;

import com.spotlightspace.core.user.dto.response.GetCouponResponseDto;
import com.spotlightspace.core.usercoupon.domain.UserCoupon;
import com.spotlightspace.core.usercoupon.repository.UserCouponRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCouponService {

    private final UserCouponRepository userCouponRepository;

    public List<GetCouponResponseDto> getUserCouponByUserId(long userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findAllByUserId(userId);

        return userCoupons.stream()
                .map(userCoupon -> GetCouponResponseDto.of(userCoupon.getId(), userCoupon.getCoupon().getExpiredAt(),
                        userCoupon.getCoupon().getDiscountAmount()))
                .collect(Collectors.toList());
    }

}
