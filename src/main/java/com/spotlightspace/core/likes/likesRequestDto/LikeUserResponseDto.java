package com.spotlightspace.core.likes.likesRequestDto;

import com.spotlightspace.core.user.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeUserResponseDto {
    private String nickname;

    public static LikeUserResponseDto of(User user) {
        return new LikeUserResponseDto(user.getNickname());
    }
}
