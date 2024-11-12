package com.spotlightspace.core.event.domain;

import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventElastic {
    @Id
    @Field(name = "event_id")
    private Long id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Text)
    private String location;

    // 시작 일시
    @Field(type = FieldType.Date, name = "start_at", format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startAt;

    // 종료 일시
    @Field(type = FieldType.Date, name = "end_at", format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endAt;

    @Field(type = FieldType.Integer, name = "max_people")
    private int maxPeople;

    @Field(type = FieldType.Integer)
    private int price;

    @Field(type = FieldType.Keyword)
    private EventCategory category;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recruitmentStartAt;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recruitmentFinishAt;

    @Field(type = FieldType.Boolean)
    private boolean isDeleted = false;

    @Field(type = FieldType.Boolean)
    private boolean isCalculated = false;

    @LastModifiedDate
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private EventElastic(CreateEventRequestDto createEventRequestDto, Long id) {
        this.id = id;
        this.title = createEventRequestDto.getTitle();
        this.content = createEventRequestDto.getContent();
        this.location = createEventRequestDto.getLocation();
        this.startAt = createEventRequestDto.getStartAt();
        this.endAt = createEventRequestDto.getEndAt();
        this.maxPeople = createEventRequestDto.getMaxPeople();
        this.price = createEventRequestDto.getPrice();
        this.category = createEventRequestDto.getCategory();
        this.recruitmentStartAt = createEventRequestDto.getRecruitmentStartAt();
        this.recruitmentFinishAt = createEventRequestDto.getRecruitmentFinishAt();
    }

    public static EventElastic of(CreateEventRequestDto createEventRequestDto, Long id) {
        return new EventElastic(createEventRequestDto, id);
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changeLocation(String location) {
        this.location = location;
    }

    public void changeStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public void changeEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public void changeMaxPeople(int maxPeople) {
        this.maxPeople = maxPeople;
    }

    public void changePrice(int price) {
        this.price = price;
    }

    public void changeCategory(EventCategory category) {
        this.category = category;
    }

    public void changeRecruitmentStartAt(LocalDateTime recruitmentStartAt) {
        this.recruitmentStartAt = recruitmentStartAt;
    }

    public void changeRecruitmentFinishAt(LocalDateTime recruitmentFinishAt) {
        this.recruitmentFinishAt = recruitmentFinishAt;
    }

    public void deleteEvent() {
        this.isDeleted = true;
    }
}
