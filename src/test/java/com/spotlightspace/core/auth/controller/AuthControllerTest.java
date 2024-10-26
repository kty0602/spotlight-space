package com.spotlightspace.core.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotlightspace.config.JwtUtil;
import com.spotlightspace.core.auth.dto.SigninUserRequestDto;
import com.spotlightspace.core.auth.dto.SignupUserRequestDto;
import com.spotlightspace.core.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static com.spotlightspace.core.data.UserTestData.testSigninUserRequestDto;
import static com.spotlightspace.core.data.UserTestData.testSignupUserRequestDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@WithMockUser
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

//    @Test
//    @DisplayName("회원가입 성공 테스트")
//    void signUpSuccessTest() throws Exception {
//    }

//    @Test
//    @DisplayName("로그인 성공 테스트")
//    void signInSuccessTest() throws Exception {
//        // given
//        SigninUserRequestDto requestDto = testSigninUserRequestDto();
//
//        String accessToken = "token";
//
//        given(authService.signin(any(SigninUserRequestDto.class))).willReturn(accessToken);
//
//        // when & then
//        mockMvc.perform(post("/api/v1/auth/signin")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto))
//                        .with(csrf().asHeader()))
//                .andExpect(status().isOk())
//                .andExpect(header().string(HttpHeaders.AUTHORIZATION, accessToken));
//    }
}
