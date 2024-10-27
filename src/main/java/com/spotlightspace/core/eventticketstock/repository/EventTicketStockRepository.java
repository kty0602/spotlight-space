package com.spotlightspace.core.eventticketstock.repository;

import static com.spotlightspace.common.exception.ErrorCode.EVENT_TICKET_STOCK_NOT_FOUND;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.eventticketstock.domain.EventTicketStock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventTicketStockRepository extends JpaRepository<EventTicketStock, Long> {

    Optional<EventTicketStock> findByEvent(Event event);

    default EventTicketStock findByEventOrElseThrow(Event event) {
        return findByEvent(event).orElseThrow(() -> new ApplicationException(EVENT_TICKET_STOCK_NOT_FOUND));
    }
}
