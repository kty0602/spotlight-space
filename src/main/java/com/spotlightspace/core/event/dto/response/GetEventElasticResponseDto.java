package com.spotlightspace.core.event.dto.response;

import com.spotlightspace.core.event.domain.EventCategory;
import com.spotlightspace.core.event.domain.EventElastic;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetEventElasticResponseDto {

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
    private LocalDateTime updatedAt;

    private GetEventElasticResponseDto(EventElastic eventElastic) {
        this.id = eventElastic.getId();
        this.title = eventElastic.getTitle();
        this.content = eventElastic.getContent();
        this.location = eventElastic.getLocation();
        this.startAt = eventElastic.getStartAt();
        this.endAt = eventElastic.getEndAt();
        this.maxPeople = eventElastic.getMaxPeople();
        this.price = eventElastic.getPrice();
        this.category = eventElastic.getCategory();
        this.recruitmentStartAt = eventElastic.getRecruitmentStartAt();
        this.recruitmentFinishAt = eventElastic.getRecruitmentFinishAt();
        this.updatedAt = eventElastic.getUpdatedAt();
    }

    public static GetEventElasticResponseDto from(EventElastic eventElastic) {
        return new GetEventElasticResponseDto(eventElastic);
    }
}
