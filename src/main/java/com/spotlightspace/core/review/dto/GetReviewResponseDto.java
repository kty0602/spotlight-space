package com.spotlightspace.core.review.dto;

import com.spotlightspace.core.likes.likesRequestDto.LikeUserResponseDto;
import com.spotlightspace.core.review.domain.Review;
import lombok.Getter;

import java.util.List;

@Getter
public class GetReviewResponseDto {

    private Long id;
    private String nickname;
    private String contents;
    private Integer rating;
    private String attachment;
    private int likeCount;
    private List<LikeUserResponseDto> likeUsers;

    private GetReviewResponseDto(Review review, String attachment, List<LikeUserResponseDto> likeUserDtos, int likeCount) {
        this.id = review.getId();
        this.nickname = review.getUser().getNickname();
        this.rating = review.getRating();
        this.contents = review.getContents();
        this.attachment = attachment;
        this.likeUsers = likeUserDtos;
        this.likeCount = likeCount;
    }

    public static GetReviewResponseDto of(Review review, String attachment, List<LikeUserResponseDto> likeUserDtos, int likeCount) {
        return new GetReviewResponseDto(review, attachment, likeUserDtos, likeCount);
    }

}
