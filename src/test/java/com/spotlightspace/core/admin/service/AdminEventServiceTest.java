package com.spotlightspace.core.admin.service;

import com.spotlightspace.core.admin.dto.responsedto.AdminEventResponseDto;
import com.spotlightspace.core.admin.repository.AdminQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class AdminEventServiceTest {

    @Mock
    private AdminQueryRepository adminRepository;

    @InjectMocks
    private AdminEventService adminEventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAdminEvents_withKeyword() {
        // given
        String keyword = "test";
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
        AdminEventResponseDto eventDto = AdminEventResponseDto.from(
                1L, "Test Event", "Content", "Location", null, null,
                100, 1000, "Category", null, null, false
        );
        Page<AdminEventResponseDto> expectedPage = new PageImpl<>(Collections.singletonList(eventDto));

        when(adminRepository.getAdminEvents(anyString(), any(PageRequest.class))).thenReturn(expectedPage);

        // when
        Page<AdminEventResponseDto> result = adminEventService.getAdminEvents(1, 10, keyword, "title", "asc");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Event");
    }

    @Test
    void testGetAdminEvents_withoutKeyword() {
        // given
        String keyword = null;
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
        Page<AdminEventResponseDto> expectedPage = new PageImpl<>(Collections.emptyList());

        when(adminRepository.getAdminEvents(isNull(), any(PageRequest.class))).thenReturn(expectedPage);

        // when
        Page<AdminEventResponseDto> result = adminEventService.getAdminEvents(1, 10, keyword, "title", "asc");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }
}
