package com.spotlightspace.core.admin.service;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.admin.dto.requestdto.AdminCouponCreateRequestDto;
import com.spotlightspace.core.admin.dto.requestdto.AdminCouponUpdateRequestDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminCouponResponseDto;
import com.spotlightspace.core.admin.repository.AdminQueryRepository;
import com.spotlightspace.core.coupon.CouponCodeGenerator;
import com.spotlightspace.core.coupon.domain.Coupon;
import com.spotlightspace.core.coupon.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static com.spotlightspace.common.exception.ErrorCode.COUPON_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class AdminCouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private AdminQueryRepository adminRepository;

    @InjectMocks
    private AdminCouponService adminCouponService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCoupon_ShouldReturnValidCoupon() {
        // given
        AdminCouponCreateRequestDto requestDto = AdminCouponCreateRequestDto.of(1000, LocalDate.now().plusDays(30), 5);
        String generatedCode = CouponCodeGenerator.generateCode();
        Coupon coupon = Coupon.of(requestDto.getDiscountAmount(), requestDto.getExpiredAt(), requestDto.getCount(), generatedCode);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        // when
        AdminCouponResponseDto responseDto = adminCouponService.createCoupon(requestDto);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getCode()).isEqualTo(generatedCode);
        assertThat(responseDto.getDiscountAmount()).isEqualTo(requestDto.getDiscountAmount());
    }

    @Test
    void testGetAdminCoupons_WithKeyword() {
        // given
        String keyword = "test";
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("discountAmount").ascending());
        AdminCouponResponseDto couponDto = AdminCouponResponseDto.of(
                1L, 1000, LocalDate.now().plusDays(30), "TEST1234", false
        );
        Page<AdminCouponResponseDto> expectedPage = new PageImpl<>(Collections.singletonList(couponDto));

        when(adminRepository.getAdminCoupons(keyword, pageable)).thenReturn(expectedPage);

        // when
        Page<AdminCouponResponseDto> result = adminCouponService.getAdminCoupons(1, 10, keyword, "discountAmount", "asc");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("TEST1234");
    }

    @Test
    void testGetAdminCoupons_WithoutKeyword() {
        // given
        String keyword = null;
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("discountAmount").ascending());
        Page<AdminCouponResponseDto> expectedPage = new PageImpl<>(Collections.emptyList());

        when(adminRepository.getAdminCoupons(keyword, pageable)).thenReturn(expectedPage);

        // when
        Page<AdminCouponResponseDto> result = adminCouponService.getAdminCoupons(1, 10, keyword, "discountAmount", "asc");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void testUpdateCoupon_Success() {
        // given
        Long couponId = 1L;
        AdminCouponUpdateRequestDto updateRequestDto = AdminCouponUpdateRequestDto.of(1500, LocalDate.now().plusDays(60));

        // CouponCodeGenerator를 사용해 쿠폰 코드 생성
        String generatedCode = CouponCodeGenerator.generateCode();
        Coupon coupon = Coupon.of(1000, LocalDate.now().minusDays(30), 5, generatedCode);  // 적절한 수량과 생성된 코드 사용
        ReflectionTestUtils.setField(coupon, "id", couponId);  // ID 설정

        when(couponRepository.findById(anyLong())).thenReturn(Optional.of(coupon));

        // when
        adminCouponService.updateCoupon(couponId, updateRequestDto);

        // then
        verify(couponRepository, times(1)).findById(couponId);
        assertThat(coupon.getDiscountAmount()).isEqualTo(updateRequestDto.getDiscountAmount());
        assertThat(coupon.getExpiredAt()).isEqualTo(updateRequestDto.getExpiredAt());
    }

    @Test
    void testUpdateCoupon_CouponNotFound() {
        // given
        Long couponId = 1L;
        AdminCouponUpdateRequestDto updateRequestDto = AdminCouponUpdateRequestDto.of(1500, LocalDate.now().plusDays(60));

        // Mock repository to return empty, simulating coupon not found
        when(couponRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminCouponService.updateCoupon(couponId, updateRequestDto))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(COUPON_NOT_FOUND.getMessage());
    }

    @Test
    void testDeleteCoupon_Success() {
        // given
        Long couponId = 1L;
        Coupon coupon = Coupon.of(1000, LocalDate.now().plusDays(30), 5, CouponCodeGenerator.generateCode());

        when(couponRepository.findById(anyLong())).thenReturn(Optional.of(coupon));

        // when
        adminCouponService.deleteCoupon(couponId);

        // then
        verify(couponRepository, times(1)).findById(couponId);
        assertThat(coupon.isDeleted()).isTrue();
    }

    @Test
    void testDeleteCoupon_CouponNotFound() {
        // given
        Long couponId = 1L;

        // Mock repository to return empty, simulating coupon not found
        when(couponRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminCouponService.deleteCoupon(couponId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(COUPON_NOT_FOUND.getMessage());
    }
}
