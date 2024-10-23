package com.spotlightspace.core.payment.domain;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("결제 대기"),
    APPROVED("결제 승인"),
    CANCELED("결제 취소");

    private final String description;

    public static PaymentStatus of(String status) {
        return Arrays.stream(PaymentStatus.values())
                .filter(s -> s.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 상태값"));
    }
}
