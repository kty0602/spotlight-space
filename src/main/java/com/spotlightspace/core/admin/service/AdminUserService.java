package com.spotlightspace.core.admin.service;

import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import com.spotlightspace.core.admin.repository.AdminQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminQueryRepository adminRepository;

    public Page<AdminUserResponseDto> getAdminUsers(int page, int size, String keyword, String sortField, String sortOrder) {
        Sort sort = Sort.by(sortField);
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        return adminRepository.getAdminUsers(keyword, pageable);
    }
}
