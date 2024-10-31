package com.spotlightspace.core.admin.controller;


import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.common.exception.ErrorCode;
import com.spotlightspace.core.admin.dto.responsedto.AdminReviewResponseDto;
import com.spotlightspace.core.admin.service.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.spotlightspace.common.exception.ErrorCode.NO_RESULTS_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/reviews")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    /**
     * 리뷰 목록 조회
     *
     * @param page      페이지 번호 (1부터 시작)
     * @param size      페이지 크기
     * @param keyword   검색 키워드
     * @param sortField 정렬 필드
     * @param sortOrder 정렬 순서 (asc, desc)
     * @return 리뷰 목록 페이지
     */
    @GetMapping("/search")
    public ResponseEntity<Page<AdminReviewResponseDto>> getAdminReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        Page<AdminReviewResponseDto> reviews = adminReviewService.getAdminReviews(page, size, keyword, sortField, sortOrder);
        if (reviews.isEmpty()) {
            throw new ApplicationException(NO_RESULTS_FOUND);
        }
        return ResponseEntity.ok(reviews);
    }
}
