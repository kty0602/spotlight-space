package com.spotlightspace.core.review.dto;

import com.spotlightspace.core.review.domain.Review;
import lombok.Getter;

@Getter
public class UpdateReviewResponseDto {
    private Long id;
    private Integer rating;
    private String contents;

    private UpdateReviewResponseDto(Review review) {
        this.id = review.getId();
        this.rating = review.getRating();
        this.contents = review.getContents();
    }

    public static UpdateReviewResponseDto of(Review review) {
        return new UpdateReviewResponseDto(review);
    }
}
