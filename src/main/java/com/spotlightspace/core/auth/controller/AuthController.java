package com.spotlightspace.core.auth.controller;

import static com.spotlightspace.common.constant.JwtConstant.TOKEN_ACCESS_TIME;
import static com.spotlightspace.common.constant.JwtConstant.TOKEN_REFRESH_TIME;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spotlightspace.core.auth.dto.SaveTokenResponseDto;
import com.spotlightspace.core.auth.dto.SignInUserRequestDto;
import com.spotlightspace.core.auth.dto.SignUpUserRequestDto;
import com.spotlightspace.core.auth.service.AuthService;
import com.spotlightspace.core.auth.service.KakaoService;
import com.spotlightspace.core.user.dto.request.UpdatePasswordUserRequestDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final KakaoService kakaoService;

    @Value("${kakao.client_id}")
    private String kakaoClientId;

    @Value("${kakao.redirect_uri}")
    private String kakaoRedirectUri;

    /**
     * 회원가입 로직입니다
     *
     * @param signUpUserRequestDto 이메일, 비밀번호, 닉네임, 권한, 생일을 설정합니다
     * @param file                 유저의 프로필 파일을 업로드하며 필수는 아닙니다
     * @return
     * @throws IOException
     */
    @PostMapping("/auth/signup")
    public ResponseEntity<String> signUp(
            @Valid @RequestPart SignUpUserRequestDto signUpUserRequestDto,
            @RequestPart(required = false) MultipartFile file) throws IOException {
        authService.signUp(signUpUserRequestDto, file);
        return ResponseEntity.ok()
                .build();
    }

    /**
     * 로그인 로직입니다.
     *
     * @param signInUserRequestDto 아이디와 비밀번호를 받습니다
     * @param httpServletResponse  쿠키 저장용입니다
     * @return 헤더에 엑세스 토큰을 저장합니다
     * @throws IOException
     */
    @PostMapping("/auth/signin")
    public ResponseEntity<String> signIn(
            @Valid @RequestBody SignInUserRequestDto signInUserRequestDto,
            HttpServletResponse httpServletResponse) throws IOException {
        SaveTokenResponseDto tokenDto = authService.signIn(signInUserRequestDto);
        setAccessTokenCookie(httpServletResponse, tokenDto.getAccessToken());
        setRefreshTokenCookie(httpServletResponse, tokenDto.getRefreshToken());

        return ResponseEntity.ok()
                .header(AUTHORIZATION, tokenDto.getAccessToken())
                .build();
    }

    /**
     * 패스워드 변경을 구현했습니다.
     *
     * @param updateUserRequestDto 비밀번호와 이메일을 입력받습니다
     * @return
     */
    @PatchMapping("/auth/password")
    public ResponseEntity<Void> updatePassword(
            @Valid @RequestBody UpdatePasswordUserRequestDto updateUserRequestDto
    ) {
        authService.updatePassword(updateUserRequestDto);
        return ResponseEntity.ok().build();
    }

    /**
     * 리프레시 토큰을 사용하여 토큰을 재발급 받습니다
     *
     * @param httpServletRequest 쿠키에 있는 리프레시 토큰을 확인합니다
     * @return 헤더에 accessToken을 발급받습니다.
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/auth/refresh")
    public ResponseEntity<Void> getAccessToken(
            HttpServletRequest httpServletRequest) throws UnsupportedEncodingException {
        String accessToken = authService.getAccessToken(httpServletRequest);
        return ResponseEntity.ok()
                .header(AUTHORIZATION, accessToken)
                .build();
    }

    /**
     * kakao oauth 로그인을 콜백 처리합니다 kakao로 받은 인가코드를 통해 액세스토큰을 요청하고 받은 토큰을 refresh 토큰을 쿠키에 업로드합니다
     *
     * @param code     카카오에서 제공한 인가 코드
     * @param response 쿠키 설정및 리디렉션을 위한 response
     * @return 홈으로 리다이렉트 합니다.
     * @throws JsonProcessingException
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/auth/kakao/callback")
    public ResponseEntity<Void> kakaoLogin(@RequestParam String code, HttpServletResponse response)
            throws JsonProcessingException, UnsupportedEncodingException {

        String token = kakaoService.kakaoLogin(code);

        setRefreshTokenCookie(response, token);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/")
                .header(AUTHORIZATION, token)
                .build();
    }

    /**
     * 카카오에 로그인하기위한 url을 생성합니다.
     *
     * @return
     */
    @GetMapping("/auth/kakao/login-url")
    public ResponseEntity<String> getKakaoLoginUrl() {
        String kakaoLoginUrl = "https://kauth.kakao.com/oauth/authorize?client_id=" + kakaoClientId +
                "&redirect_uri=" + kakaoRedirectUri + "&response_type=code";
        return ResponseEntity.ok(kakaoLoginUrl);
    }


    private void setAccessTokenCookie(
            HttpServletResponse response,
            String accessToken) throws UnsupportedEncodingException {
        String cookieValue = URLEncoder.encode(accessToken, "utf-8").replaceAll("\\+", "%20");
        Cookie cookie = new Cookie("AccessToken", cookieValue);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) TOKEN_ACCESS_TIME);
        response.addCookie(cookie);
    }

    private void setRefreshTokenCookie(
            HttpServletResponse response,
            String accessToken) throws UnsupportedEncodingException {
        String cookieValue = URLEncoder.encode(accessToken, "utf-8").replaceAll("\\+", "%20");
        Cookie cookie = new Cookie("RefreshToken", cookieValue);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) TOKEN_REFRESH_TIME);
        response.addCookie(cookie);
    }

}
