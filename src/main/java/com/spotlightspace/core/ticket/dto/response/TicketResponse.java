package com.spotlightspace.core.ticket.dto.response;

import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.ticket.domain.Ticket;
import com.spotlightspace.core.user.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TicketResponse {

    private final Long id;
    private final User user;
    private final Event event;
    private final boolean isCanceled;

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(ticket.getId(), ticket.getUser(), ticket.getEvent(), ticket.isCanceled());
    }
}
