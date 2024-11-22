package com.spotlightspace.core.admin.service;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.admin.dto.requestdto.SearchAdminUserRequestDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import com.spotlightspace.core.admin.repository.AdminQueryRepository;
import com.spotlightspace.core.user.domain.User;
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

import static com.spotlightspace.common.exception.ErrorCode.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
        SearchAdminUserRequestDto requestDto = SearchAdminUserRequestDto.of("nickname", "email", null, "ROLE_USER", null, null, false, false);
        PageRequest pageable = PageRequest.of(0, 10);
        AdminUserResponseDto userDto = AdminUserResponseDto.of(
                1L, "test@example.com", "Test User", "010-1234-5678", "ROLE_USER", false
        );
        Page<AdminUserResponseDto> expectedPage = new PageImpl<>(Collections.singletonList(userDto));

        when(adminRepository.getAdminUsers(any(SearchAdminUserRequestDto.class), any(PageRequest.class))).thenReturn(expectedPage);

        // when
        Page<AdminUserResponseDto> result = adminUserService.getAdminUsers(1, 10, requestDto, "nickname", "asc");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNickname()).isEqualTo("Test User");
    }

    @Test
    void testUpdateUserRole_userNotFound_shouldFail() {
        // given
        Long userId = 1L;
        when(adminRepository.findUserById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminUserService.updateUserRole(userId, "ROLE_ARTIST"))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(USER_NOT_FOUND.getMessage());
    }

    @Test
    void testDeleteUser_Success() {
        // given
        User user = mock(User.class);
        when(adminRepository.findUserById(anyLong())).thenReturn(Optional.of(user));

        // when
        adminUserService.deleteUser(1L);

        // then
        verify(user, times(1)).delete();
    }

    @Test
    void testDeleteUser_UserNotFound() {
        // given
        when(adminRepository.findUserById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminUserService.deleteUser(1L))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(USER_NOT_FOUND.getMessage());
    }
}
