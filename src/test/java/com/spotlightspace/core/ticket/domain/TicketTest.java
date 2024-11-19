package com.spotlightspace.core.ticket.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.spotlightspace.core.data.UserTestData;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.domain.EventCategory;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.user.domain.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TicketTest {

    @Test
    @DisplayName("티켓 생성 시 isCanceled는 false로 초기화된다.")
    void createTicket() {
        // given
        User user = UserTestData.testUser();
        CreateEventRequestDto createEventRequestDto = CreateEventRequestDto.of(
                "title",
                "content",
                "location",
                LocalDateTime.now(),
                LocalDateTime.now(),
                10,
                10_000,
                EventCategory.ART,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Event event = Event.of(createEventRequestDto, user);

        // when
        Ticket ticket = Ticket.create(user, event, 10_000);

        // then
        assertThat(ticket.isCanceled()).isFalse();
    }

    @Test
    @DisplayName("티켓을 취소하면 isCanceled가 true로 변경된다.")
    void cancelTicket() {
        User user = UserTestData.testUser();
        CreateEventRequestDto createEventRequestDto = CreateEventRequestDto.of(
                "title",
                "content",
                "location",
                LocalDateTime.now(),
                LocalDateTime.now(),
                10,
                10_000,
                EventCategory.ART,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Event event = Event.of(createEventRequestDto, user);
        Ticket ticket = Ticket.create(user, event, 10_000);

        // when
        ticket.cancel();

        // then
        assertThat(ticket.isCanceled()).isTrue();
    }
    
}
