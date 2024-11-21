package com.spotlightspace.core.data;

import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.domain.EventCategory;
import com.spotlightspace.core.event.dto.request.UpdateEventRequestDto;
import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.review.dto.ReviewRequestDto;
import com.spotlightspace.core.review.dto.UpdateReviewRequestDto;
import com.spotlightspace.core.user.domain.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static com.spotlightspace.core.data.EventTestData.testEvent;
import static com.spotlightspace.core.data.UserTestData.testUser;

public class ReviewTestData {

    public static ReviewRequestDto createDefaultReviewRequestDto() {
        return ReviewRequestDto.of(
                3,
                "재미있습니다 ㅇㅇㅇㅇㅇㅇ",
                1L
        );
    }

    public static Review testReview() {
        ReviewRequestDto reviewRequestDto = createDefaultReviewRequestDto();
        User user = testUser();
        ReflectionTestUtils.setField(user, "id", 1L);
        Event event = testEvent();
        ReflectionTestUtils.setField(user, "id", 1L);
        Review review = Review.of(reviewRequestDto, event, user);
        ReflectionTestUtils.setField(event, "id", 1L);
        return review;
    }

    public static Review testReview2() {
        ReviewRequestDto reviewRequestDto = createDefaultReviewRequestDto();
        User user = testUser();
        ReflectionTestUtils.setField(user, "id", 1L);
        Event event = testEvent();
        ReflectionTestUtils.setField(event, "id", 2L);
        Review review = Review.of(reviewRequestDto, event, user);
        ReflectionTestUtils.setField(review, "id", 1L);
        return review;
    }

    public static UpdateReviewRequestDto updateDefaultReviewRequestDto() {
        return UpdateReviewRequestDto.of(
                1,
                "수정된 리뷰",
                "www.reviewEdit.com"
        );
    }
}
