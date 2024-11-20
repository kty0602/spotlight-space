package com.spotlightspace.core.ticket.service;

import static com.spotlightspace.common.exception.ErrorCode.RESERVED_TICKET_CANCELLATION_REQUIRED;
import static com.spotlightspace.common.exception.ErrorCode.TICKET_PRICE_CANNOT_BE_NEGATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.auth.dto.request.SignUpUserRequestDto;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.domain.EventCategory;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.payment.repository.PaymentRepository;
import com.spotlightspace.core.ticket.domain.Ticket;
import com.spotlightspace.core.ticket.repository.TicketRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TicketServiceTest {

    @Autowired
    TicketService ticketService;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @AfterEach
    void tearDown() {
        ticketRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("티켓 생성 시")
    class createTicket {

        @Test
        @DisplayName("티켓 생성 시 티켓이 정상적으로 생성된다. 초기 상태는 canceled가 false이다")
        void createTicket() {
            // given
            User user = createUser();
            User artist = createArtist();
            Event event = createEvent(artist, LocalDateTime.now(), LocalDateTime.now());

            // when
            ticketService.createTicket(user, event, 10_000);

            // then
            List<Ticket> tickets = ticketRepository.findAll();
            assertThat(tickets).hasSize(1);

            Ticket ticket = tickets.get(0);
            assertThat(ticket.getEvent().getId()).isEqualTo(event.getId());
            assertThat(ticket.getUser().getId()).isEqualTo(user.getId());
            assertThat(ticket.isCanceled()).isFalse();
        }

        @Test
        @DisplayName("티켓 생성 시 티켓 가격은 음수일 수 없다.")
        void createTicketWithNegativePrice() {
            // given
            User user = createUser();
            User artist = createArtist();
            Event event = createEvent(artist, LocalDateTime.now(), LocalDateTime.now());

            // when & then
            assertThatThrownBy(() -> ticketService.createTicket(user, event, -10_000))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(TICKET_PRICE_CANNOT_BE_NEGATIVE.getMessage());
        }
    }

    @Nested
    @DisplayName("특정 사용자의 티켓 삭제 시")
    class DeleteUserTickets {

        @Test
        @DisplayName("특정 사용자의 티켓이 삭제된다.")
        void deleteUserTickets() {
            // given
            User user = createUser();
            User artist = createArtist();
            Event event = createEvent(artist, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(3));

            Ticket ticket1 = Ticket.create(user, event, 10_000);
            Ticket ticket2 = Ticket.create(user, event, 10_000);
            ticketRepository.saveAll(List.of(ticket1, ticket2));

            // when
            ticketService.deleteUserTickets(user.getId());

            // then
            List<Ticket> tickets = ticketRepository.findAll();
            assertThat(tickets).isEmpty();
        }

        @Test
        @DisplayName("사용하지 않은 티켓은 삭제할 수 없다.")
        void deleteUnUsedTicket() {
            // given
            User user = createUser();
            User artist = createArtist();
            Event event = createEvent(artist, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(3));

            Ticket ticket1 = Ticket.create(user, event, 10_000);
            Ticket ticket2 = Ticket.create(user, event, 10_000);
            ticketRepository.saveAll(List.of(ticket1, ticket2));

            // when
            assertThatThrownBy(() -> ticketService.deleteUserTickets(user.getId()))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(RESERVED_TICKET_CANCELLATION_REQUIRED.getMessage());
        }
        
    }

    Event createEvent(User artist, LocalDateTime startAt, LocalDateTime endAt) {
        CreateEventRequestDto createEventRequestDto = CreateEventRequestDto.of(
                "title",
                "content",
                "서울",
                startAt,
                endAt,
                10,
                10_000,
                EventCategory.ART,
                LocalDateTime.of(2024,10,10, 10, 10),
                LocalDateTime.of(2024, 10, 12, 10, 10)
        );
        return eventRepository.save(Event.create(createEventRequestDto, artist));
    }

    User createUser() {
        SignUpUserRequestDto requestDto = new SignUpUserRequestDto(
                "test142@email.com",
                "rawPassword",
                "nickname",
                "role_user",
                "1998-12-12",
                false,
                "010-1234-1234",
                "한국"
        );
        return userRepository.save(User.create("encryptPassword", requestDto));
    }

    User createArtist() {
        SignUpUserRequestDto requestDto = new SignUpUserRequestDto(
                "test12@email.com",
                "rawPassword",
                "nickname",
                "role_artist",
                "1998-12-12",
                false,
                "010-4321-1234",
                "한국"
        );
        return userRepository.save(User.create("encryptPassword", requestDto));
    }
}
