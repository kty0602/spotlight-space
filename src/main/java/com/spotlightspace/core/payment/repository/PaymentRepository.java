package com.spotlightspace.core.payment.repository;

import static com.spotlightspace.common.exception.ErrorCode.TID_NOT_FOUND;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.payment.domain.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentQueryRepository {

    @Query("select p from Payment p left join fetch p.userCoupon where p.tid = :tid")
    Optional<Payment> findByTid(@Param("tid") String tid);

    long countByEvent(Event event);

    default Payment findByTidOrElseThrow(String tid) {
        return findByTid(tid).orElseThrow(() -> new ApplicationException(TID_NOT_FOUND));
    }
}
