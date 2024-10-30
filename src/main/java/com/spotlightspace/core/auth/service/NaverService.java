package com.spotlightspace.core.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotlightspace.config.JwtUtil;
import com.spotlightspace.core.auth.dto.response.NaverUserInfoDto;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.domain.UserRole;
import com.spotlightspace.core.user.repository.UserRepository;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "NAVER Login")
public class NaverService {

    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${naver.client_id}")
    private String naverClientId;

    @Value("${naver.client_secret}")
    private String naverClientSecret;

    @Value("${naver.redirect_uri}")
    private String naverRedirectUri;

    public String naverLogin(String code, String state) throws JsonProcessingException, UnsupportedEncodingException {
        //"인가 코드"로 "액세스 토큰" 요청
        String accessToken = getNaverToken(code, state);

        //토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        NaverUserInfoDto naverUserInfoDto = getNaverUserInfo(accessToken);

        if (!authService.alreadySignUp(naverUserInfoDto.getEmail())) {
            authService.signUpNaverUser(
                    naverUserInfoDto.getId(),
                    naverUserInfoDto.getNickname(),
                    naverUserInfoDto.getEmail(),
                    naverUserInfoDto.getMobile());
        }

        User user = userRepository.findByEmailOrElseThrow(naverUserInfoDto.getEmail());

        //사용자 정보를 이용해 JWT 토큰 생성
        String jwtToken = jwtUtil.createAccessToken(
                user.getId(),
                naverUserInfoDto.getEmail(),
                UserRole.ROLE_USER);

        jwtUtil.createRefreshToken(
                user.getId(),
                naverUserInfoDto.getEmail(),
                UserRole.ROLE_USER);

        return jwtToken;
    }

    public String getNaverToken(String code, String state)
            throws JsonProcessingException, UnsupportedEncodingException {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://nid.naver.com")
                .path("/oauth2.0/token")
                .encode()
                .build()
                .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", naverClientId);
        body.add("client_secret", naverClientSecret);
        body.add("redirect_uri", URLEncoder.encode(naverRedirectUri, "UTF-8")); // 추가
        body.add("code", code);
        body.add("state", state);

        // RequestEntity 생성
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


    public NaverUserInfoDto getNaverUserInfo(String accessToken) throws JsonProcessingException {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com")
                .path("/v1/nid/me")
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

        // 사용자 정보 가져오기
        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());

        JsonNode responseNode = jsonNode.get("response");
        log.info("네이버 사용자 전체 정보: {}", responseNode.toString());
        Long id = responseNode.get("id").asLong();
        String nickname = responseNode.get("nickname").asText();
        String email = responseNode.get("email").asText();
        String mobile = responseNode.get("mobile").asText();

        log.info("네이버 사용자 정보: {}, {}, {}, {}", id, nickname, email, mobile);
        return NaverUserInfoDto.of(id, nickname, email, mobile);
    }

}
