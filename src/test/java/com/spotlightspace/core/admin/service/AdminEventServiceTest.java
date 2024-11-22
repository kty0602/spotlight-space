package com.spotlightspace.core.admin.service;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.admin.dto.responsedto.AdminEventResponseDto;
import com.spotlightspace.core.admin.repository.AdminQueryRepository;
import com.spotlightspace.core.event.domain.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;

import static com.spotlightspace.common.exception.ErrorCode.EVENT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
        PageRequest pageable = PageRequest.of(0, 10);
        AdminEventResponseDto eventDto = AdminEventResponseDto.of(
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
    void testDeleteEvent_Success() {
        // given
        Event event = mock(Event.class);
        when(adminRepository.findEventById(anyLong())).thenReturn(Optional.of(event));

        // when
        adminEventService.deleteEvent(1L);

        // then
        verify(event, times(1)).deleteEvent();
    }

    @Test
    void testDeleteEvent_EventNotFound() {
        // given
        when(adminRepository.findEventById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminEventService.deleteEvent(1L))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(EVENT_NOT_FOUND.getMessage());
    }
}
