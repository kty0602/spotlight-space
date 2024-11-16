package com.spotlightspace.core.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentStatusTest {

    @Test
    @DisplayName("결제 상태에 해당하는 PaymentStatus를 반환한다.")
    void getPaymentStatus() {
        // given
        String paymentStatusName = "PENDING";

        // when
        PaymentStatus paymentStatus = PaymentStatus.of(paymentStatusName);

        // then
        assertThat(paymentStatus).isEqualTo(PaymentStatus.PENDING);
    }
}
