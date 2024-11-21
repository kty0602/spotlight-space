package com.spotlightspace.core.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotlightspace.config.JwtUtil;
import com.spotlightspace.core.auth.dto.response.KakaoUserInfoDto;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.domain.UserRole;
import com.spotlightspace.core.user.repository.UserRepository;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "KAKAO Login")
public class KakaoService {

    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${kakao.client_id}")
    private String kakaoClientId;

    @Value("${kakao.redirect_uri}")
    private String kakaoRedirectUri;

    public String kakaoLogin(String code) throws JsonProcessingException {
        //"인가 코드"로 "액세스 토큰" 요청
        String accessToken = getToken(code);

        //토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);

        if (!authService.alreadySignUp(kakaoUserInfo.getEmail())) {
            authService.signUpKakaoUser(
                    kakaoUserInfo.getId(),
                    kakaoUserInfo.getNickname(),
                    kakaoUserInfo.getEmail(),
                    kakaoUserInfo.getImage());
        }

        User user = userRepository.findByEmailOrElseThrow(kakaoUserInfo.getEmail());

        //사용자 정보를 이용해 JWT 토큰 생성
        String jwtToken = jwtUtil.createAccessToken(
                user.getId(),
                kakaoUserInfo.getEmail(),
                UserRole.ROLE_USER);

        jwtUtil.createRefreshToken(
                user.getId(),
                kakaoUserInfo.getEmail(),
                UserRole.ROLE_USER);

        return jwtToken;
    }

    private String getToken(String code) throws JsonProcessingException {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com")
                .path("/oauth/token")
                .encode()
                .build()
                .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoRedirectUri);
        body.add("code", code);

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(body);

        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        return jsonNode.get("access_token").asText();
    }

    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com")
                .path("/v2/user/me")
                .encode()
                .build()
                .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(new LinkedMultiValueMap<>());

        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );

        //사용자 정보 가져오기
        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());

        long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();

        String image = null;

        JsonNode profileNode = jsonNode.path("kakao_account").path("profile");
        if (!profileNode.isMissingNode()) {
            image = profileNode.path("profile_image_url").asText(null);
        }

        log.info("카카오 사용자 정보: {}, {}, {}, {}", id, nickname, email, image);
        return KakaoUserInfoDto.of(id, nickname, email, image);
    }
}

