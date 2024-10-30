package com.spotlightspace.core.ticket.repository;

import com.spotlightspace.core.ticket.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.isCanceled = false")
    int countTicketByEvent(@Param("eventId") Long eventId);

    @Query("SELECT SUM(t.price) FROM Ticket t JOIN t.event e WHERE e.user.id = :userId AND t.isCanceled = false")
    int findTotalAmountByUserId(@Param("userId") Long userId);

}
