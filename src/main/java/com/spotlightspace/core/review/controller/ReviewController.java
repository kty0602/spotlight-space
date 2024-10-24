package com.spotlightspace.core.review.controller;

import com.spotlightspace.common.dto.ResponseDto;
import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.review.dto.ReviewRequestDto;
import com.spotlightspace.core.review.dto.ReviewResponseDto;
import com.spotlightspace.core.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {


    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/review")
    public ResponseEntity<Review> createReview(
            @RequestBody @Valid ReviewRequestDto reviewRequestDto) {

        Review createReview = reviewService.createReview(reviewRequestDto);
        return new ResponseEntity<>(createReview, HttpStatus.CREATED);
    }

    @GetMapping("/reviews")
    public ResponseEntity<ResponseDto<List<ReviewResponseDto>>> getEventReviews(
            @RequestParam("eventId") Long eventId,
            @RequestParam(defaultValue = "1") int minRating,
            @RequestParam(defaultValue = "5") int maxRating
    ) {
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK.value(),
                reviewService.getEventReviews(eventId, minRating, maxRating),
                "리뷰를 조회했습니다."));
    }



    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Review> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
