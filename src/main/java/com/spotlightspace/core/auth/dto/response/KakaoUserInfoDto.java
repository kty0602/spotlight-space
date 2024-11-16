package com.spotlightspace.core.auth.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfoDto {

    private Long id;
    private String nickname;
    private String email;
    private String image;

    private KakaoUserInfoDto(Long id, String nickname, String email, String image) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.image = image;
    }

    public static KakaoUserInfoDto of(Long id, String nickname, String email, String image) {
        return new KakaoUserInfoDto(id, nickname, email, image);
    }
}
