package com.spotlightspace.core.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.exception.GlobalExceptionHandler;
import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.point.dto.request.CreatePointRequestDto;
import com.spotlightspace.core.point.dto.response.CreatePointResponseDto;
import com.spotlightspace.core.point.dto.response.GetPointResponseDto;
import com.spotlightspace.core.point.service.PointService;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static com.spotlightspace.core.data.PointTestData.createDefaultPointRequestDto;
import static com.spotlightspace.core.data.PointTestData.testPoint;
import static com.spotlightspace.core.data.UserTestData.testAuthUser;
import static com.spotlightspace.core.data.UserTestData.testUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(controllers = PointController.class)
@ContextConfiguration(classes = {PointController.class, GlobalExceptionHandler.class})
public class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PointService pointService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @Test
    @DisplayName("포인트 등록")
    void createPoint_Success() throws Exception {

        // given
        AuthUser authUser = testAuthUser();
        User user = testUser();
        CreatePointRequestDto requestDto = createDefaultPointRequestDto();
        Point point = testPoint();

        when(userRepository.findByIdOrElseThrow(authUser.getUserId())).thenReturn(user);
        when(pointService.createPoint(eq(requestDto.getPrice()), any()))
                .thenReturn(CreatePointResponseDto.from(point));

        // 현 포인트 컨트롤러에서 userRepository로 authUser를 사용하기 때문에 필요
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authUser, null, null)
        );

        // when & then
        mockMvc.perform(post("/api/v1/point")
                .content(objectMapper.writeValueAsString(requestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("포인트 조회")
    void getPoint_Success() throws Exception {

        // given
        User user = testUser();
        CreatePointRequestDto requestDto = createDefaultPointRequestDto();
        Point point = Point.of(requestDto.getPrice(), user);

        when(pointService.getPoint(any())).thenReturn(GetPointResponseDto.from(point));

        // when & then
        mockMvc.perform(get("/api/v1/point"))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
