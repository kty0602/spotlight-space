package com.spotlightspace.core.event.dto.request;

import com.spotlightspace.core.event.domain.EventCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
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
}
