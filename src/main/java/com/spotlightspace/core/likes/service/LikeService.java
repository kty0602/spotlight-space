package com.spotlightspace.core.likes.service;

import com.spotlightspace.core.likes.domain.Like;
import com.spotlightspace.core.likes.likesRequestDto.LikesResponseDto;
import com.spotlightspace.core.likes.repository.LikesRepository;
import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.review.repository.ReviewRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService {

    private final LikesRepository likesRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public LikesResponseDto likeReview(Long userId, Long reviewId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Review review = reviewRepository.findByIdOrElseThrow(reviewId);

        review.likeReview(user);

        Like like = Like.of(user, review);

        likesRepository.save(like);

        return LikesResponseDto.of(review.getLikeCount(), true);
    }

    public LikesResponseDto cancelLike(Long userId, Long reviewId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Review review = reviewRepository.findByIdOrElseThrow(reviewId);

        review.dislikeReview(user);

        Like dislike = Like.of(user, review);
        likesRepository.save(dislike);

        return LikesResponseDto.of(review.getLikeCount(), false);
    }

    @Transactional
    public List<User> getLikeUsersByReviewId(Long reviewId) {
        return likesRepository.findLikeUsersByReviewId(reviewId);
    }

}
