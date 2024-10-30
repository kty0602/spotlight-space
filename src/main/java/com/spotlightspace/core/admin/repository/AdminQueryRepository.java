package com.spotlightspace.core.admin.repository;

import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import org.springframework.data.domain.Page;


public interface AdminQueryRepository {
    Page<AdminUserResponseDto> getAdminUsers(String keyword, org.springframework.data.domain.Pageable pageable);

}