package com.spotlightspace.core.review.dto;

import com.spotlightspace.core.event.domain.EventCategory;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.Range;

import java.sql.Time;
import java.time.LocalDateTime;

@Getter
public class ReviewRequestDto {

    @NotNull(message = "별점을 입력해주세요")
    @Range(min = 1, max = 5)
    private Integer rating;     // 별점 (1~5점)

    @Size(min = 10 , max = 200) //최소 10글자 최대 200글자
    @NotNull(message = "리뷰 내용을 입력해주세요(최소 10글자 최대 200글자)")
    private String contents; // 리뷰 내용

    private Long ticketId;

    private ReviewRequestDto(Integer rating, String contents, Long ticketId) {
        this.rating = rating;
        this.contents = contents;
        this.ticketId = ticketId;
    }
    public static ReviewRequestDto of(Integer rating, String contents, Long ticketId) {
        return new ReviewRequestDto(rating, contents, ticketId);
    }}