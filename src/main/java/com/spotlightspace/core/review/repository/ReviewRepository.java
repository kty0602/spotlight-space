package com.spotlightspace.core.review.repository;

import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.review.dto.ReviewRequestDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewQueryRepository {

    List<Review> findEventReviewsAndIsDeletedFalse(Long eventId);

    Optional<Review> findByIdAndIsDeletedFalse (Long reviewId);
}
