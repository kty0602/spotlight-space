package com.spotlightspace.core.user.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetSettlementResponseDto {

    private Integer totalAmount;

    public static GetSettlementResponseDto from(Integer totalAmount) {
        return new GetSettlementResponseDto(totalAmount);
    }
}
