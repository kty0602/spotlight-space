package com.spotlightspace.core.review.repository;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

import static com.spotlightspace.common.exception.ErrorCode.REVIEW_NOT_FOUND;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewQueryRepository {

    List<Review> findByEventIdAndIsDeletedFalse(Long eventId);

    Optional<Review> findByIdAndIsDeletedFalse(Long reviewId);

    Optional<Review> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);

    default Review findByIdOrElseThrow(long id) {
        return findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ApplicationException(REVIEW_NOT_FOUND));
    }

    default Review findByIdAndUserIdOrElseThrow(Long id, Long userId) {
        return findByIdAndUserIdAndIsDeletedFalse(id, userId)
                .orElseThrow(() -> new ApplicationException(REVIEW_NOT_FOUND));
    }

    @Modifying
    @Query("UPDATE Review r SET r.isDeleted = true WHERE r.user.id = :userId")
    void deleteByUserId(Long userId);
}
