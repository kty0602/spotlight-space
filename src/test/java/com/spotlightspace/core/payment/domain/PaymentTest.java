package com.spotlightspace.core.payment.domain;

import static com.spotlightspace.core.payment.domain.PaymentStatus.APPROVED;
import static com.spotlightspace.core.payment.domain.PaymentStatus.PENDING;
import static com.spotlightspace.core.payment.domain.PaymentStatus.READY;
import static org.assertj.core.api.Assertions.assertThat;

import com.spotlightspace.core.auth.dto.request.SignUpUserRequestDto;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.domain.EventCategory;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.user.domain.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentTest {

    @Test
    @DisplayName("approve 호출 시 Payment 상태가 APPROVED 가 된다.")
    void approve() {
        // given
        User user = createUser();
        Event event = createEvent(user);
        Payment payment = Payment.create("cid", event, user, 10_000, 10_000, null, null);

        // when
        payment.approve();

        // then
        assertThat(payment.getStatus()).isEqualTo(APPROVED);
    }

    @Test
    @DisplayName("Payment 생성 시 초기 상태는 PENDING 이다.")
    void create() {
        // given
        User user = createUser();
        Event event = createEvent(user);
        Payment payment = Payment.create("cid", event, user, 10_000, 10_000, null, null);

        // when & then
        assertThat(payment.getStatus()).isEqualTo(PENDING);
    }


    private User createUser() {
        SignUpUserRequestDto requestDto = new SignUpUserRequestDto(
                "test142@email.com",
                "rawPassword",
                "nickname",
                "role_user",
                "1998-12-12",
                false,
                "010-1234-1234"
        );
        return User.of("encryptPassword", requestDto);
    }

    private Event createEvent(User user) {
        CreateEventRequestDto createEventRequestDto = CreateEventRequestDto.of(
                "title",
                "content",
                "서울",
                LocalDateTime.now(),
                LocalDateTime.now(),
                10,
                10_000,
                EventCategory.ART,
                LocalDateTime.of(2024,10,10, 10, 10),
                LocalDateTime.of(2024, 10, 12, 10, 10)
        );
        return Event.of(createEventRequestDto, user);
    }
}
