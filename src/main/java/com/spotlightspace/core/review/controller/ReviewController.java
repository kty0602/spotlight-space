package com.spotlightspace.core.review.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.review.dto.ReviewRequestDto;
import com.spotlightspace.core.review.dto.ReviewResponseDto;
import com.spotlightspace.core.review.dto.UpdateReviewRequestDto;
import com.spotlightspace.core.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/event/{eventId}")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 생성로직
     *
     * @param reviewRequestDto
     * @param eventId
     * @param authUser
     * @return
     */
    @PostMapping("/review")
    public ResponseEntity<ReviewResponseDto> createReview(
            @RequestPart @Valid ReviewRequestDto reviewRequestDto,
            @PathVariable("eventId") Long eventId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart(required = false) MultipartFile file
    ) throws IOException {

        ReviewResponseDto reviewResponseDto = reviewService.createReview(reviewRequestDto, eventId, authUser, file);
        return new ResponseEntity<>(reviewResponseDto, HttpStatus.CREATED);
    }

    //리뷰 조회
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviews(
            @PathVariable("eventId") Long eventId
    ) {
        List<ReviewResponseDto> reviews = reviewService.getReviews(eventId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    /**
     * 리뷰 수정 로직
     *
     * @param updateReviewRequestDto
     * @param reviewId
     * @param authUser
     * @return
     */
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @RequestPart UpdateReviewRequestDto updateReviewRequestDto,
            @PathVariable("reviewId") Long reviewId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart MultipartFile file
    ) throws IOException {
        ReviewResponseDto reviewResponseDto = reviewService.updateReview(reviewId, updateReviewRequestDto, authUser, file);
        return new ResponseEntity<>(reviewResponseDto, HttpStatus.OK);
    }

    /**
     * 리뷰 삭제
     *
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
