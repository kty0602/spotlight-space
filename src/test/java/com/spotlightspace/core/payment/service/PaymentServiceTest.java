package com.spotlightspace.core.payment.service;

import static com.spotlightspace.core.event.domain.EventCategory.ART;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.anyInt;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;

import com.spotlightspace.core.data.UserTestData;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.domain.EventCategory;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.eventticketstock.domain.EventTicketStock;
import com.spotlightspace.core.eventticketstock.repository.EventTicketStockRepository;
import com.spotlightspace.core.payment.domain.Payment;
import com.spotlightspace.core.payment.dto.response.ReadyPaymentResponseDto;
import com.spotlightspace.core.payment.repository.PaymentRepository;
import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.point.repository.PointRepository;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
    private PointRepository pointRepository;

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAllInBatch();
        eventTicketStockRepository.deleteAllInBatch();
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
                .willReturn(createReadyPaymentResponseDto());

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


    @Test
    @DisplayName("특정 유저의 결제 내역을 조회할 수 있다.")
    void findPaymentsWithUserId() {
        // given
        User user = UserTestData.testUser();
        userRepository.save(user);

        CreateEventRequestDto requestDto = getCreateEventRequestDto();
        Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
        Point point = pointRepository.save(Point.of(0, user));

        Payment approvedPayment = getApprovedPayment(event, user, requestDto.getPrice(), point);
        Payment readyPayment = getReadyPayment(event, user, requestDto.getPrice(), point);
        Payment canceledPayment = getCanceledPayment(event, user, requestDto.getPrice(), point);
        Payment failedPayment = getFailedPayment(event, user, requestDto.getPrice(), point);
        paymentRepository.saveAll(List.of(approvedPayment, readyPayment, canceledPayment, failedPayment));

        // when
        Page<Payment> payments = paymentRepository.findAllByUserId(user.getId(), PageRequest.of(0, 10));

        // then
        assertThat(payments).hasSize(4);
        assertThat(payments.getTotalElements()).isEqualTo(4);
        assertThat(payments.getTotalPages()).isEqualTo(1);
    }

    Payment getFailedPayment(Event event, User user, int price, Point point) {
        Payment payment = Payment.create("cid", event, user, price, price, null, point, 0);
        payment.fail();
        return payment;
    }

    Payment getCanceledPayment(Event event, User user, int price, Point point) {
        Payment payment = Payment.create(
                "cid",
                event,
                user,
                price,
                price,
                null,
                point,
                0
        );
        payment.cancel();
        return payment;
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
                0
        );
        payment.approve();
        return payment;
    }

    CreateEventRequestDto getCreateEventRequestDto() {
        return CreateEventRequestDto.of("title", "content", "location",
                LocalDateTime.of(2024, 10, 10, 10, 0),
                LocalDateTime.of(2024, 10, 10, 12, 0),
                109, 10_000, ART,
                LocalDateTime.of(2024, 10, 1, 0, 0),
                LocalDateTime.of(2024, 10, 5, 10, 0));
    }

    ReadyPaymentResponseDto createReadyPaymentResponseDto() {
        return new ReadyPaymentResponseDto(
                "tid",
                "url",
                "url",
                "url",
                "url",
                "url",
                LocalDateTime.now()
        );
    }

    CreateEventRequestDto getCreateEventRequestDtoWithMaxPeople(int maxPeople) {
        return CreateEventRequestDto.of("title", "content", "location",
                LocalDateTime.of(2024, 12, 12, 12, 0),
                LocalDateTime.of(2024, 12, 12, 14, 0), maxPeople, 10_000,
                EventCategory.ART, LocalDateTime.of(2024, 10, 10, 0, 0),
                LocalDateTime.of(2024, 12, 10, 0, 0));
    }

}
