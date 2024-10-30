package com.spotlightspace.core.event.repository;

import com.spotlightspace.core.event.dto.response.GetEventResponseDto;
import com.spotlightspace.core.event.dto.request.SearchEventRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventQueryRepository {

    Page<GetEventResponseDto> searchEvents(SearchEventRequestDto requestDto, String type, Pageable pageable);

}
