package com.spotlightspace.core.likes.service;

import com.spotlightspace.core.likes.domain.Likes;
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
@RequiredArgsConstructor
public class LikeService {

    private final LikesRepository likesRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;


    @Transactional
    public LikesResponseDto likeReview(Long userId, Long reviewId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Review review = reviewRepository.findByIdOrElseThrow(reviewId);

        review.likeReview(user);

        Likes like = Likes.of(user, review);

        likesRepository.save(like);

        return LikesResponseDto.of(review.getLikeCount(), true);
    }

    @Transactional
    public LikesResponseDto cancelLike(Long userId, Long reviewId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Review review = reviewRepository.findByIdOrElseThrow(reviewId);

        review.dislikeReview(user);

        Likes dislike = Likes.of(user, review);
        likesRepository.save(dislike);

        return LikesResponseDto.of(review.getLikeCount(), false);
    }


    @Transactional
    public List<User> getLikeUsersByReviewId(Long reviewId) {
        return likesRepository.findLikeUsersByReviewId(reviewId);
    }

}
