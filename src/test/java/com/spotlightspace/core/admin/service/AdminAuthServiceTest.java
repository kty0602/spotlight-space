package com.spotlightspace.core.admin.service;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.config.JwtUtil;
import com.spotlightspace.core.admin.domain.Admin;
import com.spotlightspace.core.admin.dto.requestdto.AdminLoginRequestDto;
import com.spotlightspace.core.admin.repository.AdminRepository;
import com.spotlightspace.core.auth.dto.response.SaveTokenResponseDto;
import com.spotlightspace.core.user.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static com.spotlightspace.common.exception.ErrorCode.ADMIN_NOT_FOUND;
import static com.spotlightspace.common.exception.ErrorCode.ADMIN_PASSWORD_MISMATCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AdminAuthServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AdminAuthService adminAuthService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(adminAuthService, "credentials", "testAdminPassword");
    }


    @Test
    void testAdminSignIn_Success() {
        // given
        String email = "admin@spotlightspace.com";
        String password = "securePassword";
        String certified = "testAdminPassword";

        AdminLoginRequestDto requestDto = AdminLoginRequestDto.of(email, password, certified);
        Admin admin = mock(Admin.class);

        when(adminRepository.findByEmailOrElseThrow(email)).thenReturn(admin);
        when(admin.getEmail()).thenReturn(email);
        when(admin.getRole()).thenReturn(UserRole.ROLE_ADMIN);

        String accessToken = "mockAccessToken";
        String refreshToken = "mockRefreshToken";

        when(jwtUtil.createAccessToken(anyLong(), anyString(), any())).thenReturn(accessToken);
        when(jwtUtil.createRefreshToken(anyLong(), anyString(), any())).thenReturn(refreshToken);

        // when
        SaveTokenResponseDto responseDto = adminAuthService.adminSignIn(requestDto);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getAccessToken()).isEqualTo(accessToken);
        assertThat(responseDto.getRefreshToken()).isEqualTo(refreshToken);
    }


    @Test
    void testAdminSignIn_AdminNotFound() {
        // given
        String email = "nonexistent@spotlightspace.com";
        String password = "securePassword";
        String certified = "VALID_CERTIFIED";

        AdminLoginRequestDto requestDto = AdminLoginRequestDto.of(email, password, certified);
        when(adminRepository.findByEmailOrElseThrow(anyString())).thenThrow(new ApplicationException(ADMIN_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> adminAuthService.adminSignIn(requestDto))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(ADMIN_NOT_FOUND.getMessage());
    }

    @Test
    void testAdminSignIn_InvalidCertified() {
        // given
        String email = "admin@spotlightspace.com";
        String password = "securePassword";
        String certified = "INVALID_CERTIFIED";

        AdminLoginRequestDto requestDto = AdminLoginRequestDto.of(email, password, certified);
        Admin admin = mock(Admin.class);
        when(adminRepository.findByEmailOrElseThrow(email)).thenReturn(admin);

        // when & then
        assertThatThrownBy(() -> adminAuthService.adminSignIn(requestDto))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(ADMIN_PASSWORD_MISMATCH.getMessage());
    }
}
