package com.spotlightspace.core.admin.service;

import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
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

class AdminUserServiceTest {

    @Mock
    private AdminQueryRepository adminRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAdminUsers_withKeyword() {
        // given
        String keyword = "test";
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("nickname").ascending());
        AdminUserResponseDto userDto = AdminUserResponseDto.of(
                1L, "test@example.com", "Test User", "010-1234-5678", "USER", false
        );
        Page<AdminUserResponseDto> expectedPage = new PageImpl<>(Collections.singletonList(userDto));

        // when
        when(adminRepository.getAdminUsers(anyString(), any(PageRequest.class))).thenReturn(expectedPage);
        Page<AdminUserResponseDto> result = adminUserService.getAdminUsers(1, 10, keyword, "nickname", "asc");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNickname()).isEqualTo("Test User");
    }

    @Test
    void testGetAdminUsers_withoutKeyword() {
        // given
        String keyword = null;
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("nickname").ascending());
        Page<AdminUserResponseDto> expectedPage = new PageImpl<>(Collections.emptyList());

        // when
        when(adminRepository.getAdminUsers(isNull(), any(PageRequest.class))).thenReturn(expectedPage);
        Page<AdminUserResponseDto> result = adminUserService.getAdminUsers(1, 10, keyword, "nickname", "asc");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }
}
