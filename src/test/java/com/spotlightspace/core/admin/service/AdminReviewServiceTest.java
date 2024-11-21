package com.spotlightspace.core.admin.service;


import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.admin.dto.responsedto.AdminReviewResponseDto;
import com.spotlightspace.core.admin.repository.AdminQueryRepository;
import com.spotlightspace.core.review.domain.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.Optional;

import static com.spotlightspace.common.exception.ErrorCode.REVIEW_NOT_FOUND;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminReviewServiceTest {

    @Mock
    private AdminQueryRepository adminRepository;

    @InjectMocks
    private AdminReviewService adminReviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAdminReviews_withKeyword() {
        // given
        String keyword = "test";
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("contents").ascending());
        AdminReviewResponseDto reviewDto = AdminReviewResponseDto.of(
                1L, "Test Review", "User1", "testcontents", 5, false
        );
        Page<AdminReviewResponseDto> expectedPage = new PageImpl<>(Collections.singletonList(reviewDto));

        // when
        when(adminRepository.getAdminReviews(anyString(), any(PageRequest.class))).thenReturn(expectedPage);
        Page<AdminReviewResponseDto> result = adminReviewService.getAdminReviews(1, 10, keyword, "contents", "asc");


        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContents()).isEqualTo("testcontents");
    }

    @Test
    void testGetAdminReviews_withoutKeyword() {
        // given
        String keyword = null;
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("contents").ascending());
        Page<AdminReviewResponseDto> expectedPage = new PageImpl<>(Collections.emptyList());

        // when
        when(adminRepository.getAdminReviews(isNull(), any(PageRequest.class))).thenReturn(expectedPage);
        Page<AdminReviewResponseDto> result = adminReviewService.getAdminReviews(1, 10, keyword, "contents", "asc");


        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }



    @Test
    void testDeleteReview_Success() {
        // given
        Review review = mock(Review.class);
        when(adminRepository.findReviewById(anyLong())).thenReturn(Optional.of(review));

        // when
        adminReviewService.deleteReview(1L);

        // then
        verify(review, times(1)).changeIsDeleted();
    }

    @Test
    void testDeleteReview_ReviewNotFound() {
        // given
        when(adminRepository.findReviewById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminReviewService.deleteReview(1L))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(REVIEW_NOT_FOUND.getMessage());
    }

}
