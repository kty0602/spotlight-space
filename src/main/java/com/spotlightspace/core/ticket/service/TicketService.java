package com.spotlightspace.core.ticket.service;

import static com.spotlightspace.common.exception.ErrorCode.RESERVED_TICKET_CANCELLATION_REQUIRED;
import static com.spotlightspace.common.exception.ErrorCode.TICKET_PRICE_CANNOT_BE_NEGATIVE;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.ticket.domain.Ticket;
import com.spotlightspace.core.ticket.dto.response.TicketResponse;
import com.spotlightspace.core.ticket.repository.TicketRepository;
import com.spotlightspace.core.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketResponse createTicket(User user, Event event, int price) throws ApplicationException {
        if (isNegativePrice(price)) {
            throw new ApplicationException(TICKET_PRICE_CANNOT_BE_NEGATIVE);
        }

        Ticket ticket = Ticket.create(user, event, price);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    public void cancelTicket(User user, Event event) {
        Ticket ticket = ticketRepository.findFirstByUserAndEventOrElseThrow(user, event);
        ticket.cancel();
    }

    private boolean isNegativePrice(int price) {
        return price < 0;
    }

    public void deleteUserTickets(Long userId) {
        if (ticketRepository.existTicket(userId) > 0) {
            throw new ApplicationException(RESERVED_TICKET_CANCELLATION_REQUIRED);
        }
        ticketRepository.deleteByUserId(userId);
    }
}
