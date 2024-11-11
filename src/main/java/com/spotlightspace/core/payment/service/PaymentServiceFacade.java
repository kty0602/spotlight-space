package com.spotlightspace.core.payment.service;

import com.spotlightspace.core.payment.dto.response.PaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayPaymentResponseDto;
import com.spotlightspace.core.paymentevent.domain.PaymentEvent;
import com.spotlightspace.core.paymentevent.repository.PaymentEventRepository;
import com.spotlightspace.integration.kakaopay.KakaopayApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceFacade {

    private final KakaopayApi kakaopayApi;
    private final PaymentService paymentService;
    private final PaymentEventRepository paymentEventRepository;

    public KakaopayPaymentResponseDto readyPayment(
            long userId,
            long eventId,
            String cid,
            Long couponId,
            Integer pointAmount
    ) {
        long paymentId = paymentService.createPayment(userId, eventId, cid, couponId, pointAmount);
        PaymentResponseDto paymentDto = paymentService.getPayment(paymentId);
        PaymentEvent paymentEvent =
                paymentEventRepository.save(PaymentEvent.createReadyEvent(paymentDto.getPaymentId()));

        KakaopayPaymentResponseDto kakaopayPaymentResponseDto = kakaopayApi.readyPayment(
                paymentDto.getCid(),
                paymentDto.getPaymentId(),
                paymentDto.getUserId(),
                paymentDto.getEventTitle(),
                paymentDto.getEventId(),
                paymentDto.getDiscountedAmount()
        );

        if (kakaopayPaymentResponseDto.getStatus().equals("fail")) {
            paymentService.failPayment(paymentId, paymentEvent.getId());
            return kakaopayPaymentResponseDto;
        }

        paymentService.readyPayment(paymentId, kakaopayPaymentResponseDto.getTid(), paymentEvent.getId());

        return kakaopayPaymentResponseDto;
    }

    public KakaopayPaymentResponseDto approvePayment(String pgToken, String tid) {
        PaymentResponseDto paymentDto = paymentService.getPayment(tid);
        PaymentEvent paymentEvent =
                paymentEventRepository.save(PaymentEvent.createApproveEvent(paymentDto.getPaymentId()));

        KakaopayPaymentResponseDto responseDto = kakaopayApi.approvePayment(
                pgToken,
                tid,
                paymentDto.getCid(),
                paymentDto.getPaymentId(),
                paymentDto.getUserId()
        );

        if (responseDto.getStatus().equals("success")) {
            paymentService.approvePayment(paymentDto.getPaymentId(), paymentEvent.getId());
        } else {
            paymentService.failPayment(paymentDto.getPaymentId(), paymentEvent.getId());
        }

        return responseDto;
    }

    public KakaopayPaymentResponseDto cancelPayment(String tid) {
        PaymentResponseDto paymentDto = paymentService.getPayment(tid);
        PaymentEvent paymentEvent =
                paymentEventRepository.save(PaymentEvent.createCancelEvent(paymentDto.getPaymentId()));

        KakaopayPaymentResponseDto responseDto = kakaopayApi.cancelPayment(
                paymentDto.getCid(),
                paymentDto.getTid(),
                paymentDto.getDiscountedAmount(),
                0
        );

        if (responseDto.getStatus().equals("success")) {
            paymentService.cancelPayment(paymentDto.getPaymentId(), paymentEvent.getId());
        } else {
            paymentEventRepository.deleteById(paymentEvent.getId());
        }

        return responseDto;
    }

    public Page<PaymentResponseDto> getPayments(long userId, PageRequest pageRequest) {
        return paymentService.getPayments(userId, pageRequest);
    }
}
