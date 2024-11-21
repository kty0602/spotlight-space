package com.spotlightspace.core.payment.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotlightspace.config.JwtUtil;
import com.spotlightspace.core.payment.dto.request.CancelPaymentRequestDto;
import com.spotlightspace.core.payment.dto.request.ReadyPaymentRequestDto;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayApprovePaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayApprovePaymentResponseDto.Amount;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayApprovePaymentResponseDto.CardInfo;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayCancelPaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayCancelPaymentResponseDto.ApprovedCancelAmount;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayCancelPaymentResponseDto.CancelAvailableAmount;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayCancelPaymentResponseDto.CanceledAmount;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayPaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayReadyPaymentResponseDto;
import com.spotlightspace.core.payment.service.PaymentServiceFacade;
import com.spotlightspace.core.user.domain.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtUtil jwtUtil;

    @MockBean
    PaymentServiceFacade paymentServiceFacade;

    @Test
    @DisplayName("결제 준비 요청 시 정상적으로 동작한다")
    void readyPayment() throws Exception {
        // given
        ReadyPaymentRequestDto requestDto = new ReadyPaymentRequestDto(1L, 1L, 1000);

        KakaopayPaymentResponseDto kakaopayPaymentResponseDto = KakaopayPaymentResponseDto.ofSuccess(
                new KakaopayReadyPaymentResponseDto(
                        UUID.randomUUID().toString(),
                        "/happyDog",
                        "/happyDog",
                        "/happyDog",
                        "/happyDog",
                        "/happyDog",
                        LocalDateTime.now()
                )
        );

        given(paymentServiceFacade.readyPayment(anyLong(), anyLong(), anyString(), anyLong(), anyInt()))
                .willReturn(kakaopayPaymentResponseDto);

        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_USER);

        // when & then
        mockMvc.perform(post("/api/v1/payments/ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("accessToken", accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        verify(paymentServiceFacade, times(1))
                .readyPayment(anyLong(), anyLong(), anyString(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("결제 승인 요청 시 정상적으로 동작한다.")
    void approvePayment() throws Exception {
        // given
        String pgToken = "pgToken";
        String tid = UUID.randomUUID().toString();

        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("tid", tid);

        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_USER);

        KakaopayPaymentResponseDto<KakaopayApprovePaymentResponseDto> responseDto = KakaopayPaymentResponseDto.ofSuccess(
                new KakaopayApprovePaymentResponseDto(
                        "aid",
                        tid,
                        "cid",
                        "sid",
                        String.valueOf(1),
                        String.valueOf(1),
                        "paymentMethodType",
                        new Amount(10000, 0, 0, 0, 0, 0),
                        new CardInfo("cardBin", "cardType", "cardMid", "cardCd", "cardName", "cardMoid", "cardAuthNo",
                                "cardPointAuthNo", "cardInterest", "cardQuota", "cardPartCancels",
                                "cardPartCancelType"),
                        "eventTitle",
                        String.valueOf(1),
                        1,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        "payload"
                )
        );

        given(paymentServiceFacade.approvePayment(anyString(), anyString()))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/payments/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("pg_token", pgToken)
                        .header("accessToken", accessToken)
                        .session(mockSession))
                .andDo(print())
                .andExpect(status().isOk());

        verify(paymentServiceFacade, times(1))
                .approvePayment(anyString(), anyString());
    }

    @Test
    @DisplayName("결제 취소 요청시 정상적으로 취소된다.")
    void cancelPayment() throws Exception {
        // given
        CancelPaymentRequestDto requestDto = new CancelPaymentRequestDto(UUID.randomUUID().toString());

        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_USER);

        KakaopayPaymentResponseDto<KakaopayCancelPaymentResponseDto> responseDto = KakaopayPaymentResponseDto.ofSuccess(
                new KakaopayCancelPaymentResponseDto(
                        "aid",
                        requestDto.getTid(),
                        "cid",
                        "CANCEL_PAYMENT",
                        "1",
                        "1",
                        "MONEY",
                        new KakaopayCancelPaymentResponseDto.Amount(10000, 0, 0, 0, 0, 0),
                        new ApprovedCancelAmount(10000, 0, 0, 0, 0, 0),
                        new CanceledAmount(10000, 0, 0, 0, 0, 0),
                        new CancelAvailableAmount(10000, 0, 0, 0, 0, 0),
                        "itemName",
                        "itemCode",
                        1,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        "payload"
                )
        );

        given(paymentServiceFacade.cancelPayment(anyString()))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/api/v1/payments/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("accessToken", accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        verify(paymentServiceFacade, times(1))
                .cancelPayment(anyString());
    }

}
