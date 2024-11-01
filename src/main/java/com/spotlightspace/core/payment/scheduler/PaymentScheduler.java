package com.spotlightspace.core.payment.scheduler;

import static com.spotlightspace.core.payment.domain.PaymentStatus.READY;

import com.spotlightspace.core.payment.domain.Payment;
import com.spotlightspace.core.payment.repository.PaymentRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private static final int ONE_MINUTE = 60_000;
    private static final long FIFTEEN_MINUTE = 15L;

    private final PaymentRepository paymentRepository;

    @Transactional
    @Scheduled(fixedDelay = ONE_MINUTE)
    public void failPayment() {
        paymentRepository.findAllByStatusAndUpdateAtBefore(READY, LocalDateTime.now().minusMinutes(FIFTEEN_MINUTE))
                .forEach(Payment::fail);
    }
}
