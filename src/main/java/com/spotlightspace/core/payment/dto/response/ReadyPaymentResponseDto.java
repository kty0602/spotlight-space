package com.spotlightspace.core.payment.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReadyPaymentResponseDto {

    /**
     * 결제 고유 번호
     */
    @NotBlank(message = "결제 고유 번호는 필수 값입니다.")
    private final String tid;

    /**
     * 모바일 앱일 경우 카카오톡 결제 페이지 Redirect URL
     */
    @NotBlank(message = "모바일 앱일 경우 카카오톡 결제 페이지 Redirect URL은 필수 값입니다.")
    private final String nextRedirectAppUrl;

    /**
     * 모바일 웹일 경우 카카오톡 결제 페이지 Redirect URL
     */
    @NotBlank(message = "모바일 웹일 경우 카카오톡 결제 페이지 Redirect URL은 필수 값입니다.")
    private final String nextRedirectMobileUrl;

    /**
     * PC 웹일 경우 카카오톡으로 결제 요청 메시지(TMS)를 보내기 위한 사용자 정보 입력 화면 Redirect URL
     */
    @NotBlank(message = "PC 웹일 경우 카카오톡으로 결제 요청 메시지를 보내기 위한 사용자 정보 입력 화면 Redirect URL은 필수 값입니다.")
    private final String nextRedirectPcUrl;

    /**
     * 카카오페이 결제 화면으로 이동하는 Android 앱 스킴(Scheme) - 내부 서비스용
     */
    @NotBlank(message = "카카오페이 결제 화면으로 이동하는 Android 앱 스킴은 필수 값입니다.")
    private final String androidAppScheme;

    /**
     * 카카오페이 결제 화면으로 이동하는 iOS 앱 스킴 - 내부 서비스용
     */
    @NotBlank(message = "카카오페이 결제 화면으로 이동하는 iOS 앱 스킴는 필수 값입니다.")
    private final String iosAppScheme;

    /**
     * 결제 준비 요청 시간
     */
    @NotNull(message = "결제 준비 요청 시간은 필수 값입니다.")
    private final LocalDateTime createdAt;
}
