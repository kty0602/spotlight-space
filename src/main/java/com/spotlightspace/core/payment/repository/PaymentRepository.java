package com.spotlightspace.core.payment.repository;

import static com.spotlightspace.common.exception.ErrorCode.PAYMENT_NOT_FOUND;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.payment.domain.Payment;
import com.spotlightspace.core.payment.domain.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("select p from Payment p where p.event = :event and p.status = :status")
    List<Payment> findPaymentsByEventAndStatus(@Param("event") Event event, @Param("status") PaymentStatus status);

    @Query("select p from Payment p " +
            "join fetch p.event e " +
            "join fetch p.user " +
            "left join fetch p.userCoupon " +
            "left join fetch p.point " +
            "where p.id = :paymentId")
    Optional<Payment> findById(long paymentId);

    Optional<Payment> findByTid(String tid);

    Page<Payment> findAllByUserId(long userId, PageRequest pageRequest);

    List<Payment> findAllByEvent(Event event);

    default Payment findByIdOrElseThrow(long paymentId) {
        return findById(paymentId).orElseThrow(() -> new ApplicationException(PAYMENT_NOT_FOUND));
    }

    default Payment findByTidOrElseThrow(String tid) {
        return findByTid(tid).orElseThrow(() -> new ApplicationException(PAYMENT_NOT_FOUND));
    }
}
