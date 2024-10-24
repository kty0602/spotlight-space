package com.spotlightspace.core.event.dto;

import com.spotlightspace.core.event.domain.EventCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddEventRequestDto {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    @NotBlank(message = "장소는 필수입니다.")
    private String location;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    @NotNull(message = "인원제한은 필수입니다.")
    private int maxPeople;
    @NotNull(message = "가격을 입력해주세요!")
    private int price;
    private EventCategory category;
    private LocalDateTime recruitmentStartAt;
    private LocalDateTime recruitmentFinishAt;

}
