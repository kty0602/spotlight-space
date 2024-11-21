package com.spotlightspace.core.likes.domain;

import static org.assertj.core.api.Assertions.*;

import com.spotlightspace.core.data.EventTestData;
import com.spotlightspace.core.data.UserTestData;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.review.dto.ReviewRequestDto;
import com.spotlightspace.core.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LikesTest {

    @Test
    @DisplayName("좋아요 생성 시 생성시간 및 수정시간은 null이다.")
    void createLikes() {
        // given
        User user = UserTestData.testUser();
        Event event = EventTestData.testEvent();
        ReviewRequestDto requestDto = ReviewRequestDto.of(3, "contents", 1L);
        Review review = Review.of(requestDto, event, user);

        // when
        Likes likes = Likes.of(user, review);

        // then
        assertThat(likes.getCreatedAt()).isNull();
        assertThat(likes.getUpdatedAt()).isNull();
    }

}
