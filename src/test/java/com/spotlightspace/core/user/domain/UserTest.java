package com.spotlightspace.core.user.domain;

import static org.assertj.core.api.Assertions.*;

import com.spotlightspace.core.auth.dto.request.SignUpUserRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class UserTest {

    @Test
    @DisplayName("유저 생성 시 초기 상태는 삭제되지 않은 상태이다.")
    void createUser() {
        // given
        SignUpUserRequestDto requestDto = new SignUpUserRequestDto(
                "test142@email.com",
                "rawPassword",
                "nickname",
                "role_user",
                "1998-12-12",
                false,
                "010-1234-1234",
                "한국"
        );

        // when
        User user = User.create("encryptPassword", requestDto);

        // then
        assertThat(user.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("유저 삭제 시 상태가 삭제된 상태로 변경된다.")
    void deleteUser() {
        SignUpUserRequestDto requestDto = new SignUpUserRequestDto(
                "test142@email.com",
                "rawPassword",
                "nickname",
                "role_user",
                "1998-12-12",
                false,
                "010-1234-1234",
                "한국"
        );
        User user = User.create("encryptPassword", requestDto);

        // when
        user.delete();

        // then
        assertThat(user.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("유저의 비밀번호를 변경할 수 있다.")
    void updateUserPassword() {
        SignUpUserRequestDto requestDto = new SignUpUserRequestDto(
                "test142@email.com",
                "rawPassword",
                "nickname",
                "role_user",
                "1998-12-12",
                false,
                "010-1234-1234",
                "한국"
        );
        String newPassword = "newPassword";
        User user = User.create("encryptPassword", requestDto);

        // when
        user.updatePassword(newPassword);

        // then
        assertThat(user.getPassword()).isNotEqualTo(requestDto.getPassword());
        assertThat(user.getPassword()).isEqualTo(newPassword);
    }

    @Test
    @DisplayName("유저의 역할을 변경할 수 있다.")
    void updateRole() {
        SignUpUserRequestDto requestDto = new SignUpUserRequestDto(
                "test142@email.com",
                "rawPassword",
                "nickname",
                "role_user",
                "1998-12-12",
                false,
                "010-1234-1234",
                "한국"
        );
        User user = User.create("encryptPassword", requestDto);

        UserRole newRole = UserRole.ROLE_ARTIST;

        // when
        user.updateRole(newRole);

        // then
        assertThat(user.getRole()).isNotEqualTo(requestDto.getRole());
        assertThat(user.getRole()).isEqualTo(newRole);
    }

    @Nested
    @DisplayName("유저 비교 시")
    class CompareUser {

        @Test
        @DisplayName("유저 ID가 같다면 같은 유저로 판단한다.")
        void equalById() {
            // given
            SignUpUserRequestDto requestDto = new SignUpUserRequestDto(
                    "test142@email.com",
                    "rawPassword",
                    "nickname",
                    "role_user",
                    "1998-12-12",
                    false,
                    "010-1234-1234",
                    "한국"
            );
            User user1 = User.create("encryptPassword", requestDto);
            ReflectionTestUtils.setField(user1, "id", 1L);
            User user2 = User.create("encryptPassword", requestDto);
            ReflectionTestUtils.setField(user2, "id", 1L);

            // when & then
            assertThat(user1.equals(user2)).isTrue();
        }

        @Test
        @DisplayName("유저 ID가 다르다면 다른 유저로 판단한다.")
        void notEqual() {
            // given
            SignUpUserRequestDto requestDto = new SignUpUserRequestDto(
                    "test142@email.com",
                    "rawPassword",
                    "nickname",
                    "role_user",
                    "1998-12-12",
                    false,
                    "010-1234-1234",
                    "한국"
            );
            User user1 = User.create("encryptPassword", requestDto);
            ReflectionTestUtils.setField(user1, "id", 1L);
            User user2 = User.create("encryptPassword", requestDto);
            ReflectionTestUtils.setField(user2, "id", 2L);

            // when & then
            assertThat(user1.equals(user2)).isFalse();
        }

        @Test
        @DisplayName("같은 유저 객체이면 같다고 판단한다.")
        void equalByObject() {
            // given
            SignUpUserRequestDto requestDto = new SignUpUserRequestDto(
                    "test142@email.com",
                    "rawPassword",
                    "nickname",
                    "role_user",
                    "1998-12-12",
                    false,
                    "010-1234-1234",
                    "한국"
            );
            User user1 = User.create("encryptPassword", requestDto);
            ReflectionTestUtils.setField(user1, "id", 1L);

            // when & then
            assertThat(user1.equals(user1)).isTrue();
        }

        @Test
        @DisplayName("Null 객체이면 다르다고 판단한다.")
        void equalByNull() {
            // given
            SignUpUserRequestDto requestDto = new SignUpUserRequestDto(
                    "test142@email.com",
                    "rawPassword",
                    "nickname",
                    "role_user",
                    "1998-12-12",
                    false,
                    "010-1234-1234",
                    "한국"
            );
            User user1 = User.create("encryptPassword", requestDto);
            ReflectionTestUtils.setField(user1, "id", 1L);

            // when & then
            assertThat(user1.equals(null)).isFalse();
        }

        @Test
        @DisplayName("다른 클래스의 인스턴스이면 다르다고 판단한다.")
        void equalByOtherClassInstance() {
            // given
            SignUpUserRequestDto requestDto = new SignUpUserRequestDto(
                    "test142@email.com",
                    "rawPassword",
                    "nickname",
                    "role_user",
                    "1998-12-12",
                    false,
                    "010-1234-1234",
                    "한국"
            );
            User user1 = User.create("encryptPassword", requestDto);
            ReflectionTestUtils.setField(user1, "id", 1L);

            // when & then
            assertThat(user1.equals("123")).isFalse();
        }
    }
}
