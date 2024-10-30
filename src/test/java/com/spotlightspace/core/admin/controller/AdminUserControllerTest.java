package com.spotlightspace.core.admin.controller;

import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import com.spotlightspace.core.admin.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class AdminUserControllerTest {

    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private AdminUserController adminUserController;

    @Test
    public void testSearchUsers() {
        // given
        String keyword = null;
        int page = 1;
        int size = 10;
        String sortField = "id";
        String sortOrder = "asc";
        Page<AdminUserResponseDto> usersPage = new PageImpl<>(Collections.emptyList());

        when(adminUserService.getAdminUsers(page, size, keyword, sortField, sortOrder)).thenReturn(usersPage);

        // when
        ResponseEntity<Page<AdminUserResponseDto>> response = adminUserController.searchUsers(page, size, keyword, sortField, sortOrder);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
    }
}