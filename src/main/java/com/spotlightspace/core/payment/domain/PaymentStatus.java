package com.spotlightspace.core.payment.domain;

import static com.spotlightspace.common.exception.ErrorCode.INVALID_PAYMENT_STATUS;

import com.spotlightspace.common.exception.ApplicationException;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    PENDING("결제 보류"),
    READY("결제 준비"),
    APPROVED("결제 승인"),
    CANCELED("결제 취소"),
    FAILED("결제 실패");

    private final String description;

    public static PaymentStatus of(String status) {
        return Arrays.stream(PaymentStatus.values())
                .filter(s -> s.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(INVALID_PAYMENT_STATUS));
    }
}
