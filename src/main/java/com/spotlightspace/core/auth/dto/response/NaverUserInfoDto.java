package com.spotlightspace.core.auth.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverUserInfoDto {

    private long id;
    private String nickname;
    private String email;
    private String mobile;

    private NaverUserInfoDto(long id, String nickname, String email, String mobile) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.mobile = mobile;
    }

    public static NaverUserInfoDto of(long id, String nickname, String email, String mobile) {
        return new NaverUserInfoDto(id, nickname, email, mobile);
    }
}
