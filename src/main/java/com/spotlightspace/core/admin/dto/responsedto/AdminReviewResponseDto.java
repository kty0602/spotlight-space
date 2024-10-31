package com.spotlightspace.core.admin.dto.responsedto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminReviewResponseDto {
    private Long id;
    private String eventTitle;
    private String userNickname;
    private String contents;
    private Integer rating;
    private Boolean isDeleted;


    public static AdminReviewResponseDto of(Long id, String eventTitle, String userNickname, String contents, Integer rating, Boolean isDeleted) {
        return new AdminReviewResponseDto(id, eventTitle, userNickname, contents, rating, isDeleted);
    }
}
