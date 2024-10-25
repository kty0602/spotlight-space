package com.spotlightspace.core.ticket.repository;

import com.spotlightspace.core.ticket.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

}
