package com.spotlightspace.core.user.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetCalculateResponseDto {
    private int totalAmount;

    public static GetCalculateResponseDto from(int totalAmount) {
        return new GetCalculateResponseDto(totalAmount);
    }
}
