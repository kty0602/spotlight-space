package com.spotlightspace.core.payment.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.payment.dto.response.PaymentResponseDto;
import com.spotlightspace.core.payment.dto.request.CancelPaymentRequestDto;
import com.spotlightspace.core.payment.dto.request.ReadyPaymentRequestDto;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayPaymentResponseDto;
import com.spotlightspace.core.payment.service.PaymentServiceFacade;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
public class PaymentController {

    public static final int FIFTEEN_MINUTES = 60 * 15;
    private final PaymentServiceFacade paymentServiceFacade;
    private final String cid;

    public PaymentController(
            PaymentServiceFacade paymentServiceFacade,
            @Value("${payment.kakao.cid}") String cid
    ) {
        this.paymentServiceFacade = paymentServiceFacade;
        this.cid = cid;
    }

    /**
     * 결제를 시작하기 위해 결제를 생성하고 결제정보를 카카오페이 서버에 전달하고 결제 고유번호(TID)와 URL을 응답받아 반환합니다.
     *
     * @param session    payment id를 저장하기 위한 session입니다.
     * @param requestDto 결제 요청을 위한 Dto입니다.
     * @param authUser   로그인한 유저 정보를 받아옵니다.
     * @return
     */
    @PostMapping("/api/v1/payments/ready")
    public ResponseEntity<KakaopayPaymentResponseDto> readyPayment(
            HttpSession session,
            @RequestBody ReadyPaymentRequestDto requestDto,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        KakaopayPaymentResponseDto responseDto = paymentServiceFacade.readyPayment(
                authUser.getUserId(),
                requestDto.getEventId(),
                cid,
                requestDto.getCouponId(),
                requestDto.getPointAmount()
        );
        session.setAttribute("tid", responseDto.getTid());
        session.setMaxInactiveInterval(FIFTEEN_MINUTES);

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 최종적으로 결제 완료 처리를 합니다.
     *
     * @param session payment id를 저장하기 위한 session입니다.
     * @param pgToken 결제승인 요청을 인증하는 토큰입니다.
     * @return
     */
    @GetMapping("/api/v1/payments/approve")
    public ResponseEntity<KakaopayPaymentResponseDto> approvePayment(
            HttpSession session,
            @RequestParam("pg_token") String pgToken
    ) {
        KakaopayPaymentResponseDto responseDto = paymentServiceFacade.approvePayment(
                pgToken,
                String.valueOf(session.getAttribute("tid"))
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
    public ResponseEntity<KakaopayPaymentResponseDto> cancelPayment(@RequestBody CancelPaymentRequestDto requestDto) {
        KakaopayPaymentResponseDto responseDto = paymentServiceFacade.cancelPayment(requestDto.getTid());

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 특정 유저의 결제 내역을 조회합니다.
     *
     * @param userId 조회할 유저의 id입니다.
     * @param pageNumber 결제 내역의 페이지 번호입니다.
     * @param pageSize 결제 내역의 페이지 크기입니다.
     * @return
     */
    @GetMapping("/api/v1/payments")
    public ResponseEntity<Page<PaymentResponseDto>> getPayments(
            @RequestParam(value = "userId") long userId,
            @Positive @RequestParam(defaultValue = "1") int pageNumber,
            @Positive @RequestParam(defaultValue = "10") int pageSize
    ) {
        PageRequest pageRequest = PageRequest.of(
                pageNumber - 1,
                pageSize,
                Sort.by(Direction.DESC, "createAt")
        );
        return ResponseEntity.ok(paymentServiceFacade.getPayments(userId, pageRequest));
    }
}
