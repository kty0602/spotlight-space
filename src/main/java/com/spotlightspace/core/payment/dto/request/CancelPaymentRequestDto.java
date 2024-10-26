package com.spotlightspace.core.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CancelPaymentRequestDto {

    /**
     * 결제 고유 번호
     */
    @NotNull(message = "결제 고유 번호는 필수 값입니다.")
    private String tid;

    /**
     * 결제 취소 금액
     */
    @NotNull(message = "취소 금액은 필수 값입니다.")
    private Integer cancelAmount;

    /**
     * 결제 취소 비과세 금액
     */
    @NotNull(message = "취소 비과세 금액은 필수 값입니다.")
    private Integer cancelTaxFreeAmount;
}
