package com.spotlightspace.core.payment.service;

import com.spotlightspace.core.payment.domain.Payment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class PaymentCommandService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setPaymentTid(Payment payment, String tid) {
        payment.setTid(tid);
    }
}
