package com.spotlightspace.core.paymentevent.repository;

import static com.spotlightspace.core.event.domain.EventCategory.ART;
import static com.spotlightspace.core.paymentevent.domain.PaymentEventType.READY;
import static org.assertj.core.api.Assertions.assertThat;

import com.spotlightspace.core.data.UserTestData;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.payment.domain.Payment;
import com.spotlightspace.core.payment.repository.PaymentRepository;
import com.spotlightspace.core.paymentevent.domain.PaymentEvent;
import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.point.repository.PointRepository;
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
class PaymentEventRepositoryTest {

    @Autowired
    PaymentEventRepository paymentEventRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    PointRepository pointRepository;

    @AfterEach
    void tearDown() {
        paymentEventRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("특정 시간 이후에 생성된 특정 타입의 PaymentEvent를 조회할 수 있다.")
    void findAllByTypeAndCreatedAtBefore() {
        // given
        User user = UserTestData.testUser();
        Event event = Event.of(getCreateEventRequestDto(), user);
        Point point = Point.of(0, user);
        Payment payment = Payment.create("cid", event, user, 10_000, 10_000, null, point, 0);
        userRepository.save(user);
        eventRepository.save(event);
        pointRepository.save(point);
        Long paymentId = paymentRepository.save(payment).getId();

        PaymentEvent readyEvent = PaymentEvent.createReadyEvent(paymentId);
        PaymentEvent approveEvent = PaymentEvent.createApproveEvent(paymentId);
        PaymentEvent cancelEvent = PaymentEvent.createCancelEvent(paymentId);

        paymentEventRepository.saveAll(List.of(readyEvent, approveEvent, cancelEvent));

        // when
        List<PaymentEvent> readyEvents
                = paymentEventRepository.findAllByTypeAndCreatedAtBefore(READY, LocalDateTime.now().plusMinutes(1));

        // then
        assertThat(readyEvents).hasSize(1);
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
