package com.spotlightspace.core.admin.dto.responsedto;


import com.spotlightspace.core.user.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminUserResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String phoneNumber;
    private String role;
    private Boolean isDeleted;

    // User 엔티티에서 데이터를 가져오는 정적 팩토리 메서드
    public static AdminUserResponseDto from(User user) {
        return new AdminUserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.isDeleted()
        );
    }

    // Tuple 데이터를 활용하는 정적 팩토리 메서드
    public static AdminUserResponseDto of(Long id, String email, String nickname, String phoneNumber, String role, Boolean isDeleted) {
        return new AdminUserResponseDto(id, email, nickname, phoneNumber, role, isDeleted);
    }
}
