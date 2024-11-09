package com.spotlightspace.core.admin.service;

import static com.spotlightspace.common.exception.ErrorCode.USER_NOT_FOUND;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.admin.dto.requestdto.SearchAdminUserRequestDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import com.spotlightspace.core.admin.repository.AdminQueryRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor

public class AdminUserService {

    private final AdminQueryRepository adminRepository;

    @Transactional(readOnly = true)
    public Page<AdminUserResponseDto> getAdminUsers(
            int page,
            int size,
            SearchAdminUserRequestDto searchAdminUserRequestDto,
            String field,
            String order
    ) {

        Sort sort = Sort.by(field);

        if ("desc".equalsIgnoreCase(order)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        return adminRepository.getAdminUsers(searchAdminUserRequestDto, pageable);
    }

    @Transactional
    public void updateUserRole(Long userId, String newRole) {
        User user = adminRepository.findUserById(userId)
                .orElseThrow(() -> new ApplicationException(USER_NOT_FOUND));

        if ("ROLE_ADMIN".equalsIgnoreCase(newRole)) {
            throw new IllegalArgumentException("ADMIN 역할로 변경할 수 없습니다.");
        }

        UserRole updatedRole = UserRole.from(newRole);
        user.updateRole(updatedRole);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = adminRepository.findUserById(id)
                .orElseThrow(() -> new ApplicationException(USER_NOT_FOUND));
        user.delete();
    }

}
