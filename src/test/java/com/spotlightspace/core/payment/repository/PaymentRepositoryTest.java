package com.spotlightspace.core.payment.repository;

import static com.spotlightspace.core.event.domain.EventCategory.ART;
import static com.spotlightspace.core.payment.domain.PaymentStatus.APPROVED;
import static org.assertj.core.api.Assertions.assertThat;

import com.spotlightspace.core.data.UserTestData;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.payment.domain.Payment;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PaymentRepositoryTest {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("특정 이벤트에 해당하는 결제들 중 특정 상태를 가진 결제들을 조회할 수 있다.")
    void findPaymentsByEventAndStatus() {
        // given
        User user = UserTestData.testUser();
        userRepository.save(user);

        CreateEventRequestDto requestDto = getCreateEventRequestDto();
        Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));

        Payment approvedPayment = getApprovedPayment(event, user, requestDto.getPrice());
        Payment readyPayment = getReadyPayment(event, user, requestDto.getPrice());
        Payment canceledPayment = getCanceledPayment(event, user, requestDto.getPrice());
        Payment failedPayment = getFailedPayment(event, user, requestDto.getPrice());
        paymentRepository.saveAll(List.of(approvedPayment, readyPayment, canceledPayment, failedPayment));

        // when
        List<Payment> payments = paymentRepository.findPaymentsByEventAndStatus(event, APPROVED);

        // then
        assertThat(payments).hasSize(1);
    }

    Payment getFailedPayment(Event event, User user, int price) {
        Payment payment = Payment.create("cid", event, user, price, price, null, null);
        payment.fail();
        return payment;
    }

    Payment getCanceledPayment(Event event, User user, int price) {
        Payment payment = Payment.create("cid", event, user, price, price, null, null);
        payment.cancel();
        return payment;
    }

    Payment getReadyPayment(Event event, User user, int price) {
        return Payment.create("cid", event, user, price, price, null, null);
    }

    Payment getApprovedPayment(Event event, User user, int price) {
        Payment payment = Payment.create("cid", event, user, price, price, null, null);
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
}
