package com.spotlightspace.core.review.dto;

import com.spotlightspace.core.user.dto.request.UpdatePasswordUserRequestDto;
import lombok.Getter;

@Getter
public class UpdateReviewRequestDto {

    private Integer rating;

    private String contents;

    private String attachment;

    private UpdateReviewRequestDto(Integer rating, String contents, String attachment) {
        this.rating = rating;
        this.contents = contents;
        this.attachment = attachment;
    }

    public static UpdateReviewRequestDto of(Integer rating, String contents, String attachment) {
        return new UpdateReviewRequestDto(rating, contents, attachment);
    }
}
