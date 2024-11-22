package com.spotlightspace.core.auth.dto.response;

import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.domain.UserRole;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignUpUserResponseDto {

    private long id;
    private String email;
    private String nickname;
    private LocalDate birth;
    private String phoneNumber;
    private UserRole role;
    private String location;
    private boolean isDeleted;
    private boolean isSocialLogin;

    public static SignUpUserResponseDto from(User user) {
        return new SignUpUserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getBirth(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getLocation(),
                user.isDeleted(),
                user.isSocialLogin()
        );
    }
}
