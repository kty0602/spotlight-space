package com.spotlightspace.core.data;

import com.spotlightspace.core.auth.dto.SigninUserRequestDto;
import com.spotlightspace.core.auth.dto.SignupUserRequestDto;
import com.spotlightspace.core.user.domain.User;

public class UserTestData {

    public static SignupUserRequestDto testSignupUserRequestDto() {
        return new SignupUserRequestDto(
                "email@test.com",
                "Password1!",
                "test",
                "ROLE_USER",
                "2024-10-24",
                "010-1010-1010");
    }

    public static User testUser() {
        SignupUserRequestDto userRequestDto = testSignupUserRequestDto();
        return User.of("password", userRequestDto);
    }

    public static SigninUserRequestDto testSigninUserRequestDto() {
        return new SigninUserRequestDto("email@test.com", "Password1!");
    }
}
