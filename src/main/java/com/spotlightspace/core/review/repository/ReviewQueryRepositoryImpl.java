package com.spotlightspace.core.review.repository;

import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.review.domain.Review;

import java.util.List;

public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {

    @Override
    public List<Review> findReviewWithEventId(Event eventId) {
        return List.of();
    }
}
