package com.spotlightspace.core.user.service;

import static com.spotlightspace.common.exception.ErrorCode.FORBIDDEN_USER;
import static com.spotlightspace.common.exception.ErrorCode.USER_NOT_FOUND;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.attachment.service.AttachmentService;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.dto.request.UpdateUserRequestDto;
import com.spotlightspace.core.user.dto.response.GetCouponResponseDto;
import com.spotlightspace.core.user.dto.response.GetUserResponseDto;
import com.spotlightspace.core.user.repository.UserRepository;
import com.spotlightspace.core.usercoupon.service.UserCouponService;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AttachmentService attachmentService;
    private final PasswordEncoder passwordEncoder;
    private final UserCouponService userCouponService;

    public void updateUser(Long userId, AuthUser authUser, UpdateUserRequestDto updateUserRequestDto,
            MultipartFile file)
            throws IOException {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (user.isDeleted()) {
            throw new ApplicationException(USER_NOT_FOUND);
        }

        if (!userId.equals(authUser.getUserId())) {
            throw new ApplicationException(FORBIDDEN_USER);
        }

        if (file != null) {
            //todo : 이미지 삭제 로직 구현 및 데이터베이스 수정 로직 구현
            attachmentService.addAttachment(file, userId, TableRole.USER);
        }

        String encryptPassword = passwordEncoder.encode(updateUserRequestDto.getPassword());

        user.update(encryptPassword, updateUserRequestDto);
    }

    @Transactional(readOnly = true)
    public GetUserResponseDto getUser(Long userId, Long currentUserId) {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (user.isDeleted()) {
            throw new ApplicationException(USER_NOT_FOUND);
        }

        if (!userId.equals(currentUserId)) {
            throw new ApplicationException(FORBIDDEN_USER);
        }

        String url = attachmentService.getImageUrl(userId, TableRole.USER);
        return GetUserResponseDto.from(user, url);
    }

    public void deleteUser(Long userId, Long currentUserId) {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (user.isDeleted()) {
            throw new ApplicationException(USER_NOT_FOUND);
        }

        if (!userId.equals(currentUserId)) {
            throw new ApplicationException(FORBIDDEN_USER);
        }

        user.delete();
    }

    public List<GetCouponResponseDto> getCoupons(Long userId, Long currentUserId) {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (user.isDeleted()) {
            throw new ApplicationException(USER_NOT_FOUND);
        }

        if (!userId.equals(currentUserId)) {
            throw new ApplicationException(FORBIDDEN_USER);
        }

        return userCouponService.getUserCouponByUserId(userId);
    }
}
