package com.spotlightspace.core.user.dto.response;

import com.spotlightspace.core.user.domain.User;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetUserResponseDto {

    private long id;
    private String email;
    private String nickname;
    private LocalDate birth;
    private String phoneNumber;
    private String profileImage;

    public static GetUserResponseDto of(User user, String url) {
        return new GetUserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getBirth(),
                user.getPhoneNumber(),
                url
        );
    }
}
