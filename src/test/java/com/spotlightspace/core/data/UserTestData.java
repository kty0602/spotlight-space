package com.spotlightspace.core.data;

import static com.spotlightspace.core.user.domain.UserRole.ROLE_ARTIST;
import static com.spotlightspace.core.user.domain.UserRole.ROLE_USER;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.auth.dto.request.SignInUserRequestDto;
import com.spotlightspace.core.auth.dto.request.SignUpUserRequestDto;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.dto.request.UpdateUserRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.test.util.ReflectionTestUtils;

@RequiredArgsConstructor
public class UserTestData {

    public static SignUpUserRequestDto testSignupUserRequestDto() {
        return new SignUpUserRequestDto(
                "email@test.com",
                "Password1!",
                "test",
                "ROLE_USER",
                "2024-10-24",
                false,
                "010-1010-1010",
                "한국");
    }

    public static SignUpUserRequestDto testSignupArtistRequestDto() {
        return new SignUpUserRequestDto(
                "email@test.com",
                "Password1!",
                "test",
                "ROLE_ARTIST",
                "2024-10-24",
                false,
                "010-1010-1010",
                "한국");
    }

    public static User testUser() {
        SignUpUserRequestDto userRequestDto = testSignupUserRequestDto();
        return User.of("password", userRequestDto);
    }

    public static User testArtist() {
        SignUpUserRequestDto userRequestDto = testSignupArtistRequestDto();
        return User.of("password", userRequestDto);
    }

    public static SignInUserRequestDto testSigninUserRequestDto() {
        return new SignInUserRequestDto("email@test.com", "Password1!");
    }

    public static UpdateUserRequestDto testUpdateUserRequestDto() {
        return new UpdateUserRequestDto("email@test.com", "newPassword", "2024-10-21", "newPhone", null);
    }

    //authUser
    public static AuthUser testAuthUser() {
        return new AuthUser(1L, "email@test.com", ROLE_USER);
    }

    public static AuthUser testArtistAuthUser() {return new AuthUser(1L, "email@test.com", ROLE_ARTIST); }

    public static AuthUser testAnotherArtistAuthUser() {return new AuthUser(2L, "email2@test.com", ROLE_ARTIST); }

    public static User testUser_deleted() {
        User user = testUser();
        ReflectionTestUtils.setField(user, "isDeleted", true);
        return user;
    }
}
