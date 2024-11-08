package com.spotlightspace.core.admin.dto.requestdto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchAdminUserRequestDto {

    private String nickname;
    private String email;
    private String phoneNumber;
    private String role;
    private String location;
    private String birth;
    private boolean isSocialLogin;
    private boolean isDeleted;

    public static SearchAdminUserRequestDto of(
            String nickname,
            String email,
            String phoneNumber,
            String role,
            String location,
            String birth,
            boolean isSocialLogin,
            boolean isDeleted
    ) {
        return new SearchAdminUserRequestDto(nickname, email, phoneNumber, role, location, birth, isSocialLogin, isDeleted);
    }

}
