package com.spotlightspace.core.payment.service;

import static com.spotlightspace.common.exception.ErrorCode.CANCELLATION_PERIOD_EXPIRED;
import static com.spotlightspace.common.exception.ErrorCode.NOT_ENOUGH_POINT_AMOUNT;
import static com.spotlightspace.core.event.domain.EventCategory.ART;
import static com.spotlightspace.core.payment.domain.PaymentStatus.APPROVED;
import static com.spotlightspace.core.payment.domain.PaymentStatus.CANCELED;
import static com.spotlightspace.core.payment.domain.PaymentStatus.FAILED;
import static com.spotlightspace.core.payment.domain.PaymentStatus.READY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.*;
import static org.mockito.BDDMockito.anyInt;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.data.UserTestData;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.domain.EventCategory;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.eventticketstock.domain.EventTicketStock;
import com.spotlightspace.core.eventticketstock.repository.EventTicketStockRepository;
import com.spotlightspace.core.payment.domain.Payment;
import com.spotlightspace.core.payment.dto.response.PaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayPaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayReadyPaymentResponseDto;
import com.spotlightspace.core.payment.repository.PaymentRepository;
import com.spotlightspace.core.paymentevent.domain.PaymentEvent;
import com.spotlightspace.core.paymentevent.repository.PaymentEventRepository;
import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.point.repository.PointRepository;
import com.spotlightspace.core.pointhistory.domain.PointHistory;
import com.spotlightspace.core.pointhistory.domain.PointHistoryStatus;
import com.spotlightspace.core.pointhistory.repository.PointHistoryRepository;
import com.spotlightspace.core.ticket.domain.Ticket;
import com.spotlightspace.core.ticket.repository.TicketRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import com.spotlightspace.integration.kakaopay.KakaopayApi;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
class PaymentServiceTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventTicketStockRepository eventTicketStockRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @MockBean
    KakaopayApi kakaopayApi;

    @Autowired
    PointRepository pointRepository;

    @Autowired
    PaymentEventRepository paymentEventRepository;

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    @Autowired
    TicketRepository ticketRepository;

    @AfterEach
    void tearDown() {
        pointHistoryRepository.deleteAllInBatch();
        paymentEventRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
        eventTicketStockRepository.deleteAllInBatch();
        ticketRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("결제 동시성 테스트 - Pessimistic Lock")
    void pessimisticLock() throws InterruptedException {
        // given
        User user = userRepository.save(UserTestData.testUser());
        pointRepository.save(Point.of(0, user));
        CreateEventRequestDto createEventRequestDto = getCreateEventRequestDtoWithMaxPeople(10);
        Event event = eventRepository.save(Event.of(createEventRequestDto, user));
        EventTicketStock eventTicketStock = eventTicketStockRepository.save(EventTicketStock.create(event));

        given(kakaopayApi.readyPayment(anyString(), anyLong(), anyLong(), anyString(), anyLong(), anyInt()))
                .willReturn(createKakaopayPaymentResponseDto());

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    paymentService.createPayment(user.getId(), event.getId(), "cid", null, null);
                } catch (Exception e) {
                    System.err.println("결제 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        List<Payment> payments = paymentRepository.findAll();
        EventTicketStock foundEventTicketStock = eventTicketStockRepository.findById(eventTicketStock.getId()).get();
        assertThat(payments).hasSize(10);
        assertThat(foundEventTicketStock.getStock()).isZero();
    }

    @Nested
    @DisplayName("결제 생성 시")
    class CreatePayment {

        @Test
        @DisplayName("결제가 정상적으로 생성된다.")
        void createPayment() {
            // given
            User user = UserTestData.testUser();
            Point point = Point.of(10_000, user);
            Event event = Event.of(getCreateEventRequestDto(), user);
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            userRepository.save(user);
            pointRepository.save(point);
            eventRepository.save(event);
            eventTicketStockRepository.save(eventTicketStock);

            // when
            long paymentId = paymentService.createPayment(user.getId(), event.getId(), "cid", null, null);

            // then
            List<Payment> payments = paymentRepository.findAll();
            assertThat(payments).hasSize(1);

            Payment payment = paymentRepository.findByIdOrElseThrow(paymentId);
            assertThat(payment.getEvent().getId()).isEqualTo(event.getId());
            assertThat(payment.getUser().getId()).isEqualTo(user.getId());
            assertThat(payment.getOriginalAmount()).isEqualTo(event.getPrice());
            assertThat(payment.getDiscountedAmount()).isEqualTo(event.getPrice());
        }

        @Test
        @DisplayName("포인트가 부족하면 결제를 생성할 수 없다.")
        void createPaymentWithNotEnoughPointAmount() {
            User user = UserTestData.testUser();
            Point point = Point.of(0, user);
            Event event = Event.of(getCreateEventRequestDto(), user);
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            userRepository.save(user);
            pointRepository.save(point);
            eventRepository.save(event);
            eventTicketStockRepository.save(eventTicketStock);

            // when & then
            assertThatThrownBy(() -> paymentService.createPayment(user.getId(), event.getId(), "cid", null, 1_000))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(NOT_ENOUGH_POINT_AMOUNT.getMessage());
        }
    }

    @Nested
    @DisplayName("결제 준비 시")
    class ReadyPayment {

        @Test
        @DisplayName("결제 상태가 READY로 변경된다")
        void readyPaymentREADY() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            CreateEventRequestDto requestDto = getCreateEventRequestDto();
            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            Point point = pointRepository.save(Point.of(0, user));

            Payment payment = paymentRepository.save(getPendingPayment(event, user, requestDto.getPrice(), point));
            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createReadyEvent(payment.getId()));

            // when
            paymentService.readyPayment(payment.getId(), "tid", paymentEvent.getId());

            // then
            Payment foundPayment = paymentRepository.findByIdOrElseThrow(payment.getId());
            assertThat(foundPayment.getStatus()).isEqualTo(READY);
        }

        @Test
        @DisplayName("Tid가 저장된다.")
        void readyPaymentTid() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            CreateEventRequestDto requestDto = getCreateEventRequestDto();
            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            Point point = pointRepository.save(Point.of(0, user));

            Payment payment = paymentRepository.save(getPendingPayment(event, user, requestDto.getPrice(), point));
            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createReadyEvent(payment.getId()));

            // when
            paymentService.readyPayment(payment.getId(), "tid", paymentEvent.getId());

            // then
            Payment foundPayment = paymentRepository.findByIdOrElseThrow(payment.getId());
            assertThat(foundPayment.getTid()).isEqualTo("tid");
        }

        @Test
        @DisplayName("성공적으로 동작한다면 결제 준비 이벤트가 삭제된다.")
        void readyPaymentAboutPaymentEvent() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getPendingPayment(event, user, 10_000, initialPoint));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));

            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createReadyEvent(payment.getId()));

            // when
            paymentService.readyPayment(payment.getId(), "tid", paymentEvent.getId());

            // then
            assertThat(paymentEventRepository.findById(paymentEvent.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("결제 승인 시")
    class ApprovePayment {

        @Test
        @DisplayName("결제 상태가 APPROVED로 변경된다")
        void approvePaymentWithStatus() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));

            Payment payment = paymentRepository.save(getReadyPayment(event, user, 10_000, initialPoint));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));

            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createApproveEvent(payment.getId()));

            // when
            paymentService.approvePayment(payment.getId(), paymentEvent.getId());

            // then
            Payment foundPayment = paymentRepository.findByIdOrElseThrow(payment.getId());
            assertThat(foundPayment.getStatus()).isEqualTo(APPROVED);
        }

        @Test
        @DisplayName("포인트가 사용되었다면 포인트가 감소하고 포인트 기록이 저장된다.")
        void approvePaymentWithPoint() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getReadyPayment(event, user, 10_000, initialPoint));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));
            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createApproveEvent(payment.getId()));

            // when
            paymentService.approvePayment(payment.getId(), paymentEvent.getId());

            // then
            Point foundPoint = pointRepository.findById(initialPoint.getId()).get();
            assertThat(foundPoint.getAmount()).isEqualTo(initialPoint.getAmount() - payment.getUsedPointAmount());

            PointHistory pointHistory = pointHistoryRepository.findByPointOrElseThrow(foundPoint);
            assertThat(pointHistory.getAmount()).isEqualTo(payment.getUsedPointAmount());
        }

        @Test
        @DisplayName("성공적으로 동작한다면 결제 승인 이벤트가 삭제된다.")
        void approvePaymentAboutPaymentEvent() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getReadyPayment(event, user, 10_000, initialPoint));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));

            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createApproveEvent(payment.getId()));

            // when
            paymentService.approvePayment(payment.getId(), paymentEvent.getId());

            // then
            assertThat(paymentEventRepository.findById(paymentEvent.getId())).isEmpty();
        }

        @Test
        @DisplayName("티켓이 발급된다.")
        void approvePaymentWithTicket() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getReadyPayment(event, user, 10_000, initialPoint));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));

            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createApproveEvent(payment.getId()));

            // when
            paymentService.approvePayment(payment.getId(), paymentEvent.getId());

            // then
            List<Ticket> tickets = ticketRepository.findAll();
            assertThat(tickets).hasSize(1);

            Ticket ticket = tickets.get(0);
            assertThat(ticket.getEvent().getId()).isEqualTo(event.getId());
            assertThat(ticket.getUser().getId()).isEqualTo(user.getId());
            assertThat(ticket.isCanceled()).isFalse();
        }

    }

    @Nested
    @DisplayName("결제 취소 시")
    class CancelPayment {

        @Test
        @DisplayName("결제 상태가 CANCELED로 변경된다")
        void cancelPaymentWithStatus() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createCancelEvent(payment.getId()));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));

            ticketRepository.save(Ticket.create(user, event, event.getPrice()));

            // when
            paymentService.cancelPayment(payment.getId(), paymentEvent.getId());

            // then
            assertThat(paymentEventRepository.findById(paymentEvent.getId())).isEmpty();
        }

        @Test
        @DisplayName("발급된 티켓이 취소된다.")
        void cancelTicket() {
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createCancelEvent(payment.getId()));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));

            ticketRepository.save(Ticket.create(user, event, event.getPrice()));

            // when
            paymentService.cancelPayment(payment.getId(), paymentEvent.getId());

            // then
            List<Ticket> tickets = ticketRepository.findAll();
            assertThat(tickets).hasSize(1);
            Ticket ticket = tickets.get(0);
            assertThat(ticket.isCanceled()).isTrue();
        }

        @Test
        @DisplayName("모집 기간이 지난 이벤트의 결제는 취소할 수 없다.")
        void cancelPaymentWithFinishedRecruitment() {
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDtoWithFinishedRecruitment(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createCancelEvent(payment.getId()));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));

            ticketRepository.save(Ticket.create(user, event, event.getPrice()));

            // when & then
            assertThatThrownBy(() -> paymentService.cancelPayment(payment.getId(), paymentEvent.getId()))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(CANCELLATION_PERIOD_EXPIRED.getMessage());
        }

        @Test
        @DisplayName("포인트를 사용했다면 포인트 사용기록이 CANCELED로 변경된다.")
        void cancelPaymentWithPointHistory() {
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createCancelEvent(payment.getId()));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));

            ticketRepository.save(Ticket.create(user, event, event.getPrice()));

            // when
            paymentService.cancelPayment(payment.getId(), paymentEvent.getId());

            // then
            PointHistory pointHistory = pointHistoryRepository.findByPaymentOrElseThrow(payment);
            assertThat(pointHistory.getStatus()).isEqualTo(PointHistoryStatus.CANCELED);
        }

        @Test
        @DisplayName("재고가 원상복구된다.")
        void cancelPaymentWithStock() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStock.decreaseStock();
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createCancelEvent(payment.getId()));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));

            ticketRepository.save(Ticket.create(user, event, event.getPrice()));

            // when
            paymentService.cancelPayment(payment.getId(), paymentEvent.getId());

            // then
            EventTicketStock foundEventTicketStock
                    = eventTicketStockRepository.findById(eventTicketStock.getId()).get();
            assertThat(foundEventTicketStock.getStock()).isEqualTo(event.getMaxPeople());
        }

        @Test
        @DisplayName("성공적으로 동작한다면 결제 승인 이벤트가 삭제된다.")
        void cancelPaymentAboutPaymentEvent() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStockRepository.save(eventTicketStock);
            ticketRepository.save(Ticket.create(user, event, event.getPrice()));

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));

            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createCancelEvent(payment.getId()));

            // when
            paymentService.cancelPayment(payment.getId(), paymentEvent.getId());

            // then
            assertThat(paymentEventRepository.findById(paymentEvent.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("특정 이벤트에 대한 결제 취소 시")
    class CancelPaymentByEvent {

        @Test
        @DisplayName("이벤트에 대한 모든 결제가 취소된다.")
        void cancelPaymentsByEvent() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment1 = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            Payment payment2 = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            pointHistoryRepository.save(PointHistory.create(payment1, initialPoint, payment1.getUsedPointAmount()));
            pointHistoryRepository.save(PointHistory.create(payment2, initialPoint, payment2.getUsedPointAmount()));

            ticketRepository.save(Ticket.create(user, event, event.getPrice()));

            // when
            paymentService.cancelPayments(event);

            // then
            List<Payment> payments = paymentRepository.findAll();
            payments.forEach(payment -> {
                assertThat(payment.getStatus()).isEqualTo(CANCELED);
            });
        }

        @Test
        @DisplayName("발급된 티켓이 취소된다.")
        void cancelTicket() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));

            ticketRepository.save(Ticket.create(user, event, event.getPrice()));

            // when
            paymentService.cancelPayments(event);

            // then
            List<Ticket> tickets = ticketRepository.findAll();
            assertThat(tickets).hasSize(1);
            Ticket ticket = tickets.get(0);
            assertThat(ticket.isCanceled()).isTrue();
        }

        @Test
        @DisplayName("포인트를 사용했다면 포인트 사용기록이 CANCELED로 변경된다.")
        void cancelPaymentWithPointHistory() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));

            ticketRepository.save(Ticket.create(user, event, event.getPrice()));

            // when
            paymentService.cancelPayments(event);

            // then
            PointHistory pointHistory = pointHistoryRepository.findByPaymentOrElseThrow(payment);
            assertThat(pointHistory.getStatus()).isEqualTo(PointHistoryStatus.CANCELED);
        }

        @Test
        @DisplayName("재고가 원상복구된다.")
        void cancelPaymentWithStock() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStock.decreaseStock(2);
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment1 = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            Payment payment2 = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            pointHistoryRepository.save(PointHistory.create(payment1, initialPoint, payment1.getUsedPointAmount()));
            pointHistoryRepository.save(PointHistory.create(payment2, initialPoint, payment2.getUsedPointAmount()));

            ticketRepository.save(Ticket.create(user, event, event.getPrice()));

            // when
            paymentService.cancelPayments(event);

            // then
            EventTicketStock foundEventTicketStock
                    = eventTicketStockRepository.findById(eventTicketStock.getId()).get();
            assertThat(foundEventTicketStock.getStock()).isEqualTo(event.getMaxPeople());
        }

    }

    @Nested
    @DisplayName("결제 실패 시")
    class FailPayment {

        @Test
        @DisplayName("결제 상태가 FAILED로 변경된다.")
        void failPayment() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));

            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createApproveEvent(payment.getId()));

            ticketRepository.save(Ticket.create(user, event, event.getPrice()));

            // when
            paymentService.failPayment(payment.getId(), paymentEvent.getId());

            // then
            Payment foundPayment = paymentRepository.findByIdOrElseThrow(payment.getId());
            assertThat(foundPayment.getStatus()).isEqualTo(FAILED);
        }

        @Test
        @Transactional
        @DisplayName("재고가 원상복구된다.")
        void failPaymentStock() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStock.decreaseStock();
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            pointHistoryRepository.save(PointHistory.create(payment, initialPoint, payment.getUsedPointAmount()));

            PaymentEvent paymentEvent = paymentEventRepository.save(PaymentEvent.createApproveEvent(payment.getId()));

            ticketRepository.save(Ticket.create(user, event, event.getPrice()));

            // when
            paymentService.failPayment(payment.getId(), paymentEvent.getId());

            // then
            EventTicketStock foundEventTicketStock = eventTicketStockRepository.findByEventOrElseThrow(event);
            assertThat(foundEventTicketStock.getStock()).isEqualTo(event.getMaxPeople());
        }
    }

    @Nested
    @DisplayName("결제 조회 시")
    class GetPayment {

        @Test
        @DisplayName("결제 ID로 결제를 단건 조회할 수 있다")
        void getPaymentByPaymentId() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStock.decreaseStock();
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));

            // when
            PaymentResponseDto responseDto = paymentService.getPayment(payment.getId());

            // then
            assertThat(responseDto.getPaymentId()).isEqualTo(payment.getId());
            assertThat(responseDto.getEventId()).isEqualTo(event.getId());
            assertThat(responseDto.getUserId()).isEqualTo(user.getId());
            assertThat(responseDto.getOriginalAmount()).isEqualTo(payment.getOriginalAmount());
            assertThat(responseDto.getDiscountedAmount()).isEqualTo(payment.getDiscountedAmount());
            assertThat(responseDto.getStatus()).isEqualTo(payment.getStatus());
        }

        @Test
        @DisplayName("결제 Tid로 결제를 단건 조회할 수 있다")
        void getPaymentByTid() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStock.decreaseStock();
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));

            // when
            PaymentResponseDto responseDto = paymentService.getPayment(payment.getTid());

            // then
            assertThat(responseDto.getPaymentId()).isEqualTo(payment.getId());
            assertThat(responseDto.getEventId()).isEqualTo(event.getId());
            assertThat(responseDto.getUserId()).isEqualTo(user.getId());
            assertThat(responseDto.getOriginalAmount()).isEqualTo(payment.getOriginalAmount());
            assertThat(responseDto.getDiscountedAmount()).isEqualTo(payment.getDiscountedAmount());
            assertThat(responseDto.getStatus()).isEqualTo(payment.getStatus());
        }

        @Test
        @DisplayName("특정 유저의 결제를 다건 조회할 수 있다.")
        void getPayments() {
            // given
            User user = UserTestData.testUser();
            userRepository.save(user);

            Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
            EventTicketStock eventTicketStock = EventTicketStock.create(event);
            eventTicketStock.decreaseStock();
            eventTicketStockRepository.save(eventTicketStock);

            Point initialPoint = pointRepository.save(Point.of(10_000, user));
            Payment payment1 = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));
            Payment payment2 = paymentRepository.save(getApprovedPayment(event, user, 10_000, initialPoint));

            // when
            Page<PaymentResponseDto> paymentResponseDtos =
                    paymentService.getPayments(user.getId(), PageRequest.of(0, 10));

            // then
            assertThat(paymentResponseDtos.getContent()).hasSize(2)
                    .extracting(PaymentResponseDto::getPaymentId, PaymentResponseDto::getUserId, PaymentResponseDto::getPaymentId)
                    .containsExactly(
                            tuple(payment1.getId(), user.getId(), payment1.getId()),
                            tuple(payment2.getId(), user.getId(), payment2.getId())
                    );
        }
    }

    Payment getReadyPayment(Event event, User user, int price, Point point) {
        return Payment.create("cid", event, user, price, price, null, point, 0);
    }

    Payment getApprovedPayment(Event event, User user, int price, Point point) {
        Payment payment = Payment.create(
                "cid",
                event,
                user,
                price,
                price,
                null,
                point,
                1_000
        );
        payment.approve();
        return payment;
    }

    Payment getPendingPayment(Event event, User user, int price, Point point) {
        return Payment.create("cid", event, user, price, price, null, point, 0);
    }

    CreateEventRequestDto getCreateEventRequestDto() {
        return CreateEventRequestDto.of("title", "content", "location",
                LocalDateTime.of(2024, 10, 10, 10, 0),
                LocalDateTime.of(2024, 10, 10, 12, 0),
                109, 10_000, ART,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1));
    }

    CreateEventRequestDto getCreateEventRequestDtoWithFinishedRecruitment() {
        return CreateEventRequestDto.of("title", "content", "location",
                LocalDateTime.of(2024, 10, 10, 10, 0),
                LocalDateTime.of(2024, 10, 10, 12, 0),
                109, 10_000, ART,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1));
    }

    KakaopayPaymentResponseDto createKakaopayPaymentResponseDto() {
        return KakaopayPaymentResponseDto.ofSuccess(new KakaopayReadyPaymentResponseDto(
                "tid",
                "url",
                "url",
                "url",
                "url",
                "url",
                LocalDateTime.now()
        ));
    }

    CreateEventRequestDto getCreateEventRequestDtoWithMaxPeople(int maxPeople) {
        return CreateEventRequestDto.of("title", "content", "location",
                LocalDateTime.of(2024, 12, 12, 12, 0),
                LocalDateTime.of(2024, 12, 12, 14, 0), maxPeople, 10_000,
                EventCategory.ART, LocalDateTime.of(2024, 10, 10, 0, 0),
                LocalDateTime.of(2024, 12, 10, 0, 0));
    }

}
