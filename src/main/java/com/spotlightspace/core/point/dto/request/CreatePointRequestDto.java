package com.spotlightspace.core.point.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreatePointRequestDto {

    private int price;

    private CreatePointRequestDto(int price) {
        this.price = price;
    }

    public static CreatePointRequestDto from(int price) {
        return new CreatePointRequestDto(price);
    }
}
