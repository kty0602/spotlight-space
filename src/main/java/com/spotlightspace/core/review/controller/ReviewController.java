package com.spotlightspace.core.review.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.dto.ResponseDto;
import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.review.dto.ReviewRequestDto;
import com.spotlightspace.core.review.dto.ReviewResponseDto;
import com.spotlightspace.core.review.dto.UpdateReviewRequestDto;
import com.spotlightspace.core.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/event/{eventId}")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 생성로직
     * @param reviewRequestDto
     * @param eventId
     * @param authUser
     * @return
     */
    @PostMapping("/review")
    public ResponseEntity<ReviewResponseDto> createReview(
            @RequestBody @Valid ReviewRequestDto reviewRequestDto,
            @PathVariable("eventId") Long eventId,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        ReviewResponseDto reviewResponseDto = reviewService.createReview(reviewRequestDto, eventId, authUser);
        return new ResponseEntity<>(reviewResponseDto, HttpStatus.CREATED);
    }

//    @GetMapping("/reviews/{reviewId}")
//    public ResponseEntity<ResponseDto<List<ReviewResponseDto>>> getEventReviews(
//            @PathVariable("eventId") Long eventId,
//            @PathVariable("reviewId") Long reviewId,
//            @RequestParam(defaultValue = "1") int minRating,
//            @RequestParam(defaultValue = "5") int maxRating
//    ) {
//        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK.value(),
//                reviewService.getEventReviews(eventId, minRating, maxRating),
//                "리뷰를 조회했습니다."));
//    }

    /**
     * 리뷰 수정 로직
     * @param updateReviewRequestDto
     * @param reviewId
     * @param authUser
     * @return
     */
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @RequestBody UpdateReviewRequestDto updateReviewRequestDto,
            @PathVariable("reviewId") Long reviewId,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        ReviewResponseDto reviewResponseDto = reviewService.updateReview(reviewId, updateReviewRequestDto, authUser);
        return new ResponseEntity<>(reviewResponseDto, HttpStatus.OK);
    }


    /**
     * 리뷰 삭제
     * @param reviewId
     * @param authUser
     * @return
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable("reviewId") Long reviewId,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        reviewService.deleteReview(reviewId, authUser);
        return new ResponseEntity<>("성공적으로 삭제되었습니다.", HttpStatus.OK);
    }
}
