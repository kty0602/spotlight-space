package com.spotlightspace.core.admin.controller;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import com.spotlightspace.core.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.spotlightspace.common.exception.ErrorCode.NO_RESULTS_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;


    /**
     * @param page      조회할 페이지 번호 (1부터 시작)
     * @param size      페이지 당 항목 수
     * @param keyword   검색 키워드 (선택적)
     * @param sortField 결과를 정렬할 필드명
     * @param sortOrder 정렬 순서 (오름차순 또는 내림차순)
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
            throw new ApplicationException(NO_RESULTS_FOUND);
        }
        return ResponseEntity.ok(users);
    }

    /**
     * @param id 삭제할 사용자의 ID
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * @param id   역할을 변경할 사용자의 ID
     * @param role 새로운 역할 (ADMIN으로 변경은 불가)
     * @return 역할 변경 완료 응답
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        adminUserService.updateUserRole(id, role);
        return ResponseEntity.noContent().build();
    }

}
