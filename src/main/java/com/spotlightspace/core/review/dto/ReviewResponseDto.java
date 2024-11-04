package com.spotlightspace.core.review.dto;

import com.spotlightspace.core.review.domain.Review;
import lombok.Getter;

@Getter
public class ReviewResponseDto {

    private Long id;
    private String nickname;
    private String contents;
    private Integer rating;
    private String attachment;

    private ReviewResponseDto(Review review, String attachment) {
        this.id = review.getId();
        this.nickname = review.getUser().getNickname();
        this.rating = review.getRating();
        this.contents = review.getContents();
        this.attachment = attachment;
    }
    public static ReviewResponseDto from(Review review, String attachment) {
        return new ReviewResponseDto(review, attachment);
    }}