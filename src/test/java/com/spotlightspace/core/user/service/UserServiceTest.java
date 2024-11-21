package com.spotlightspace.core.user.service;

import static com.spotlightspace.common.exception.ErrorCode.USER_NOT_FOUND;
import static com.spotlightspace.core.data.UserCouponData.getCouponResponse;
import static com.spotlightspace.core.data.UserTestData.testAuthUser;
import static com.spotlightspace.core.data.UserTestData.testUpdateUserRequestDto;
import static com.spotlightspace.core.data.UserTestData.testUser;
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
import com.spotlightspace.core.usercoupon.service.UserCouponService;
import java.io.IOException;
import java.util.List;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttachmentService attachmentService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserCouponService userCouponService;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("회원 수정 테스트")
    class UpdateUserTest {

//        @Test
//        @DisplayName("회원 수정 성공 테스트")
//        public void updateUser_defaultInfo_success() throws IOException {
//            //given
//            UpdateUserRequestDto updateRequestDto = testUpdateUserRequestDto();
//            long userId = 1;
//
//            User user = testUser();
//            AuthUser authUser = testAuthUser();
//
//            given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);
//
//            MultipartFile testFile = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
//
//            BDDMockito.doNothing().when(attachmentService).addAttachment(any(MultipartFile.class), anyLong(), any(
//                    TableRole.class));
//
//            //when - then
//            assertDoesNotThrow(() -> userService.updateUser(userId, authUser, updateRequestDto, testFile));
//        }

        @Test
        @DisplayName("회원 수정 성공 테스트 - 파일이 없을경우")
        public void updateUser_defaultInfo_success_nofile() throws IOException {
            //given
            UpdateUserRequestDto updateRequestDto = testUpdateUserRequestDto();
            Long userId = 1L;

            User user = testUser();
            AuthUser authUser = testAuthUser();
            ReflectionTestUtils.setField(user,"id",userId);

            given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);

            MultipartFile testFile = null;

            //when - then
            assertDoesNotThrow(() -> userService.updateUser(userId, authUser, updateRequestDto, testFile));
        }


        @Test
        @DisplayName("회원 수정 실패 테스트 - 유저가 삭제된 경우")
        public void updateUser_deletedUser_failure() {
            //given
            long userId = 1;
            UpdateUserRequestDto updateRequestDto = testUpdateUserRequestDto();

            AuthUser authUser = testAuthUser();

            given(userRepository.findByIdOrElseThrow(anyLong()))
                    .willThrow(new ApplicationException(USER_NOT_FOUND));

            //when - then
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

            //when - then
            assertThrows(ApplicationException.class,
                    () -> userService.updateUser(anotherUserId, authUser, updateRequestDto, null));
        }

        @Nested
        @DisplayName("회원 조회 테스트")
        class GetUserTest {

            @Test
            @DisplayName("회원 조회 성공 테스트")
            public void findUser_success() {
                // given
                User user = testUser();
                ReflectionTestUtils.setField(user, "id", 1L);

                given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);

                // when - then
                assertDoesNotThrow(() -> userService.getUser(user.getId(), testAuthUser().getUserId()));
            }

            @Test
            @DisplayName("회원 조회 실패 - 회원이 삭제된 경우")
            public void getUser_notFoundUser_failure() {
                User user = testUser();
                ReflectionTestUtils.setField(user, "id", 1L);
                ReflectionTestUtils.setField(user, "isDeleted", true);

                given(userRepository.findByIdOrElseThrow(anyLong()))
                        .willThrow(new ApplicationException(USER_NOT_FOUND));

                //when - then
                assertThrows(ApplicationException.class,
                        () -> userService.getUser(user.getId(), testAuthUser().getUserId()));
            }

            @Test
            @DisplayName("다른 회원을 조회시 에러")
            public void getUser_notSameUserId_failure() {
                // given
                User user = testUser();
                ReflectionTestUtils.setField(user, "id", 1L);

                given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);

                // when - then
                assertThrows(ApplicationException.class, () -> userService.getUser(2L, testAuthUser().getUserId()));
            }
        }

//        @Nested
//        @DisplayName("유저 삭제 테스트")
//        class DeleteUserTest {
//
//            @Test
//            @DisplayName("유저 삭제 성공")
//            public void deleteUser_success() {
//                // given
//                long userId = 1L;
//                User user = testUser();
//                ReflectionTestUtils.setField(user, "id", userId);
//
//                AuthUser authUser = testAuthUser();
//
//                given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);
//
//                // when - then
//                assertDoesNotThrow(() -> userService.deleteUser(userId, authUser.getUserId()));
//            }
//
//            @Test
//            @DisplayName("유저 삭제 실패 - 회원이 이미 삭제된 경우")
//            public void deleteUser_notFoundUser_failure() {
//                long userId = 1L;
//                User user = testUser();
//                ReflectionTestUtils.setField(user, "isDeleted", true);
//
//                AuthUser authUser = testAuthUser();
//
//                given(userRepository.findByIdOrElseThrow(anyLong()))
//                        .willThrow(new ApplicationException(USER_NOT_FOUND));
//
//                // when - then
//                assertThrows(ApplicationException.class, () -> userService.deleteUser(userId, authUser.getUserId()));
//            }
//
//            @Test
//            @DisplayName("다른 회원을 삭제시 에러")
//            public void deleteUser_notSameUserId_failure() {
//                // given
//                long userId = 1L;
//                User user = testUser();
//                ReflectionTestUtils.setField(user, "id", userId);
//
//                AuthUser authUser = testAuthUser();
//
//                given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);
//
//                // when - then
//                assertThrows(ApplicationException.class, () -> userService.deleteUser(2L, authUser.getUserId()));
//            }
//        }

        @Nested
        @DisplayName("쿠폰조회테스트")
        class getCoupons {

            @Test
            @DisplayName("쿠폰 조회 성공")
            public void getCoupons_success() {
                // given
                User user = testUser();
                ReflectionTestUtils.setField(user, "id", 1L);
                AuthUser authUser = testAuthUser();

                given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);
                given(userCouponService.getUserCouponByUserId(user.getId()))
                        .willReturn(List.of(getCouponResponse()));

                // when - then
                assertDoesNotThrow(() -> userService.getCoupons(user.getId(), authUser.getUserId()));
            }


            @Test
            @DisplayName("쿠폰 조회 실패 - 회원이 삭제된 경우")
            public void getCoupons_notFoundUser_failure() {
                //given
                User user = testUser();
                ReflectionTestUtils.setField(user, "id", 1L);
                ReflectionTestUtils.setField(user, "isDeleted", true);

                AuthUser authUser = testAuthUser();

                given(userRepository.findByIdOrElseThrow(anyLong()))
                        .willThrow(new ApplicationException(USER_NOT_FOUND));

                // when - then
                assertThrows(ApplicationException.class,
                        () -> userService.getCoupons(user.getId(), authUser.getUserId()));
            }

            @Test
            @DisplayName("다른 회원을 조회시 에러")
            public void getCoupons_notSameUserId_failure() {
                // given
                User user = testUser();
                ReflectionTestUtils.setField(user, "id", 1L);

                AuthUser authUser = testAuthUser();

                given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);

                // when - then
                assertThrows(ApplicationException.class, () -> userService.getCoupons(2L, authUser.getUserId()));
            }
        }
    }
}
