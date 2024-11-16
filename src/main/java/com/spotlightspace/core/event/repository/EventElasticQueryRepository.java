package com.spotlightspace.core.event.repository;

import com.spotlightspace.core.event.dto.request.SearchEventRequestDto;
import com.spotlightspace.core.event.dto.response.GetEventElasticResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface EventElasticQueryRepository {
    Page<GetEventElasticResponseDto> searchElasticEvents(
            SearchEventRequestDto requestDto, String type, Pageable pageable) throws IOException;
}
