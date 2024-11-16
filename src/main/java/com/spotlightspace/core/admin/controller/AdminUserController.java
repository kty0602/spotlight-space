package com.spotlightspace.core.admin.controller;

import static com.spotlightspace.common.exception.ErrorCode.NO_RESULTS_FOUND;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.admin.dto.requestdto.SearchAdminUserRequestDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import com.spotlightspace.core.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 유저를 검색합니다
     *
     * @param page          조회할 페이지 번호 (1부터 시작)
     * @param size          페이지 당 항목 수
     * @param nickname      검색 키워드 : 닉네임 (선택적)
     * @param email         검색 키워드 : 이메일 (선택적)
     * @param phoneNumber   검색 키워드 : 전화번호 (선택적)
     * @param role          검색 키워드 : 권한 아티스트 or 유저 (선택적)
     * @param location      검색 키워드 : 지역 (선택적)
     * @param isSocialLogin 검색 키워드 : 소셜 로그인 여부 (선택적)
     * @param isDeleted     검색 키워드 : 삭제 여부 (선택적)
     * @param sortField     결과를 정렬할 필드명
     * @param sortOrder     정렬 순서 (오름차순 또는 내림차순)
     * @return 페이지 형태로 검색 결과 반환
     */
    @GetMapping("/search")
    public ResponseEntity<Page<AdminUserResponseDto>> searchUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String birth,
            @RequestParam(defaultValue = "false") Boolean isSocialLogin,
            @RequestParam(defaultValue = "false") Boolean isDeleted,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        SearchAdminUserRequestDto searchAdminUserRequestDto = SearchAdminUserRequestDto.of(nickname, email, phoneNumber,
                role, location, birth, isSocialLogin, isDeleted);
        Page<AdminUserResponseDto> users = adminUserService.getAdminUsers(page, size, searchAdminUserRequestDto,
                sortField, sortOrder);
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
