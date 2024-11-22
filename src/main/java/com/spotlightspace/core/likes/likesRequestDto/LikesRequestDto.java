package com.spotlightspace.core.likes.likesRequestDto;


import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.user.domain.User;
import lombok.Getter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Getter
public class LikesRequestDto {

    private Review review;

    private User user;

    private LikesRequestDto(Review review, User user) {
        this.review = review;
        this.user = user;
    }

    public static LikesRequestDto of(Review review, User user) {
        return new LikesRequestDto(review, user);
    }
}
