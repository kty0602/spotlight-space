package com.spotlightspace.core.likes.likesRequestDto;

public class LikesResponseDto {

    private int likeCount;
    private boolean isLiked;

    private LikesResponseDto(int likeCount, boolean isLiked) {
        this.likeCount = likeCount;
        this.isLiked = isLiked;
    }

    public static LikesResponseDto of(int likeCount, boolean isLiked) {
        return new LikesResponseDto(likeCount, isLiked);
    }
}
