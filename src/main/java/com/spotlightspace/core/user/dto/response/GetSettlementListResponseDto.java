package com.spotlightspace.core.user.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetSettlementListResponseDto {

    private String name;
    private int totalPoints;

    private GetSettlementListResponseDto(String name, int totalPoints) {
        this.name = name;
        this.totalPoints = totalPoints;
    }

    public static GetSettlementListResponseDto of(String name, int totalPoints) {
        return new GetSettlementListResponseDto(name, totalPoints);
    }
}

