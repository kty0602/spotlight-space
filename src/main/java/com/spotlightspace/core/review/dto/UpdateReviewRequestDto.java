package com.spotlightspace.core.review.dto;

import lombok.Getter;

@Getter
public class UpdateReviewRequestDto {

    private Integer rating;

    private String contents;

    private String attachment;
}
