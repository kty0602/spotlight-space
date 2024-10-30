package com.spotlightspace.core.admin.service;

import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import com.spotlightspace.core.admin.repository.AdminQueryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class AdminUserServiceTest {

    @Autowired
    private AdminUserService adminUserService;

    @Mock
    @Qualifier("adminQueryRepositoryImpl")
    private AdminQueryRepository adminQueryRepository;


    @Test
    public void testGetAdminUsers() {
        // given
        String keyword = null;
        int page = 1;
        int size = 10;
        String sortField = "id";
        String sortOrder = "asc";

        // when
        Page<AdminUserResponseDto> users = adminUserService.getAdminUsers(page, size, keyword, sortField, sortOrder);

        // then
        assertThat(users).isNotNull();
        assertThat(users.getContent().size()).isLessThanOrEqualTo(size);
    }
}