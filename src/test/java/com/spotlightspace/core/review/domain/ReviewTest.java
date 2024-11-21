package com.spotlightspace.core.review.domain;

import static com.spotlightspace.core.event.domain.EventCategory.ART;
import static org.assertj.core.api.Assertions.assertThat;

import com.spotlightspace.core.data.UserTestData;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.review.dto.ReviewRequestDto;
import com.spotlightspace.core.user.domain.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReviewTest {

    @Test
    @DisplayName("리뷰 별점을 변경할 수 있다.")
    void changeRating() {
        // given
        User user = UserTestData.testUser();
        Event event = Event.create(getCreateEventRequestDto(), user);

        int initialRating = 3;
        ReviewRequestDto requestDto = ReviewRequestDto.of(initialRating, "contents", 1L);
        Review review = Review.of(requestDto, event, user);

        // when
        review.changeRating(5);

        // then
        assertThat(review.getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("리뷰 내용을 변경할 수 있다.")
    void changeContents() {
        // given
        User user = UserTestData.testUser();
        Event event = Event.create(getCreateEventRequestDto(), user);

        String initialContents = "contents";
        ReviewRequestDto requestDto = ReviewRequestDto.of(3, initialContents, 1L);
        Review review = Review.of(requestDto, event, user);

        // when
        review.changeContents("new contents");

        // then
        assertThat(review.getContents()).isEqualTo("new contents");
    }

    @Test
    @DisplayName("리뷰를 삭제 상태로 변경할 수 있다.")
    void changeIsDeleted() {
        // given
        User user = UserTestData.testUser();
        Event event = Event.create(getCreateEventRequestDto(), user);
        ReviewRequestDto requestDto = ReviewRequestDto.of(3, "contents", 1L);
        Review review = Review.of(requestDto, event, user);

        // when
        review.changeIsDeleted();

        // then
        assertThat(review.isDeleted()).isTrue();
    }


    CreateEventRequestDto getCreateEventRequestDto() {
        return CreateEventRequestDto.of("title", "content", "location",
                LocalDateTime.of(2024, 10, 10, 10, 0),
                LocalDateTime.of(2024, 10, 10, 12, 0),
                109, 10_000, ART,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1));
    }
}
