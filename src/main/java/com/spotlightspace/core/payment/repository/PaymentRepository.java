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

    default Payment findByTidOrElseThrow(String tid) {
        return findByTid(tid).orElseThrow(() -> new ApplicationException(TID_NOT_FOUND));
    }

    @Query("select p from Payment p where p.event = :event and p.status = :status")
    List<Payment> findPaymentsByEventAndStatus(@Param("event") Event event, @Param("status") PaymentStatus status);

    List<Payment> findAllByStatusAndUpdateAtBefore(PaymentStatus paymentStatus, LocalDateTime failDateTime);
}
