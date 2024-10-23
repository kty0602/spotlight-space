package com.spotlightspace.core.event.dto;

import com.spotlightspace.core.event.domain.EventCategory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EventResponseDto {
    private Long id;
    private String title;
    private String content;
    private String location;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private int maxPeople;
    private int price;
    private EventCategory category;
    private LocalDateTime recruitmentStartAt;
    private LocalDateTime recruitmentFinishAt;
}
