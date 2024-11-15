package com.spotlightspace.core.event.domain;

import com.spotlightspace.common.entity.Timestamped;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.user.domain.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Entity
@Table(name = "events")
public class Event extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(length = 100)
    private String title;

    @Column(length = 100)
    private String content;

    @Column(length = 100)
    private String location;

    // 시작 일시
    @Column(length = 50, name = "start_at")
    private LocalDateTime startAt;

    // 종료 일시
    @Column(length = 50, name = "end_at")
    private LocalDateTime endAt;

    @Column(length = 100, name = "max_people")
    private int maxPeople;

    @Column(length = 200)
    private int price;

    @Enumerated(EnumType.STRING)
    private EventCategory category;

    @Column
    private LocalDateTime recruitmentStartAt;

    @Column
    private LocalDateTime recruitmentFinishAt;

    @Column
    private boolean isDeleted = false;

    @Column
    private boolean isCalculated = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Event(CreateEventRequestDto addEventRequestDto, User user) {
        this.title = addEventRequestDto.getTitle();
        this.content = addEventRequestDto.getContent();
        this.location = addEventRequestDto.getLocation();
        this.startAt = addEventRequestDto.getStartAt();
        this.endAt = addEventRequestDto.getEndAt();
        this.maxPeople = addEventRequestDto.getMaxPeople();
        this.price = addEventRequestDto.getPrice();
        this.category = addEventRequestDto.getCategory();
        this.recruitmentStartAt = addEventRequestDto.getRecruitmentStartAt();
        this.recruitmentFinishAt = addEventRequestDto.getRecruitmentFinishAt();
        this.user = user;
    }

    public static Event of(CreateEventRequestDto createEventRequestDto, User user) {
        return new Event(createEventRequestDto, user);
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

    public boolean isParticipantLimitExceed(int participantCount) {
        return maxPeople < participantCount;
    }

    public boolean isNotRecruitmentPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return !(now.isAfter(recruitmentStartAt) && now.isBefore(recruitmentFinishAt));
    }

    public boolean isFinishedRecruitment(LocalDateTime now) {
        return now.isAfter(recruitmentFinishAt);
    }

    public void calculate() {
        this.isCalculated = true;
    }
}
