package com.spotlightspace.core.usercoupon.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spotlightspace.config.JwtUtil;
import com.spotlightspace.core.user.domain.UserRole;
import com.spotlightspace.core.usercoupon.service.UserCouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserCouponControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtUtil jwtUtil;

    @MockBean
    UserCouponService userCouponService;

    @Test
    @DisplayName("쿠폰 발급 요청 시 쿠폰이 정상적으로 발급된다.")
    void issueCouponBasic() throws Exception {
        // given
        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_ADMIN);

        // when & then
        mockMvc.perform(post("/api/v1/user-coupons/issue/basic/{couponId}", 1L)
                        .header("accessToken", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userCouponService, times(1)).issueCouponBasic(any());
    }

    @Test
    @DisplayName("쿠폰 선착순 발급 요청 시 쿠폰이 정상적으로 발급된다.")
    void issueCouponWithPessimisticLockAndQueue() throws Exception {
        // given
        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_ADMIN);

        // when & then
        mockMvc.perform(post("/api/v1/user-coupons/issue/pessimistic/{couponId}", 1L)
                        .header("accessToken", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userCouponService, times(1)).issueCouponWithPessimisticLockAndQueue(any());
    }
}
