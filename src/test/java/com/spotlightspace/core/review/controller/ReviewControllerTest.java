package com.spotlightspace.core.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotlightspace.config.JwtUtil;
import com.spotlightspace.core.data.EventTestData;
import com.spotlightspace.core.data.ReviewTestData;
import com.spotlightspace.core.data.UserTestData;
import com.spotlightspace.core.likes.likesRequestDto.LikeUserResponseDto;
import com.spotlightspace.core.likes.service.LikeService;
import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.review.dto.GetReviewResponseDto;
import com.spotlightspace.core.review.dto.ReviewRequestDto;
import com.spotlightspace.core.review.dto.ReviewResponseDto;
import com.spotlightspace.core.review.dto.UpdateReviewRequestDto;
import com.spotlightspace.core.review.service.ReviewService;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.domain.UserRole;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtUtil jwtUtil;

    @MockBean
    ReviewService reviewService;

    @MockBean
    LikeService likeService;

    @Test
    @DisplayName("리뷰 생성 요청 시 리뷰를 생성할 수 있다.")
    @WithMockUser
    void createReview() throws Exception {
        // given
        ReviewRequestDto reviewRequestDto = ReviewRequestDto.of(3, "contentscontents", 1L);

        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "image.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47}
        );
        MockMultipartFile request = new MockMultipartFile(
                "reviewRequestDto",
                "reviewRequestDto.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(reviewRequestDto));

        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_USER);

        Review review = Review.of(ReviewTestData.createDefaultReviewRequestDto(), EventTestData.testEvent(),
                UserTestData.testUser());
        ReviewResponseDto reviewResponseDto = ReviewResponseDto.of(review, "attatchment");

        given(reviewService.createReview(any(), anyLong(), any(), any()))
                .willReturn(reviewResponseDto);

        // when & then
        mockMvc.perform(multipart("/api/v1/event/1/review")
                        .file(request)
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("accessToken", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.path").value("/api/v1/event/1/review"))
                .andExpect(jsonPath("$.data.id").isEmpty())
                .andExpect(jsonPath("$.data.nickname").value(reviewResponseDto.getNickname()))
                .andExpect(jsonPath("$.data.contents").value(reviewResponseDto.getContents()))
                .andExpect(jsonPath("$.data.rating").value(reviewResponseDto.getRating()))
                .andExpect(jsonPath("$.data.attachment").value(reviewResponseDto.getAttachment()));

        verify(reviewService, times(1)).createReview(any(), anyLong(), any(), any());
    }

    @Test
    @DisplayName("리뷰 다건 조회 요청 시 리뷰를 조회할 수 있다.")
    void getReviews() throws Exception {
        // given
        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_USER);

        User user = UserTestData.testUser();
        Review review = Review.of(ReviewTestData.createDefaultReviewRequestDto(), EventTestData.testEvent(),
                UserTestData.testUser());
        GetReviewResponseDto getReviewResponseDto = GetReviewResponseDto.of(review, "attachment", List.of(
                LikeUserResponseDto.from(user)), 1);

        given(reviewService.getReviews(anyLong(), any()))
                .willReturn(List.of(getReviewResponseDto));

        // when & then
        mockMvc.perform(get("/api/v1/event/{eventId}/reviews", 1)
                        .param("eventId", "1")
                        .header("accessToken", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.path").value("/api/v1/event/1/reviews"))
                .andExpect(jsonPath("$.data[0].nickname").value(getReviewResponseDto.getNickname()))
                .andExpect(jsonPath("$.data[0].contents").value(getReviewResponseDto.getContents()))
                .andExpect(jsonPath("$.data[0].rating").value(getReviewResponseDto.getRating()))
                .andExpect(jsonPath("$.data[0].attachment").value(getReviewResponseDto.getAttachment()))
                .andExpect(jsonPath("$.data[0].likeCount").value(getReviewResponseDto.getLikeCount()));

        verify(reviewService, times(1)).getReviews(anyLong(), any());
    }

    @Test
    @DisplayName("리뷰 수정 요청 시 리뷰를 저장할 수 있다.")
    void updateReview() throws Exception {
        // given
        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_USER);

        UpdateReviewRequestDto updateReviewRequestDto = UpdateReviewRequestDto.of(3, "contentscontents", "attachment");
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "image.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47}
        );
        MockMultipartFile request = new MockMultipartFile(
                "updateReviewRequestDto",
                "updateReviewRequestDto.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(updateReviewRequestDto));

        // when & then
        mockMvc.perform(multipart("/api/v1/event/{eventId}/reviews/{reviewId}", 1, 1)
                        .file(request)
                        .file(imageFile)
                        .header("accessToken", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(r -> {
                            r.setMethod("PATCH");
                            return r;
                        }))
                .andDo(print())
                .andExpect(status().isOk());

        verify(reviewService, times(1)).updateReview(anyLong(), any(), any(), any());
    }

    @Test
    @DisplayName("리뷰 삭제 요청 시 리뷰를 삭제할 수 있다.")
    void deleteReview() throws Exception {
        // given
        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_USER);

        // when & then
        mockMvc.perform(delete("/api/v1/event/{eventId}/reviews/{reviewId}", 1, 1)
                        .header("accessToken", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        verify(reviewService, times(1)).deleteReview(anyLong(), any());
    }

    @Test
    @DisplayName("리뷰에 좋아요 요청 시 좋아요를 할 수 있다.")
    void likeReview() throws Exception {
        // given
        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_USER);

        // when & then
        mockMvc.perform(post("/api/v1/event/{eventId}/reviews/likes/{reviewId}", 1, 1)
                        .header("accessToken", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        verify(likeService, times(1)).likeReview(anyLong(), any());
    }

    @Test
    @DisplayName("리뷰에 좋아요 취소 요청 시 좋아요를 취소할 수 있다.")
    void cancelLike() throws Exception {
        // given
        String accessToken = jwtUtil.createAccessToken(1L, "test@email.com", UserRole.ROLE_USER);

        // when & then
        mockMvc.perform(patch("/api/v1/event/{eventId}/reviews/likes/{reviewId}", 1, 1)
                        .header("accessToken", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        verify(likeService, times(1)).cancelLike(anyLong(), anyLong());
    }
}
