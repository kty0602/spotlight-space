package com.spotlightspace.core.auth.service;

import static com.spotlightspace.common.exception.ErrorCode.USER_NOT_FOUND;
import static com.spotlightspace.core.data.UserTestData.testSigninUserRequestDto;
import static com.spotlightspace.core.data.UserTestData.testSignupUserRequestDto;
import static com.spotlightspace.core.data.UserTestData.testUser;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.config.JwtUtil;
import com.spotlightspace.core.attachment.service.AttachmentService;
import com.spotlightspace.core.auth.dto.request.SignInUserRequestDto;
import com.spotlightspace.core.auth.dto.request.SignUpUserRequestDto;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.dto.request.UpdatePasswordUserRequestDto;
import com.spotlightspace.core.user.repository.UserRepository;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(SpringExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AttachmentService attachmentService;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("회원가입 테스트")
    class SignUpTest {

//        @Test
//        @DisplayName("회원가입 성공 테스트")
//        void signUp_success() throws IOException {
//            // given
//            SignUpUserRequestDto signupUserRequestDto = testSignupUserRequestDto();
//
//            String password = passwordEncoder.encode(signupUserRequestDto.getPassword());
//            User user = testUser();
//
//            given(userRepository.existsByEmail(anyString())).willReturn(false);
//            given(passwordEncoder.encode(anyString())).willReturn(password);
//            given(userRepository.save(any(User.class))).willReturn(user);
//
//            MultipartFile testFile = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
//
//            BDDMockito.doNothing().when(attachmentService).addAttachment(any(MultipartFile.class), anyLong(), any(
//                    TableRole.class));
//
//            // when - then
//            assertDoesNotThrow(() -> authService.signUp(signupUserRequestDto, testFile));
//        }

//        @Test
//        @DisplayName("파일이 없을경우")
//        void signUp_noFile_success() throws IOException {
//            // given
//            SignUpUserRequestDto signupUserRequestDto = testSignupUserRequestDto();
//
//            String password = passwordEncoder.encode(signupUserRequestDto.getPassword());
//            User user = testUser();
//
//            given(userRepository.existsByEmail(anyString())).willReturn(false);
//            given(passwordEncoder.encode(anyString())).willReturn(password);
//            given(userRepository.save(any(User.class))).willReturn(user);
//
//            MultipartFile testFile = null;
//
//            // when - then
//            assertDoesNotThrow(() -> authService.signUp(signupUserRequestDto, testFile));
//        }

        @Test
        @DisplayName("중복 이메일로 회원가입 실패")
        void signUp_duplicateEmail_failure() throws IOException {
            // given
            SignUpUserRequestDto signupUserRequestDto = testSignupUserRequestDto();

            String password = passwordEncoder.encode(signupUserRequestDto.getPassword());
            User user = testUser();

            given(userRepository.existsByEmail(anyString())).willReturn(true);
            MultipartFile testFile = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());

            BDDMockito.doNothing().when(attachmentService).addAttachment(any(MultipartFile.class), anyLong(), any(
                    TableRole.class));

            // when - then
            assertThrows(ApplicationException.class, () -> authService.signUp(signupUserRequestDto, testFile));
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class SignInTest {
//        @Test
//        @DisplayName("로그인 성공 테스트")
//        void signIn_success() {
//            // given
//            SigninUserRequestDto signinUserRequestDto = testSigninUserRequestDto();
//            User user = testUser();
//
//            String expectedToken = "token";
//
//            given(userRepository.findByEmailOrElseThrow(anyString())).willReturn(user);
//            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
//            given(jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole())).willReturn(expectedToken);
//
//            // when
//            String actualToken = authService.signIn(signinUserRequestDto);
//
//            // then
//            assertDoesNotThrow(() -> authService.signIn(signinUserRequestDto));
//            assertEquals(expectedToken, actualToken);
//        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 실패")
        void signIn_notFoundEmail_failure() {
            // given
            SignInUserRequestDto signinUserRequestDto = testSigninUserRequestDto();

            given(userRepository.findByEmailOrElseThrow(anyString())).willThrow(
                    new ApplicationException(USER_NOT_FOUND));

            // when - then
            assertThrows(ApplicationException.class, () -> authService.signIn(signinUserRequestDto));
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않아 로그인 실패")
        void signIn_wrongPassword_failure() {
            // given
            SignInUserRequestDto signinUserRequestDto = testSigninUserRequestDto();
            User user = testUser();

            given(userRepository.findByEmailOrElseThrow(anyString())).willReturn(user);
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            // when - then
            assertThrows(ApplicationException.class, () -> authService.signIn(signinUserRequestDto));
        }
    }

    @Nested
    @DisplayName("유저 비밀번호 테스트")
    class ChangePasswordTest {

        @Test
        @DisplayName("비밀번호 변경 성공")
        void changePassword_success() {
            // given
            String newPassword = "newPassword";

            User user = testUser();
            ReflectionTestUtils.setField(user, "password", newPassword);

            UpdatePasswordUserRequestDto updateRequestDto = new UpdatePasswordUserRequestDto(user.getEmail(),
                    newPassword);

            given(userRepository.findByEmailOrElseThrow(anyString())).willReturn(user);
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            // when - then
            assertDoesNotThrow(() -> authService.updatePassword(updateRequestDto));
        }
    }
}
