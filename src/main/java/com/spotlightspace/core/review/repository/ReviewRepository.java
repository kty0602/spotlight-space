package com.spotlightspace.core.review.repository;

import com.spotlightspace.core.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewQueryRepository {

    List<Review> findEventReviewsWithStarByCreatedAtDesc(Long storeId, int minRating, int maxRating);

}
