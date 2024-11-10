package com.spotlightspace.core.user.domain;

import com.spotlightspace.common.entity.Timestamped;
import com.spotlightspace.core.auth.dto.request.SignUpUserRequestDto;
import com.spotlightspace.core.user.dto.request.UpdateUserRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Entity
@Getter
@Service
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_nickname_phone", columnList = "phone_number, nickname"),
                @Index(name = "idx_nickname_birth", columnList = "nickname, birth"),
                @Index(name = "idx_location", columnList = "location, nickname")
        }
)

public class User extends Timestamped {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotNull
    private String email;

    @NotNull
    private String nickname;

    @NotNull
    private LocalDate birth;

    @Column(unique = true)
    @NotNull
    private String phoneNumber;

    @Column
    @NotNull
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @NotNull
    private String location;

    private boolean isDeleted = false;
    private boolean isSocialLogin;

    private User(
            String email,
            String nickname,
            String password,
            UserRole role,
            LocalDate birth,
            String phoneNumber,
            boolean isSocialLogin,
            String location
    ) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.role = role;
        this.birth = birth;
        this.phoneNumber = phoneNumber;
        this.isSocialLogin = isSocialLogin;
        this.location = location;
    }

    public static User of(String encryptPassword, SignUpUserRequestDto signupUserRequestDto) {
        return new User(
                signupUserRequestDto.getEmail(),
                signupUserRequestDto.getNickname(),
                encryptPassword,
                UserRole.from(signupUserRequestDto.getRole()),
                LocalDate.parse(signupUserRequestDto.getBirth(), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                signupUserRequestDto.getPhoneNumber(),
                signupUserRequestDto.isSocialLogin(),
                signupUserRequestDto.getLocation()
        );
    }

    public void update(String encryptPassword, UpdateUserRequestDto updateUserRequestDto) {
        this.nickname = updateUserRequestDto.getNickname();
        this.birth = LocalDate.parse(updateUserRequestDto.getBirth(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.phoneNumber = updateUserRequestDto.getPhoneNumber();
        this.password = encryptPassword;
        this.location = updateUserRequestDto.getLocation();
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void updatePassword(String encryptPassword) {
        this.password = encryptPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public void updateRole(UserRole role) {
        this.role = role;
    }

}

