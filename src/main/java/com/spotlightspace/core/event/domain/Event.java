package com.spotlightspace.core.event.domain;

import com.spotlightspace.common.entity.Timestamped;
import com.spotlightspace.core.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

    @Column(length = 20)
    private String category;

    @Column
    private LocalDate recruitmentStartAt;

    @Column
    private LocalDate recruitmentFinishAt;

    @Column
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeContent(String content) {
        this.content = content;
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

    public void changeCategory(String category) {
        this.category = category;
    }

    public void changeRecruitmentStartAt(LocalDate recruitmentStartAt) {
        this.recruitmentStartAt = recruitmentStartAt;
    }

    public void changeRecruitmentFinishAt(LocalDate recruitmentFinishAt) {
        this.recruitmentFinishAt = recruitmentFinishAt;
    }

    public void deleteEvent() {
        this.isDeleted = true;
    }

}
