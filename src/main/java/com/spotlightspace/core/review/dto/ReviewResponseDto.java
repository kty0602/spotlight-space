package com.spotlightspace.core.review.dto;

import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.user.domain.User;
import lombok.Getter;

@Getter
public class ReviewResponseDto {

    private Long id;
    private User nickname;
    private String contents;
    private Integer rating;

    public ReviewResponseDto(Review review) {
        this.id = review.getId();
        this.nickname = review.getNickname();
        this.rating = review.getRating();
        this.contents = review.getContents();

    }
}
