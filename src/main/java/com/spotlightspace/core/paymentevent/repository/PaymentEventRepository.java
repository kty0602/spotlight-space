package com.spotlightspace.core.paymentevent.repository;

import com.spotlightspace.core.paymentevent.domain.PaymentEvent;
import com.spotlightspace.core.paymentevent.domain.PaymentEventType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {

    @Query("select pe from PaymentEvent pe join fetch Payment p on p.id = pe.paymentId where pe.type = :type and p.createdAt < :createdAt")
    List<PaymentEvent> findAllByTypeAndCreatedAtBefore(PaymentEventType type, LocalDateTime createdAt);
}
