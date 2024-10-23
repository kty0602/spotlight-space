package com.spotlightspace.core.review.repository;

import com.spotlightspace.core.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewQueryRepository {


}
