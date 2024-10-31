package com.spotlightspace.core.event.dto.request;

import com.spotlightspace.core.event.domain.EventCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
public class CreateEventRequestDto {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    @NotBlank(message = "장소는 필수입니다.")
    private String location;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    @NotNull(message = "인원제한은 필수입니다.")
    private Integer maxPeople;
    @NotNull(message = "가격을 입력해주세요!")
    private Integer price;
    private EventCategory category;
    private LocalDateTime recruitmentStartAt;
    private LocalDateTime recruitmentFinishAt;

    private CreateEventRequestDto(String title, String content, String location,
                                  LocalDateTime startAt, LocalDateTime endAt,
                                  Integer maxPeople, Integer price, EventCategory category,
                                  LocalDateTime recruitmentStartAt, LocalDateTime recruitmentFinishAt) {
        this.title = title;
        this.content = content;
        this.location = location;
        this.startAt = startAt;
        this.endAt = endAt;
        this.maxPeople = maxPeople;
        this.price = price;
        this.category = category;
        this.recruitmentStartAt = recruitmentStartAt;
        this.recruitmentFinishAt = recruitmentFinishAt;
    }

    public static CreateEventRequestDto of(String title, String content, String location,
                                           LocalDateTime startAt, LocalDateTime endAt,
                                           Integer maxPeople, Integer price, EventCategory category,
                                           LocalDateTime recruitmentStartAt, LocalDateTime recruitmentFinishAt) {
        return new CreateEventRequestDto(title, content, location, startAt, endAt, maxPeople, price, category,
                recruitmentStartAt, recruitmentFinishAt);
    }
}
