package com.spotlightspace.core.payment.service;

import static com.spotlightspace.common.exception.ErrorCode.EVENT_PARTICIPANT_LIMIT_EXCEED;
import static com.spotlightspace.common.exception.ErrorCode.NOT_IN_EVENT_RECRUITMENT_PERIOD;
import static com.spotlightspace.core.payment.constant.PaymentConstant.CID;

import com.spotlightspace.common.exception.ApplicationException;
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

    public ReadyPaymentResponseDto readyPayment(long userId, long eventId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Event event = eventRepository.findByIdOrElseThrow(eventId);

        if (event.isNotRecruitmentPeriod()) {
            throw new ApplicationException(NOT_IN_EVENT_RECRUITMENT_PERIOD);
        }

        Payment payment = Payment.createWithoutTid(CID, event, user, 1);
        paymentRepository.save(payment);

        Long buyerCount = paymentRepository.countByEvent(event);
        if (event.isParticipantLimitExceed(buyerCount.intValue() + 1)) {
            throw new ApplicationException(EVENT_PARTICIPANT_LIMIT_EXCEED);
        }

        ReadyPaymentResponseDto responseDto = kakaopayApi.readyPayment(payment, user, event);

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
}
