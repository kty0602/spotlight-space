package com.spotlightspace.core.review.dto;

import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.user.domain.User;
import lombok.Getter;

@Getter
public class ReviewResponseDto {

    private Long id;
    private String nickname;
    private String contents;
    private Integer rating;

    private ReviewResponseDto(Review review) {
        this.id = review.getId();
        this.nickname = review.getUser().getNickname();
        this.rating = review.getRating();
        this.contents = review.getContents();
    }


    public static ReviewResponseDto from(Review review) {
        return new ReviewResponseDto(review);
    }
}
