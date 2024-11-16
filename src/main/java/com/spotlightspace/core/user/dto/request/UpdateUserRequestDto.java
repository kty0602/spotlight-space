package com.spotlightspace.core.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestDto {

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "비밀번호는 대소문자, 숫자, 특수문자를 포함해 최소 8자, 최대 20자 입력해주세요.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String nickname;

    @NotBlank(message = "생년월일을 입력해주세요")
    private String birth;

    @Pattern(
            regexp = "^010-\\d{4}-\\d{4}$",
            message = "휴대폰 번호는 010-0000-0000 형식이어야 합니다."
    )
    private String phoneNumber;

    private String location;
}
