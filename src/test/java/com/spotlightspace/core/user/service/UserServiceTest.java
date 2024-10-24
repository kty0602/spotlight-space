package com.spotlightspace.core.user.service;

import static com.spotlightspace.core.data.UserTestData.testAuthUser;
import static com.spotlightspace.core.data.UserTestData.testUpdateUserRequestDto;
import static com.spotlightspace.core.data.UserTestData.testUser;
import static com.spotlightspace.core.data.UserTestData.testUser_deleted;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.attachment.service.AttachmentService;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.dto.request.UpdateUserRequestDto;
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
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(SpringExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttachmentService attachmentService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("회원 수정 테스트")
    class UpdateUserTest {

        @Test
        @DisplayName("회원 수정 성공 테스트")
        public void updateUser_defaultInfo_success() throws IOException {
            //given
            UpdateUserRequestDto updateRequestDto = testUpdateUserRequestDto();
            long userId = 1;

            User user = testUser();
            AuthUser authUser = testAuthUser();

            given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);

            MultipartFile testFile = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());

            BDDMockito.doNothing().when(attachmentService).addAttachment(any(MultipartFile.class), anyLong(), any(
                    TableRole.class));

            //when-then
            assertDoesNotThrow(() -> userService.updateUser(userId, authUser, updateRequestDto, testFile));
        }

        @Test
        @DisplayName("회원 수정 성공 테스트 - 파일이 없을경우")
        public void updateUser_defaultInfo_success_nofile() throws IOException {
            //given
            UpdateUserRequestDto updateRequestDto = testUpdateUserRequestDto();
            long userId = 1;

            User user = testUser();
            AuthUser authUser = testAuthUser();

            given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);

            MultipartFile testFile = null;

            //when-then
            assertDoesNotThrow(() -> userService.updateUser(userId, authUser, updateRequestDto, testFile));
        }


        @Test
        @DisplayName("회원 수정 실패 테스트 - 유저가 삭제된 경우")
        public void updateUser_deletedUser_failure() {
            //given
            long userId = 1;
            UpdateUserRequestDto updateRequestDto = testUpdateUserRequestDto();

            User user = testUser_deleted();
            AuthUser authUser = testAuthUser();

            given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);
            //when-then
            assertThrows(ApplicationException.class,
                    () -> userService.updateUser(userId, authUser, updateRequestDto, null));
        }

        @Test
        @DisplayName("수정할 user id와 로그인된 user id가 다를 경우")
        public void updateUser_notSameUserId_failure() {
            //given
            long anotherUserId = 2;
            UpdateUserRequestDto updateRequestDto = testUpdateUserRequestDto();

            AuthUser authUser = testAuthUser();

            given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(testUser());

            //when-then
            assertThrows(ApplicationException.class,
                    () -> userService.updateUser(anotherUserId, authUser, updateRequestDto, null));
        }
    }
}
