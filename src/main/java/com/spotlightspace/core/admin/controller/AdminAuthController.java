package com.spotlightspace.core.admin.controller;

import com.spotlightspace.core.admin.dto.requestdto.AdminLoginRequestDto;
import com.spotlightspace.core.admin.service.AdminAuthService;
import com.spotlightspace.core.auth.dto.response.SaveTokenResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.spotlightspace.common.constant.JwtConstant.TOKEN_ACCESS_TIME;
import static com.spotlightspace.common.constant.JwtConstant.TOKEN_REFRESH_TIME;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * @param adminLoginRequestDto
     * @param httpServletResponse
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/auth")
    public ResponseEntity<String> adminSignIn(
            @Valid @RequestBody AdminLoginRequestDto adminLoginRequestDto,
            HttpServletResponse httpServletResponse
    ) throws UnsupportedEncodingException {

        SaveTokenResponseDto tokenDto = adminAuthService.adminSignIn(adminLoginRequestDto);
        setAccessTokenCookie(httpServletResponse, tokenDto.getAccessToken());
        setRefreshTokenCookie(httpServletResponse, tokenDto.getRefreshToken());

        return ResponseEntity.ok()
                .header(AUTHORIZATION, tokenDto.getAccessToken())
                .build();

    }

    private void setAccessTokenCookie(
            HttpServletResponse response,
            String accessToken
    ) throws UnsupportedEncodingException {
        String cookieValue = URLEncoder.encode(accessToken, "utf-8").replaceAll("\\+", "%20");
        Cookie cookie = new Cookie("Authorization", cookieValue);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) TOKEN_ACCESS_TIME);
        response.addCookie(cookie);
    }

    private void setRefreshTokenCookie(
            HttpServletResponse response,
            String accessToken
    ) throws UnsupportedEncodingException {
        String cookieValue = URLEncoder.encode(accessToken, "utf-8").replaceAll("\\+", "%20");
        Cookie cookie = new Cookie("RefreshToken", cookieValue);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) TOKEN_REFRESH_TIME);
        response.addCookie(cookie);
    }
}
