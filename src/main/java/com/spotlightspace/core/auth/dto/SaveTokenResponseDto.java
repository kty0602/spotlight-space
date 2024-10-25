package com.spotlightspace.core.auth.dto;

import lombok.Getter;

@Getter
public class SaveTokenResponseDto {

    private String accessToken;
    private String refreshToken;

    private SaveTokenResponseDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static SaveTokenResponseDto of(String accessToken, String refreshToken) {
        return new SaveTokenResponseDto(accessToken, refreshToken);
    }
}
