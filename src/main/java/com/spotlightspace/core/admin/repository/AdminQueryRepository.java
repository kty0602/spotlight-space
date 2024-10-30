package com.spotlightspace.core.admin.repository;

import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface AdminQueryRepository {
    Page<AdminUserResponseDto> getAdminUsers(String keyword, Pageable pageable);

}
