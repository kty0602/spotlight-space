package com.spotlightspace.core.admin.controller;

import com.spotlightspace.common.exception.ErrorCode;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import com.spotlightspace.core.admin.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Autowired
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     *
     * @param page       조회할 페이지 번호 (1부터 시작)
     * @param size       페이지 당 항목 수
     * @param keyword    검색 키워드 (선택적)
     * @param sortField  결과를 정렬할 필드명
     * @param sortOrder  정렬 순서 (오름차순 또는 내림차순)
     * @return {@link AdminUserResponseDto}의 페이지 형태로 검색 결과 반환
     */
    @GetMapping("/search")
    public ResponseEntity<Page<AdminUserResponseDto>> searchUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        Page<AdminUserResponseDto> users = adminUserService.getAdminUsers(page, size, keyword, sortField, sortOrder);
        if (users.isEmpty()) {
            throw new ApplicationException(ErrorCode.NO_RESULTS_FOUND);
        }
        return ResponseEntity.ok(users);
    }
}