package com.spotlightspace.core.admin.dto.requestdto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminLoginRequestDto {

    @NotNull
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String certified;

    public static AdminLoginRequestDto of(String email, String password, String certified) {
        return new AdminLoginRequestDto(email, password, certified);
    }

}
