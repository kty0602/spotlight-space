package com.spotlightspace.core.review.service;

import static com.spotlightspace.common.exception.ErrorCode.FORBIDDEN_USER;
import static com.spotlightspace.common.exception.ErrorCode.REVIEW_NOT_FOUND;
import static com.spotlightspace.common.exception.ErrorCode.TICKET_NOT_FOUND;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.attachment.service.AttachmentService;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.review.dto.ReviewRequestDto;
import com.spotlightspace.core.review.dto.ReviewResponseDto;
import com.spotlightspace.core.review.dto.UpdateReviewRequestDto;
import com.spotlightspace.core.review.repository.ReviewRepository;
import com.spotlightspace.core.ticket.domain.Ticket;
import com.spotlightspace.core.ticket.repository.TicketRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final AttachmentService attachmentService;

    //리뷰 생성
    public ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto, Long eventId,
            AuthUser authUser, MultipartFile file) throws IOException {

        // 티켓을 가진 유저만 생성 할 수 있다.
        User user = checkUserExist(authUser.getUserId());

        //티켓아이디를 가지고 있나?
        Ticket ticket = ticketRepository.findByIdOrElseThrow(reviewRequestDto.getTicketId());

        if (!ticket.getEvent().getId().equals(eventId) || !ticket.getUser().getId().equals(authUser.getUserId())) {
            throw new ApplicationException(TICKET_NOT_FOUND);
        }

        // 리뷰 달려고 하는 이벤트가 존재하는가?
        Event event = eventRepository.findByIdOrElseThrow(eventId);

        Review review = reviewRepository.save(Review.of(reviewRequestDto, event, user));

        attachmentService.addAttachment(file, review.getId(), TableRole.REVIEW);

        String attachment = attachmentService.getImageUrl(review.getId(), TableRole.REVIEW);

        return ReviewResponseDto.of(review, attachment);
    }

    //리뷰 조회
    public List<ReviewResponseDto> getReviews(Long eventId) {

        List<Review> reviews = reviewRepository.findByEventIdAndIsDeletedFalse(eventId);

        //stream리뷰 조회 시
        return reviews.stream()
                .map(review -> {
                    String attachment = attachmentService.getImageUrl(review.getId(), TableRole.REVIEW);
                    return ReviewResponseDto.of(review, attachment);
                }).collect(Collectors.toList());
    }

    //리뷰 수정
    public ReviewResponseDto updateReview(Long reviewId, UpdateReviewRequestDto updateReviewRequestDto,
            AuthUser authUser, MultipartFile file) throws IOException {
        // id로 기존 리뷰를 찾음
        Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new ApplicationException(REVIEW_NOT_FOUND));

        // 권한 체크 코드
        checkUserOnReview(review, authUser);

        // 기존 리뷰 데이터를 수정
        if (updateReviewRequestDto.getRating() != null) {
            review.changeRating(updateReviewRequestDto.getRating());
        }
        if (updateReviewRequestDto.getContents() != null) {
            review.changeContents(updateReviewRequestDto.getContents());
        }
        if (file != null) {
            Long attachmentId = attachmentService.getAttachmentList(review.getId(), TableRole.REVIEW).get(0).getId();
            attachmentService.updateAttachment(attachmentId, file, reviewId, TableRole.REVIEW, authUser);
        }
        // 수정된 리뷰를 저장
        Review savedReview = reviewRepository.save(review);
        String saveAttachment = attachmentService.getImageUrl(reviewId, TableRole.REVIEW);

        // 수정된 리뷰 데이터를 반환 (DTO로 변환하여)
        return ReviewResponseDto.of(savedReview, saveAttachment);
    }

    //리뷰 삭제
    public void deleteReview(Long reviewId, AuthUser authUser) {
        // id를 기반으로 리뷰 조회
        Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new ApplicationException(REVIEW_NOT_FOUND));
        // 권한 체크 코드
        checkUserOnReview(review, authUser);
        // 리뷰 삭제
        review.changeIsDeleted();
    }

    // 리뷰 작성자인가?
    private void checkUserOnReview(Review review, AuthUser authUser) {
        if (!review.getUser().getId().equals(authUser.getUserId())) {
            throw new ApplicationException(FORBIDDEN_USER);
        }
    }

    // 유저 존재 확인
    private User checkUserExist(Long id) {
        return userRepository.findByIdOrElseThrow(id);
    }

    public void deleteUserReview(Long userId) {
        reviewRepository.deleteByUserId(userId);
    }
}
