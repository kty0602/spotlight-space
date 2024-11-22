package com.spotlightspace.core.data;


import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.ticket.domain.Ticket;
import com.spotlightspace.core.user.domain.User;
import lombok.RequiredArgsConstructor;

import static com.spotlightspace.core.data.EventTestData.testEvent;
import static com.spotlightspace.core.data.UserTestData.testUser;

@RequiredArgsConstructor
public class TicketTestData {

    public static Ticket testTicket() {
        User user = testUser();
        Event event = testEvent();
        int price = 100;

        return Ticket.create(user, event, price);
    }
}
