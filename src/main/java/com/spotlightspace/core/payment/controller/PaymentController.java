package com.spotlightspace.core.payment.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.payment.dto.request.ReadyPaymentRequestDto;
import com.spotlightspace.core.payment.dto.response.ApprovePaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.ReadyPaymentResponseDto;
import com.spotlightspace.core.payment.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private static final String TID = "tid";

    private final PaymentService paymentService;

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
        ReadyPaymentResponseDto readyPaymentResponseDto = paymentService.readyPayment(
                authUser.getUserId(),
                requestDto.getEventId(),
                requestDto.getCouponId(),
                requestDto.getPointAmount()
        );

        session.setAttribute(TID, readyPaymentResponseDto.getTid());

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
        ApprovePaymentResponseDto responseDto = paymentService.approvePayment(
                pgToken,
                String.valueOf(session.getAttribute(TID))
        );
        return ResponseEntity.ok(responseDto);
    }
}
