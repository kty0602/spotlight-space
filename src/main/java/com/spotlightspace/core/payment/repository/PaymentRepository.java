package com.spotlightspace.core.payment.repository;

import com.spotlightspace.core.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentQueryRepository {

}
