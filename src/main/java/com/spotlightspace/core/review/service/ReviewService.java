package com.spotlightspace.core.review.service;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.common.exception.ErrorCode;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.review.dto.ReviewRequestDto;
import com.spotlightspace.core.review.dto.ReviewResponseDto;
import com.spotlightspace.core.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    //리뷰 생성
    public Review createReview(ReviewRequestDto reviewRequestDto) {
        // 티켓을 가진 유저만 생성 할 수 있다.
        //true부분 티켓 서비스 작성 후 작성
        if(true){
            throw new ApplicationException(ErrorCode.UNAUTHORIZED);
        }
//        이벤트 서비스 구현 되면 주석 풀기
//        Event events = eventService.findEvents(eventId);

//        Review newReview = new Review(
//                events.getEvent(),
//                reviewRequestDto.getNickname(),
//                reviewRequestDto.getRating(),
//                reviewRequestDto.getContents());
//        return reviewRepository.save(newReview);
        return null;
    }


    //리뷰 조회
    public List<ReviewResponseDto> getEventReviews(Long eventId, int minRating, int maxRating) {
        List<Review> reviews = reviewRepository.findEventReviewsWithStarByCreatedAtDesc(
                eventId, minRating, maxRating);
        log.info("reviews : {}", reviews);

        //최신순
        return reviews.stream()
                .map(ReviewResponseDto::new)
                .collect(Collectors.toList());
    }

    //리뷰 수정
    public ReviewResponseDto updateReview(Long id, Review updatedReview) {
        // id로 기존 리뷰를 찾음
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with id: " + id));

        // 기존 리뷰 데이터를 수정
        review.setContents(updatedReview.getContents());
        review.setRating(updatedReview.getRating());

        // 수정된 리뷰를 저장
        Review savedReview = reviewRepository.save(review);

        // 수정된 리뷰 데이터를 반환 (DTO로 변환하여)
        return new ReviewResponseDto(savedReview);
    }

    //리뷰 삭제
    public void deleteReview(Long id) {
        // id를 기반으로 리뷰 조회
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.REVIEW_NOT_FOUND));

        // 리뷰 삭제
        reviewRepository.delete(review);
    }
}
