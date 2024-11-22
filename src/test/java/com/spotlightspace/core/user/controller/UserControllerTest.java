package com.spotlightspace.core.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotlightspace.common.exception.ErrorResponseDto;
import com.spotlightspace.config.JwtUtil;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.domain.UserRole;
import com.spotlightspace.core.user.dto.request.UpdateUserRequestDto;
import com.spotlightspace.core.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtUtil jwtUtil;

    @MockBean
    UserService userService;
    @Autowired
    private User user;

    @Test
    @DisplayName("유저 업데이트 요청 시 유저 정보가 업데이트된다.")
    void updateUser() throws Exception {
        // given
        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_USER);

        UpdateUserRequestDto updateUserRequestDto = new UpdateUserRequestDto("Test1111!", "nickname", "birth",
                "010-1234-5678", "location");
        MockMultipartFile request = new MockMultipartFile(
                "updateUserRequestDto",
                "updateUserRequestDto.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(updateUserRequestDto));
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "image.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47}
        );

        // when & then
        mockMvc.perform(multipart("/api/v1/user/{userId}", 1)
                        .file(request)
                        .file(imageFile)
                        .header("accessToken", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(r -> {
                            r.setMethod("PATCH");
                            return r;
                        }))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUser(anyLong(), any(), any(), any());
    }

    @Test
    @DisplayName("유저 업데이트 요청 시 유저 생년월인은 필수 값이다.")
    void updateUserWithoutBirth() throws Exception {
        // given
        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_USER);

        UpdateUserRequestDto updateUserRequestDto = new UpdateUserRequestDto("Test1111!", "nickname", null,
                "010-1234-5678", "location");
        MockMultipartFile request = new MockMultipartFile(
                "updateUserRequestDto",
                "updateUserRequestDto.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(updateUserRequestDto));
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "image.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47}
        );

        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(400, "잘못된 요청입니다.");
        errorResponseDto.addValidation("birth", "생년월일을 입력해주세요");

        // when & then
        mockMvc.perform(multipart("/api/v1/user/{userId}", 1)
                        .file(request)
                        .file(imageFile)
                        .header("accessToken", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(r -> {
                            r.setMethod("PATCH");
                            return r;
                        }))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/user/1"))
                .andExpect(jsonPath("$.data.statusCode").value(400))
                .andExpect(jsonPath("$.data.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.data.validations.birth").value("생년월일을 입력해주세요"));

        verify(userService, never()).updateUser(anyLong(), any(), any(), any());
    }

    @Test
    @DisplayName("유저 조회 요청 시 유저 정보가 조회된다.")
    void getUser() throws Exception {
        // given
        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_USER);

        // when & then
        mockMvc.perform(get("/api/v1/user/{userId}", 1)
                        .header("accessToken", accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService, times(1)).getUser(anyLong(), any());
    }

    @Test
    @DisplayName("유저 삭제 요청 시 유저가 삭제된다.")
    void deleteUser() throws Exception {
        // given
        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_USER);

        // when & then
        mockMvc.perform(delete("/api/v1/user/{userId}", 1)
                        .header("accessToken", accessToken)
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(anyLong(), any(), anyString());
    }

    @Test
    @DisplayName("쿠폰 조회 요청 시 쿠폰이 정상적으로 조회된다.")
    void getCoupons() throws Exception {
        // given
        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_USER);

        // when & then
        mockMvc.perform(get("/api/v1/user/{userId}/coupons", 1)
                        .header("accessToken", accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService, times(1)).getCoupons(anyLong(), any());
    }
}
