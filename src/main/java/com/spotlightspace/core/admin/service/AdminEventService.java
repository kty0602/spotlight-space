package com.spotlightspace.core.admin.service;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.admin.dto.responsedto.AdminEventResponseDto;
import com.spotlightspace.core.admin.repository.AdminQueryRepository;
import com.spotlightspace.core.event.domain.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.spotlightspace.common.exception.ErrorCode.EVENT_NOT_FOUND;
import static com.spotlightspace.common.util.SortFieldValidator.validateSortField;

@Service
@RequiredArgsConstructor

public class AdminEventService {

    private final AdminQueryRepository adminRepository;

    public Page<AdminEventResponseDto> getAdminEvents(int page, int size, String keyword, String sortField, String sortOrder) {
        validateSortField(sortField);

        Sort sort = Sort.by(sortField);
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        return adminRepository.getAdminEvents(keyword, pageable);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = adminRepository.findEventById(id)
                .orElseThrow(() -> new ApplicationException(EVENT_NOT_FOUND));
        event.deleteEvent();
    }

}
