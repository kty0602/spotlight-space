package com.spotlightspace.core.review.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.likes.likesRequestDto.LikesResponseDto;
import com.spotlightspace.core.likes.service.LikeService;
import com.spotlightspace.core.review.dto.GetReviewResponseDto;
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
    private final LikeService likeService;


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
    public ResponseEntity<List<GetReviewResponseDto>> getReviews(
            @PathVariable("eventId") Long eventId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        List<GetReviewResponseDto> reviews = reviewService.getReviews(eventId, authUser);
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
            @RequestPart(required = false) MultipartFile file
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

    /**
     *
     * @param reviewId
     * @param authUser
     * @return
     */
    //좋아요
    @PostMapping("/reviews/likes/{reviewId}")
    public ResponseEntity<LikesResponseDto> likeReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        LikesResponseDto addLike = likeService.likeReview(authUser.getUserId(), reviewId);
        return new ResponseEntity<>(addLike, HttpStatus.OK);
    }

    /**
     *
     * @param reviewId
     * @param authUser
     * @return
     */
    //좋아요 취소
    @PatchMapping("/reviews/likes/{reviewId}")
    public ResponseEntity<LikesResponseDto> cancelLike(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        LikesResponseDto message = likeService.cancelLike(authUser.getUserId(), reviewId);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

}
