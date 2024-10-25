package com.spotlightspace.core.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ReadyPaymentRequestDto {

    @NotNull(message = "이벤트 ID는 필수 값입니다.")
    private Long eventId;

    private Long couponId;
}
