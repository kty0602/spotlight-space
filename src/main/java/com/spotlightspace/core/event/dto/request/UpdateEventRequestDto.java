package com.spotlightspace.core.event.dto.request;

import com.spotlightspace.core.event.domain.EventCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
public class UpdateEventRequestDto {
    private String title;
    private String content;
    private String location;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer maxPeople;
    private Integer price;
    private EventCategory category;
    private LocalDateTime recruitmentStartAt;
    private LocalDateTime recruitmentFinishAt;

    private UpdateEventRequestDto(String title, String content, String location,
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

    public static UpdateEventRequestDto of(String title, String content, String location,
                                           LocalDateTime startAt, LocalDateTime endAt,
                                           Integer maxPeople, Integer price, EventCategory category,
                                           LocalDateTime recruitmentStartAt, LocalDateTime recruitmentFinishAt) {
        return new UpdateEventRequestDto(title, content, location, startAt, endAt, maxPeople, price, category,
                recruitmentStartAt, recruitmentFinishAt);
    }
}
