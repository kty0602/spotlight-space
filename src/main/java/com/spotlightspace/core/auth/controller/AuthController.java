package com.spotlightspace.core.auth.controller;

import static com.spotlightspace.common.constant.JwtConstant.TOKEN_ACCESS_TIME;
import static com.spotlightspace.common.constant.JwtConstant.TOKEN_REFRESH_TIME;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spotlightspace.core.auth.dto.request.SignInUserRequestDto;
import com.spotlightspace.core.auth.dto.request.SignUpUserRequestDto;
import com.spotlightspace.core.auth.dto.response.SaveTokenResponseDto;
import com.spotlightspace.core.auth.dto.response.SignUpUserResponseDto;
import com.spotlightspace.core.auth.service.AuthService;
import com.spotlightspace.core.auth.service.KakaoService;
import com.spotlightspace.core.auth.service.NaverService;
import com.spotlightspace.core.user.dto.request.UpdatePasswordUserRequestDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
    private final NaverService naverService;

    @Value("${kakao.client_id}")
    private String kakaoClientId;

    @Value("${kakao.redirect_uri}")
    private String kakaoRedirectUri;

    @Value("${naver.redirect_uri}")
    private String naverRedirectUri;

    @Value("${naver.client_id}")
    private String naverClientId;

    /**
     * 회원가입 로직입니다
     *
     * @param signUpUserRequestDto 이메일, 비밀번호, 닉네임, 권한, 생일을 설정합니다
     * @param file                 유저의 프로필 파일을 업로드하며 필수는 아닙니다
     * @return 생성된 유저 정보를 반환하며 201 CREATED 로 반환됩니다.
     * @throws IOException
     */
    @PostMapping("/auth/signup")
    public ResponseEntity<SignUpUserResponseDto> signUp(
            @Valid @RequestPart SignUpUserRequestDto signUpUserRequestDto,
            @RequestPart(required = false) MultipartFile file
    ) throws IOException {
        SignUpUserResponseDto signupUserResponseDto = authService.signUp(signUpUserRequestDto, file);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(signupUserResponseDto);
    }

    /**
     * 로그인 로직입니다.
     *
     * @param signInUserRequestDto 아이디와 비밀번호를 받습니다
     * @param httpServletResponse  쿠키 저장용입니다
     * @return 200OK와 유저 정보를 반환하며 헤더에 엑세스 토큰을 저장합니다
     * @throws IOException
     */
    @PostMapping("/auth/signin")
    public ResponseEntity<Map<String, String>> signIn(
            @Valid @RequestBody SignInUserRequestDto signInUserRequestDto,
            HttpServletResponse httpServletResponse
    ) throws IOException {
        SaveTokenResponseDto tokenDto = authService.signIn(signInUserRequestDto);
        setAccessTokenCookie(httpServletResponse, tokenDto.getAccessToken());
        setRefreshTokenCookie(httpServletResponse, tokenDto.getRefreshToken());

        Map<String, String> response = new HashMap<>();
        response.put("message", "성공적으로 로그인 되었습니다.");

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(AUTHORIZATION, tokenDto.getAccessToken())
                .body(response);
    }

    /**
     * 패스워드 변경을 구현했습니다.
     *
     * @param updateUserRequestDto 비밀번호와 이메일을 입력받습니다
     * @return 변경되었다는 메세지를 출력합니다.
     */
    @PatchMapping("/auth/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @Valid @RequestBody UpdatePasswordUserRequestDto updateUserRequestDto
    ) {
        authService.updatePassword(updateUserRequestDto);

        Map<String, String> response = new HashMap<>();
        response.put("message", "성공적으로 비밀번호가 변경되었습니다!");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    /**
     * 리프레시 토큰을 사용하여 토큰을 재발급 받습니다.
     *
     * @param httpServletRequest 쿠키에 있는 리프레시 토큰을 확인합니다
     * @return 헤더에 accessToken을 발급받습니다. 또한 발급되었습니다 메세지를 출력합니다.
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/auth/refresh")
    public ResponseEntity<Map<String, String>> getAccessToken(
            HttpServletRequest httpServletRequest) throws UnsupportedEncodingException {
        String refreshToken = null;

        //쿠키에서 리프레시 토큰을 가져오는 로직입니다
        if (httpServletRequest.getCookies() != null) {
            for (Cookie cookie : httpServletRequest.getCookies()) {
                if ("RefreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        String accessToken = authService.getAccessToken(refreshToken);

        Map<String, String> response = new HashMap<>();
        response.put("message", "성공적으로 액세스 토큰이 발급되었습니다!");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(AUTHORIZATION, accessToken)
                .body(response);
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

        return ResponseEntity
                .status(HttpStatus.FOUND)
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

    /**
     * naver 로그인의 콜백을 처리합니다, naver로 부터 받은 인가코드를 통해 엑세스 토큰을 요청하고 받은 토큰을 이용하여 리프레시 토큰을생성하여 쿠키에 업로드합니다
     *
     * @param code     인가 코드
     * @param state    상태값
     * @param response 리프레시 토큰을 쿠키에 추가하기위해 사용
     * @return 루트 경로로 리디렉션합니다
     * @throws UnsupportedEncodingException
     * @throws JsonProcessingException
     */
    @GetMapping("/auth/naver/callback")
    public ResponseEntity<Void> naverLogin(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletResponse response
    ) throws UnsupportedEncodingException, JsonProcessingException {

        String token = naverService.naverLogin(code, state);

        setRefreshTokenCookie(response, token);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/")
                .header(AUTHORIZATION, token)
                .build();
    }

    /**
     * 네이버에 로그인하기 위한 URL을 생성합니다.
     *
     * @return 네이버 로그인 URL
     */
    @GetMapping("/auth/naver/login-url")
    public ResponseEntity<String> getNaverLoginUrl() throws UnsupportedEncodingException {
        String state = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8");

        String naverLoginUrl = "https://nid.naver.com/oauth2.0/authorize?client_id=" + naverClientId +
                "&redirect_uri=" + naverRedirectUri + "&response_type=code&state=" + state;
        return ResponseEntity.ok(naverLoginUrl);
    }

    private void setAccessTokenCookie(
            HttpServletResponse response,
            String accessToken
    ) throws UnsupportedEncodingException {
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
