package com.spotlightspace.core.payment.service;

import static com.spotlightspace.common.exception.ErrorCode.COUPON_ALREADY_USED;
import static com.spotlightspace.common.exception.ErrorCode.EVENT_PARTICIPANT_LIMIT_EXCEED;
import static com.spotlightspace.common.exception.ErrorCode.NOT_IN_EVENT_RECRUITMENT_PERIOD;
import static com.spotlightspace.core.payment.constant.PaymentConstant.CID;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.coupon.domain.Coupon;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.payment.client.KakaopayApi;
import com.spotlightspace.core.payment.domain.Payment;
import com.spotlightspace.core.payment.dto.response.ApprovePaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.ReadyPaymentResponseDto;
import com.spotlightspace.core.payment.repository.PaymentRepository;
import com.spotlightspace.core.ticket.service.TicketService;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import com.spotlightspace.core.usercoupon.domain.UserCoupon;
import com.spotlightspace.core.usercoupon.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketService ticketService;
    private final KakaopayApi kakaopayApi;
    private final UserCouponRepository userCouponRepository;

    public ReadyPaymentResponseDto readyPayment(long userId, long eventId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Event event = eventRepository.findByIdOrElseThrow(eventId);

        validateRecruitmentPeriod(event);

        Payment payment = Payment.create(CID, event, user, event.getPrice());
        paymentRepository.save(payment);

        validateParticipantLimit(event);

        ReadyPaymentResponseDto responseDto = kakaopayApi.readyPayment(
                payment.getPartnerOrderId(),
                user.getId(),
                event.getTitle(),
                event.getId(),
                payment.getAmount()
        );

        payment.setTid(responseDto.getTid());

        return responseDto;
    }

    public ReadyPaymentResponseDto readyPayment(long userId, long eventId, long couponId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Event event = eventRepository.findByIdOrElseThrow(eventId);
        UserCoupon userCoupon = userCouponRepository.findByCouponIdAndUserIdOrElseThrow(couponId, user.getId());
        Coupon coupon = userCoupon.getCoupon();

        validateRecruitmentPeriod(event);
        validateUserCoupon(userCoupon);

        int paymentAmount = calculatePaymentAmount(event.getPrice(), coupon.getDiscountAmount());
        Payment payment = Payment.createWithCoupon(CID, event, user, paymentAmount, userCoupon);
        paymentRepository.save(payment);

        validateParticipantLimit(event);

        ReadyPaymentResponseDto responseDto = kakaopayApi.readyPayment(
                payment.getPartnerOrderId(),
                user.getId(),
                event.getTitle(),
                event.getId(),
                payment.getAmount()
        );

        payment.setTid(responseDto.getTid());

        return responseDto;
    }

    public ApprovePaymentResponseDto approvePayment(String pgToken, String tid) {
        Payment payment = paymentRepository.findByTidOrElseThrow(tid);
        ApprovePaymentResponseDto responseDto = kakaopayApi.approvePayment(pgToken, payment);

        ticketService.createTicket(payment.getUser(), payment.getEvent(), payment.getAmount());
        payment.approve();

        return responseDto;
    }

    private void validateRecruitmentPeriod(Event event) {
        if (event.isNotRecruitmentPeriod()) {
            throw new ApplicationException(NOT_IN_EVENT_RECRUITMENT_PERIOD);
        }
    }

    private void validateParticipantLimit(Event event) {
        Long buyerCount = paymentRepository.countByEvent(event);
        if (event.isParticipantLimitExceed(buyerCount.intValue() + 1)) {
            throw new ApplicationException(EVENT_PARTICIPANT_LIMIT_EXCEED);
        }
    }

    private void validateUserCoupon(UserCoupon userCoupon) {
        if (userCoupon.isUsed()) {
            throw new ApplicationException(COUPON_ALREADY_USED);
        }
    }

    private int calculatePaymentAmount(int price, int discountAmount) {
        return Math.max(price - discountAmount, 0);
    }
}
