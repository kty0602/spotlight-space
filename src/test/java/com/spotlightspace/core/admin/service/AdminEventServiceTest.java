package com.spotlightspace.core.admin.service;

import com.spotlightspace.core.admin.dto.responsedto.AdminEventResponseDto;
import com.spotlightspace.core.admin.repository.AdminQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class AdminEventServiceTest {

    @Mock
    private AdminQueryRepository adminQueryRepository;

    @InjectMocks
    private AdminEventService adminEventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAdminEvents_withKeyword() {
        // given: 테스트 데이터를 설정
        String keyword = "example";
        int page = 1;
        int size = 10;
        String sortField = "id";
        String sortOrder = "asc";
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, sortField));
        List<AdminEventResponseDto> eventList = List.of(
                new AdminEventResponseDto(1L, "제목1", "내용1", "서울", null, null, 100, 10000, "카테고리", null, null, false)
        );
        Page<AdminEventResponseDto> expectedPage = new PageImpl<>(eventList, pageable, 1);

        // when: 검색어가 포함된 쿼리를 수행할 때 adminQueryRepository의 동작을 정의
        when(adminQueryRepository.getAdminEvents(keyword, pageable)).thenReturn(expectedPage);

        // then: 서비스 메서드가 올바른 페이지 객체를 반환하는지 확인
        Page<AdminEventResponseDto> result = adminEventService.getAdminEvents(page, size, keyword, sortField, sortOrder);
        assertNotNull(result); // 결과가 null이 아님을 검증
        assertEquals(expectedPage, result);
    }

    @Test
    void testGetAdminEvents_withoutKeyword() {
        // given: 검색어 없이 테스트 데이터를 설정
        String keyword = null;
        int page = 1;
        int size = 10;
        String sortField = "id";
        String sortOrder = "asc";
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, sortField));
        List<AdminEventResponseDto> eventList = List.of(
                new AdminEventResponseDto(1L, "제목1", "내용1", "서울", null, null, 100, 10000, "카테고리", null, null, false)
        );
        Page<AdminEventResponseDto> expectedPage = new PageImpl<>(eventList, pageable, 1);

        // when: 검색어 없이 쿼리를 수행할 때 adminQueryRepository의 동작을 정의
        when(adminQueryRepository.getAdminEvents(keyword, pageable)).thenReturn(expectedPage);

        // then: 서비스 메서드가 올바른 페이지 객체를 반환하는지 확인
        Page<AdminEventResponseDto> result = adminEventService.getAdminEvents(page, size, keyword, sortField, sortOrder);
        assertNotNull(result); // 결과가 null이 아님을 검증
        assertEquals(expectedPage, result);
    }

    @Test
    void testGetAdminEvents_noResults() {
        // given: 검색 결과가 없는 경우를 테스트
        String keyword = "not_exist";
        int page = 1;
        int size = 10;
        String sortField = "id";
        String sortOrder = "asc";
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, sortField));
        Page<AdminEventResponseDto> expectedPage = Page.empty(pageable);

        // when: 검색어가 포함된 쿼리를 수행할 때 adminQueryRepository의 동작을 정의
        when(adminQueryRepository.getAdminEvents(keyword, pageable)).thenReturn(expectedPage);

        // then: 서비스 메서드가 빈 페이지 객체를 반환하는지 확인
        Page<AdminEventResponseDto> result = adminEventService.getAdminEvents(page, size, keyword, sortField, sortOrder);
        assertNotNull(result); // 결과가 null이 아님을 검증
        assertEquals(expectedPage, result);
    }
}
