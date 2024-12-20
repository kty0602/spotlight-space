package com.spotlightspace.core.payment.dto.response.kakaopay;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class KakaopayPaymentErrorResponseDto implements TidAccessible {

    /**
     * 에러 코드, ex) -780
     */
    private final int errorCode;

    /**
     * 에러 메시지, ex) approval failure!
     */
    private final String errorMessage;

    /**
     * 추가 정보
     */
    private final Extras extras;

    @Override
    public String getTid() {
        throw new ApplicationException(ErrorCode.TID_NOT_FOUND);
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Extras {

        /**
         * 결과 코드, ex) USER_LOCKED
         */
        private final String methodResultCode;

        /**
         * 결과 메시지, ex) 진행중인 거래가 있습니다. 잠시 후 다시 시도해 주세요.
         */
        private final String methodResultMessage;
    }
}
