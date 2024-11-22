package com.spotlightspace.core.review.repository;

import static com.spotlightspace.common.exception.ErrorCode.REVIEW_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spotlightspace.common.exception.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReviewRepositoryTest {

    @Autowired
    ReviewRepository reviewRepository;

    @Test
    @DisplayName("리뷰 ID와 사용자 ID로 리뷰 조회 시 존재하지 않으면 예외가 발생한다.")
    void findByIdAndUserIdOrElseThrow() {
        // when & then
        assertThatThrownBy(() -> reviewRepository.findByIdAndUserIdOrElseThrow(1L, 1L))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(REVIEW_NOT_FOUND.getMessage());
    }
    
}
