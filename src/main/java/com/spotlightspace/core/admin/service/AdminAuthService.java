package com.spotlightspace.core.admin.service;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.config.JwtUtil;
import com.spotlightspace.core.admin.domain.Admin;
import com.spotlightspace.core.admin.dto.requestdto.AdminLoginRequestDto;
import com.spotlightspace.core.admin.repository.AdminRepository;
import com.spotlightspace.core.auth.dto.response.SaveTokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.spotlightspace.common.exception.ErrorCode.ADMIN_PASSWORD_MISMATCH;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;

    @Value("${admin.credentials}")
    private String credentials;

    public SaveTokenResponseDto adminSignIn(AdminLoginRequestDto adminLoginRequestDto) {
        Admin admin = adminRepository.findByEmailOrElseThrow(adminLoginRequestDto.getEmail());

        if (!Objects.equals(adminLoginRequestDto.getCertified(), credentials)) {
            throw new ApplicationException(ADMIN_PASSWORD_MISMATCH);
        }

        String accessToken = jwtUtil.createAccessToken(admin.getId(), admin.getEmail(), admin.getRole());
        String refreshToken = jwtUtil.createRefreshToken(admin.getId(), admin.getEmail(), admin.getRole());

        return SaveTokenResponseDto.of(accessToken, refreshToken);
    }
}
