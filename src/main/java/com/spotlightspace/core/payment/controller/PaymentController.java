package com.spotlightspace.core.payment.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.payment.dto.PaymentDto;
import com.spotlightspace.core.payment.dto.request.CancelPaymentRequestDto;
import com.spotlightspace.core.payment.dto.request.ReadyPaymentRequestDto;
import com.spotlightspace.core.payment.dto.response.ApprovePaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.CancelPaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.ReadyPaymentResponseDto;
import com.spotlightspace.core.payment.service.PaymentService;
import com.spotlightspace.integration.kakaopay.KakaopayApi;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class PaymentController {

    private static final String TID = "tid";

    private final PaymentService paymentService;
    private final KakaopayApi kakaopayApi;
    private final String cid;

    public PaymentController(
            PaymentService paymentService,
            KakaopayApi kakaopayApi,
            @Value("${payment.kakao.cid}") String cid
    ) {
        this.paymentService = paymentService;
        this.kakaopayApi = kakaopayApi;
        this.cid = cid;
    }

    /**
     * 결제를 시작하기 위해 결제정보를 카카오페이 서버에 전달하고 결제 고유번호(TID)와 URL을 응답받아 반환합니다.
     *
     * @param session    TID를 저장하기 위한 session입니다.
     * @param requestDto 결제 요청을 위한 Dto입니다.
     * @param authUser   로그인한 유저 정보를 받아옵니다.
     * @return
     */
    @PostMapping("/api/v1/payments/ready")
    public ResponseEntity<ReadyPaymentResponseDto> readyPayment(
            HttpSession session,
            @RequestBody ReadyPaymentRequestDto requestDto,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        PaymentDto paymentDto = paymentService.createPayment(
                authUser.getUserId(),
                requestDto.getEventId(),
                cid,
                requestDto.getCouponId(),
                requestDto.getPointAmount()
        );

        ReadyPaymentResponseDto readyPaymentResponseDto = kakaopayApi.readyPayment(
                paymentDto.getCid(),
                paymentDto.getPaymentId(),
                paymentDto.getUserId(),
                paymentDto.getEventTitle(),
                paymentDto.getEventId(),
                paymentDto.getDiscountedAmount()
        );

        session.setAttribute(TID, readyPaymentResponseDto.getTid());

        paymentService.readyPayment(paymentDto.getPaymentId(), readyPaymentResponseDto.getTid());

        return ResponseEntity.ok(readyPaymentResponseDto);
    }

    /**
     * 최종적으로 결제 완료 처리를 합니다.
     *
     * @param session TID를 저장하기 위한 session입니다.
     * @param pgToken 결제승인 요청을 인증하는 토큰입니다.
     * @return
     */
    @GetMapping("/api/v1/payments/approve")
    public ResponseEntity<ApprovePaymentResponseDto> approvePayment(
            HttpSession session,
            @RequestParam("pg_token") String pgToken
    ) {
        paymentService.approvePayment(String.valueOf(session.getAttribute(TID)));

        PaymentDto paymentDto = paymentService.getPayment(String.valueOf(session.getAttribute(TID)));
        ApprovePaymentResponseDto responseDto = kakaopayApi.approvePayment(
                pgToken,
                paymentDto.getTid(),
                paymentDto.getCid(),
                paymentDto.getPaymentId(),
                paymentDto.getUserId()
        );

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 결제 취소 처리를 합니다.
     *
     * @param requestDto 결제 취소 요청을 위한 Dto입니다.
     * @return
     */
    @PatchMapping("/api/v1/payments/cancel")
    public ResponseEntity<CancelPaymentResponseDto> cancelPayment(@RequestBody CancelPaymentRequestDto requestDto) {
        CancelPaymentResponseDto responseDto = paymentService.cancelPayment(
                requestDto.getTid(),
                requestDto.getCancelAmount(),
                requestDto.getCancelTaxFreeAmount()
        );

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 결제 실패 처리를 합니다.
     *
     * @param session 결제 고유 번호를 얻기 위한 세션입니다.
     * @return
     */
    @GetMapping("/api/v1/payments/fail")
    public ResponseEntity<Void> failPayment(HttpSession session) {
        paymentService.failPayment(String.valueOf(session.getAttribute(TID)));
        return ResponseEntity.ok().build();
    }
}
