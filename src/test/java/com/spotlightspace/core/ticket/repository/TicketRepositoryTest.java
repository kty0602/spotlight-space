package com.spotlightspace.core.ticket.repository;

import static com.spotlightspace.common.exception.ErrorCode.TICKET_NOT_FOUND;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TicketRepositoryTest {

    @Autowired
    TicketRepository ticketRepository;

    @Test
    @DisplayName("티켓 ID에 해당하는 티켓이 없다면 예외가 발생한다.")
    void findByIdOrElseThrow() {
        // given
        long nonExistingTicketId = 1L;

        // when & then
        Assertions.assertThatThrownBy(() -> ticketRepository.findByIdOrElseThrow(nonExistingTicketId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(TICKET_NOT_FOUND.getMessage());
    }

}
