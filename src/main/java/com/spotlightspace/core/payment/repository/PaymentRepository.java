package com.spotlightspace.core.payment.repository;

import static com.spotlightspace.common.exception.ErrorCode.TID_NOT_FOUND;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.payment.domain.Payment;
import com.spotlightspace.core.payment.domain.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentQueryRepository {

    @Query("select p from Payment p left join fetch p.userCoupon where p.tid = :tid")
    Optional<Payment> findByTid(@Param("tid") String tid);

    @Query("select p " +
            "from Payment p " +
            "join fetch p.event e " +
            "join fetch e.user " +
            "where p.status = :status and :startInclusive <= e.endAt and e.endAt < :endExclusive and e.isDeleted = false")
    List<Payment> findPaymentsForCalculation(
            @Param("status") PaymentStatus status,
            @Param("startInclusive") LocalDateTime startInclusive,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    default Payment findByTidOrElseThrow(String tid) {
        return findByTid(tid).orElseThrow(() -> new ApplicationException(TID_NOT_FOUND));
    }

    @Query("select p from Payment p where p.event = :event and p.status = :status")
    List<Payment> findPaymentsByEventAndStatus(@Param("event") Event event, @Param("status") PaymentStatus status);

    List<Payment> findAllByStatusAndUpdateAtBefore(PaymentStatus paymentStatus, LocalDateTime failDateTime);
}
