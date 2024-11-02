package com.spotlightspace.core.payment.service;

import static com.spotlightspace.common.exception.ErrorCode.CANCELLATION_PERIOD_EXPIRED;
import static com.spotlightspace.common.exception.ErrorCode.COUPON_ALREADY_USED;
import static com.spotlightspace.common.exception.ErrorCode.EVENT_TICKET_OUT_OF_STOCK;
import static com.spotlightspace.common.exception.ErrorCode.NOT_ENOUGH_POINT_AMOUNT;
import static com.spotlightspace.common.exception.ErrorCode.NOT_IN_EVENT_RECRUITMENT_PERIOD;
import static com.spotlightspace.common.exception.ErrorCode.POINT_AMOUNT_CANNOT_BE_NEGATIVE;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.eventticketstock.domain.EventTicketStock;
import com.spotlightspace.core.eventticketstock.repository.EventTicketStockRepository;
import com.spotlightspace.core.payment.domain.Payment;
import com.spotlightspace.core.payment.domain.PaymentStatus;
import com.spotlightspace.core.payment.dto.PaymentDto;
import com.spotlightspace.core.payment.repository.PaymentRepository;
import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.point.repository.PointRepository;
import com.spotlightspace.core.point.service.PointService;
import com.spotlightspace.core.pointhistory.service.PointHistoryService;
import com.spotlightspace.core.ticket.service.TicketService;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import com.spotlightspace.core.usercoupon.domain.UserCoupon;
import com.spotlightspace.core.usercoupon.repository.UserCouponRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final TicketService ticketService;
    private final PointService pointService;
    private final PointHistoryService pointHistoryService;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final UserCouponRepository userCouponRepository;
    private final PointRepository pointRepository;
    private final EventTicketStockRepository eventTicketStockRepository;

    public PaymentDto getPayment(long paymentId) {
        return PaymentDto.from(paymentRepository.findByIdOrElseThrow(paymentId));
    }

    public long createPayment(long userId, long eventId, String cid, Long couponId, Integer pointAmount) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Event event = eventRepository.findByIdOrElseThrow(eventId);
        EventTicketStock eventTicketStock = eventTicketStockRepository
                .findByEventIdWithPessimisticLockOrElseThrow(event.getId());

        validateRecruitmentPeriod(event);
        validateEventTicketStock(eventTicketStock);

        eventTicketStock.decreaseStock();

        Point point = null;
        UserCoupon userCoupon = null;
        int discountedPrice = event.getPrice();
        if (doesCouponIdExist(couponId)) {
            userCoupon = userCouponRepository.findByCouponIdAndUserIdOrElseThrow(couponId, user.getId());
            validateUserCoupon(userCoupon);
            discountedPrice -= userCoupon.getDiscountAmount();
        }
        if (doesPointAmountExist(pointAmount)) {
            point = pointRepository.findByUserOrElseThrow(user);
            validatePoint(point, pointAmount);
            point.deduct(pointAmount);
            discountedPrice -= pointAmount;
        }

        Payment payment = Payment.create(cid, event, user, event.getPrice(), discountedPrice, userCoupon, point);
        paymentRepository.save(payment);

        if (isPointUsed(point)) {
            pointHistoryService.createPointHistory(payment, point, pointAmount);
        }

        return payment.getId();
    }

    public void readyPayment(long paymentId, String tid) {
        Payment payment = paymentRepository.findByIdOrElseThrow(paymentId);
        payment.ready(tid);
    }

    public void approvePayment(long paymentId) {
        Payment payment = paymentRepository.findByIdOrElseThrow(paymentId);
        payment.approve();

        ticketService.createTicket(payment.getUser(), payment.getEvent(), payment.getOriginalAmount());
    }

    public void cancelPayment(long paymentId) {
        Payment payment = paymentRepository.findByIdOrElseThrow(paymentId);
        Event event = payment.getEvent();

        if (event.isFinishedRecruitment(LocalDateTime.now())) {
            throw new ApplicationException(CANCELLATION_PERIOD_EXPIRED);
        }

        payment.cancel();
        if (payment.isPointUsed()) {
            pointService.cancelPointUsage(payment.getPoint());
        }

        EventTicketStock eventTicketStock = eventTicketStockRepository.findByEventOrElseThrow(payment.getEvent());
        eventTicketStock.increaseStock();
    }

    public void cancelPayments(Event event) {
        paymentRepository.findPaymentsByEventAndStatus(event, PaymentStatus.APPROVED)
                .forEach(payment -> cancelPayment(payment.getId())
                );
    }

    public void failPayment(long paymentId) {
        Payment payment = paymentRepository.findByIdOrElseThrow(paymentId);
        payment.fail();
    }

    private boolean doesCouponIdExist(Long couponId) {
        return couponId != null;
    }

    private boolean doesPointAmountExist(Integer pointAmount) {
        return pointAmount != null;
    }

    private void validateRecruitmentPeriod(Event event) {
        if (event.isNotRecruitmentPeriod()) {
            throw new ApplicationException(NOT_IN_EVENT_RECRUITMENT_PERIOD);
        }
    }

    private void validateEventTicketStock(EventTicketStock eventTicketStock) {
        if (eventTicketStock.isOutOfStock()) {
            throw new ApplicationException(EVENT_TICKET_OUT_OF_STOCK);
        }
    }

    private void validateUserCoupon(UserCoupon userCoupon) {
        if (userCoupon.isUsed()) {
            throw new ApplicationException(COUPON_ALREADY_USED);
        }
    }

    private void validatePoint(Point point, int pointAmount) {
        if (pointAmount < 0) {
            throw new ApplicationException(POINT_AMOUNT_CANNOT_BE_NEGATIVE);
        }
        if (point.cannotDeduct(pointAmount)) {
            throw new ApplicationException(NOT_ENOUGH_POINT_AMOUNT);
        }
    }

    private boolean isPointUsed(Point point) {
        return point != null;
    }
}
