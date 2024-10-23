package com.spotlightspace.core.review.dto;

import com.spotlightspace.core.review.domain.Review;
import lombok.Getter;

@Getter
public class ReviewResponseDto {

    private Long id;
    private String contents;
    private int star;

    public ReviewResponseDto(Review review) {
        this.id = review.getId();
        this.contents = review.getContents();
        this.star = review.getStar();
    }
}
