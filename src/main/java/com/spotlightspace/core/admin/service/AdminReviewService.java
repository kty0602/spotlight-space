package com.spotlightspace.core.admin.service;




import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.admin.dto.responsedto.AdminReviewResponseDto;
import com.spotlightspace.core.admin.repository.AdminQueryRepository;
import com.spotlightspace.core.review.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.spotlightspace.common.exception.ErrorCode.REVIEW_NOT_FOUND;

@Service
@RequiredArgsConstructor

public class AdminReviewService {

    private final AdminQueryRepository adminRepository;

    @Transactional(readOnly = true)
    public Page<AdminReviewResponseDto> getAdminReviews(int page, int size, String keyword, String sortField, String sortOrder) {
        Sort sort = Sort.by(sortField);
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);

        // AdminRepository의 QueryDSL 메서드를 통해 검색 수행 (검색어 추가)
        return adminRepository.getAdminReviews(keyword, pageable);
    }

    @Transactional
    public void deleteReview(Long id) {
        Review review = adminRepository.findReviewById(id)
                .orElseThrow(() -> new ApplicationException(REVIEW_NOT_FOUND));
        review.changeIsDeleted();
    }

}
