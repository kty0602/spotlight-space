package com.spotlightspace.core.likes.service;

import static com.spotlightspace.core.event.domain.EventCategory.ART;
import static org.assertj.core.api.Assertions.assertThat;

import com.spotlightspace.core.data.UserTestData;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.likes.domain.Likes;
import com.spotlightspace.core.likes.repository.LikesRepository;
import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.review.dto.ReviewRequestDto;
import com.spotlightspace.core.review.repository.ReviewRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LikeServiceTest {

    @Autowired
    LikeService likeService;

    @Autowired
    LikesRepository likesRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    @AfterEach
    void tearDown() {
        likesRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("좋아요를 등록할 수 있다.")
    void likeReview() {
        // given
        User user = userRepository.save(UserTestData.testUser());
        Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
        ReviewRequestDto requestDto = ReviewRequestDto.of(3, "contents", 1L);
        Review review = reviewRepository.save(Review.of(requestDto, event, user));

        // when
        likeService.likeReview(user.getId(), review.getId());

        // then
        List<Likes> likes = likesRepository.findAll();
        assertThat(likes).hasSize(1);
    }

    @Test
    @DisplayName("좋아요를 취소할 수 있다.")
    void cancelLike() {
        // given
        User user = userRepository.save(UserTestData.testUser());
        Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
        ReviewRequestDto requestDto = ReviewRequestDto.of(3, "contents", 1L);
        Review review = reviewRepository.save(Review.of(requestDto, event, user));
        likeService.likeReview(user.getId(), review.getId());

        // when
        likeService.cancelLike(user.getId(), review.getId());

        // then
        long likeCount = likesRepository.findLikeUsersByReviewId(review.getId()).stream().count();
        assertThat(likeCount).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 누른 유저를 조회할 수 있다.")
    void getLikeUsersByReviewId() {
        // given
        User user = userRepository.save(UserTestData.testUser());
        Event event = eventRepository.save(Event.of(getCreateEventRequestDto(), user));
        ReviewRequestDto requestDto = ReviewRequestDto.of(3, "contents", 1L);
        Review review = reviewRepository.save(Review.of(requestDto, event, user));
        likeService.likeReview(user.getId(), review.getId());

        // when
        List<User> users = likeService.getLikeUsersByReviewId(review.getId());

        // then
        assertThat(users).hasSize(1);
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
