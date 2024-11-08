package com.spotlightspace.core.admin.dto.responsedto;


import com.spotlightspace.core.event.domain.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminEventResponseDto {
    private Long id;
    private String title;
    private String content;
    private String location;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private int maxPeople;
    private int price;
    private String category;
    private LocalDateTime recruitmentStartAt;
    private LocalDateTime recruitmentFinishAt;
    private boolean isDeleted;

    // Event 엔티티에서 데이터를 가져오는 정적 팩토리 메서드
    public static AdminEventResponseDto from(Event event) {
        return new AdminEventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getContent(),
                event.getLocation(),
                event.getStartAt(),
                event.getEndAt(),
                event.getMaxPeople(),
                event.getPrice(),
                event.getCategory().name(),
                event.getRecruitmentStartAt(),
                event.getRecruitmentFinishAt(),
                event.isDeleted()
        );
    }
    //of 메서드는 DTO 객체를 생성하는 정적 팩토리 메서드로 외부에서 이 메서드를 호출해 DTO를 생성할 수 있으며, 생성자와 달리 이름을 부여할 수 있다.
    public static AdminEventResponseDto of(Long id, String title, String content, String location, LocalDateTime startAt,
                                             LocalDateTime endAt, int maxPeople, int price, String category,
                                             LocalDateTime recruitmentStartAt, LocalDateTime recruitmentFinishAt,
                                             Boolean isDeleted) {
        return new AdminEventResponseDto(id, title, content, location, startAt, endAt, maxPeople, price, category,
                recruitmentStartAt, recruitmentFinishAt, isDeleted);
    }
}
