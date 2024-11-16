package com.spotlightspace.core.event.repository;

import static com.spotlightspace.common.exception.ErrorCode.EVENT_NOT_FOUND;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.event.domain.Event;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface EventRepository extends JpaRepository<Event, Long>, EventQueryRepository {

    Optional<Event> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);

    Optional<Event> findByIdAndIsDeletedFalse(Long id);

    default Event findByIdOrElseThrow(long id) {
        return findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ApplicationException(EVENT_NOT_FOUND));
    }

    default Event findByIdAndUserIdOrElseThrow(Long id, Long userId) {
        return findByIdAndUserIdAndIsDeletedFalse(id, userId)
                .orElseThrow(() -> new ApplicationException(EVENT_NOT_FOUND));
    }

    @Query("SELECT Count(e) FROM Event e "
            + "WHERE e.user.id = :userId "
            + "AND e.recruitmentStartAt < CURRENT_TIMESTAMP AND e.endAt > CURRENT_TIMESTAMP")
    int existEvent(Long userId);

    @Modifying
    @Query("UPDATE Event e SET e.isDeleted = true WHERE e.user.id = :userId")
    void deleteByUserId(Long userId);

    @Query("SELECT COUNT(e) FROM Event e "
            + "WHERE e.user.id = :userId AND e.isDeleted = false And e.isCalculated = false")
    int existCalculation(Long userId);
}
