package com.spotlightspace.core.admin.controller;


import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.admin.dto.responsedto.AdminEventResponseDto;
import com.spotlightspace.core.admin.service.AdminEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.spotlightspace.common.exception.ErrorCode.NO_RESULTS_FOUND;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/events")
public class AdminEventController {

    private final AdminEventService adminEventService;


    /**
     * 이벤트 목록 조회 API
     *
     * @param page      페이지 번호 (기본값: 1)
     * @param size      페이지 크기 (기본값: 10)
     * @param keyword   검색어 (선택적)
     * @param sortField 정렬 필드 (기본값: id)
     * @param sortOrder 정렬 순서 (기본값: asc)
     * @return 이벤트 목록 페이지
     */

    @GetMapping("/search")
    public ResponseEntity<Page<AdminEventResponseDto>> getAdminEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        Page<AdminEventResponseDto> events = adminEventService.getAdminEvents(page, size, keyword, sortField, sortOrder);
        if (events.isEmpty()) {
            throw new ApplicationException(NO_RESULTS_FOUND);
        }
        return ResponseEntity.ok(events);
    }

    /**
     *
     * @param id 삭제할 이벤트의 Id
     * @return 성공 시 204 No Content 응답
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        adminEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

}
