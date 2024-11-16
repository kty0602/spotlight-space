package com.spotlightspace.core.likes.likesRequestDto;


import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.user.domain.User;
import lombok.Getter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Getter
public class LikesRequestDto {

    private Review reviewId;

    private User userId;

    private LikesRequestDto(Review reviewId, User userId) {
        this.reviewId = reviewId;
        this.userId = userId;
    }

    public static LikesRequestDto of(Review reviewId, User userId) {
        return new LikesRequestDto(reviewId, userId);
    }
}
