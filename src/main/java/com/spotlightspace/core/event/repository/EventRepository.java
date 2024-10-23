package com.spotlightspace.core.event.repository;

import com.spotlightspace.core.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long>, EventQueryRepository {

}
