package com.spotlightspace.core.data;

import static com.spotlightspace.core.user.domain.UserRole.ROLE_USER;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.auth.dto.SigninUserRequestDto;
import com.spotlightspace.core.auth.dto.SignupUserRequestDto;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.dto.request.UpdateUserRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.test.util.ReflectionTestUtils;

@RequiredArgsConstructor
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

    public static UpdateUserRequestDto testUpdateUserRequestDto() {
        return new UpdateUserRequestDto("email@test.com", "newPassword", "2024-10-21", "newPhone");
    }

    //authUser
    public static AuthUser testAuthUser() {
        return new AuthUser(1L, "email@test.com", ROLE_USER);
    }

    public static User testUser_deleted() {
        User user = testUser();
        ReflectionTestUtils.setField(user, "isDeleted", true);
        return user;
    }
}
