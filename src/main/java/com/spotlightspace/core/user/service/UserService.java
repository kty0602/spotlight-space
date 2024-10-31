package com.spotlightspace.core.user.service;

import static com.spotlightspace.common.constant.JwtConstant.TOKEN_ACCESS_TIME;
import static com.spotlightspace.common.exception.ErrorCode.FORBIDDEN_USER;
import static com.spotlightspace.common.exception.ErrorCode.SOCIAL_LOGIN_UPDATE_NOT_ALLOWED;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.attachment.service.AttachmentService;
import com.spotlightspace.core.ticket.repository.TicketRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.dto.request.UpdateUserRequestDto;
import com.spotlightspace.core.user.dto.response.GetCalculateListResponseDto;
import com.spotlightspace.core.user.dto.response.GetCalculateResponseDto;
import com.spotlightspace.core.user.dto.response.GetCouponResponseDto;
import com.spotlightspace.core.user.dto.response.GetUserResponseDto;
import com.spotlightspace.core.user.repository.UserRepository;
import com.spotlightspace.core.usercoupon.service.UserCouponService;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, String> redisTemplate;
    private final TicketRepository ticketRepository;

    public void updateUser(Long userId, AuthUser authUser, UpdateUserRequestDto updateUserRequestDto,
            MultipartFile file)
            throws IOException {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (!userId.equals(authUser.getUserId())) {
            throw new ApplicationException(FORBIDDEN_USER);
        }

        //소셜로그인시, 유저 업데이트 불가능
        if (user.isSocialLogin()) {
            throw new ApplicationException(SOCIAL_LOGIN_UPDATE_NOT_ALLOWED);
        }

        if (file != null) {
            Long attachmentId = attachmentService.getAttachmentList(user.getId(), TableRole.USER).get(0).getId();
            attachmentService.updateAttachment(attachmentId, file, userId, TableRole.USER, authUser);
        }

        String encryptPassword = passwordEncoder.encode(updateUserRequestDto.getPassword());

        user.update(encryptPassword, updateUserRequestDto);
    }

    @Transactional(readOnly = true)
    public GetUserResponseDto getUser(Long userId, Long currentUserId) {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (!userId.equals(currentUserId)) {
            throw new ApplicationException(FORBIDDEN_USER);
        }

        String url = attachmentService.getImageUrl(userId, TableRole.USER);
        return GetUserResponseDto.from(user, url);
    }

    public void deleteUser(Long userId, Long currentUserId, String accessToken) {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (!userId.equals(currentUserId)) {
            throw new ApplicationException(FORBIDDEN_USER);
        }

        addBlackList(userId, accessToken);
        user.delete();
    }

    @Transactional(readOnly = true)
    public List<GetCouponResponseDto> getCoupons(Long userId, Long currentUserId) {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (!userId.equals(currentUserId)) {
            throw new ApplicationException(FORBIDDEN_USER);
        }

        return userCouponService.getUserCouponByUserId(userId);
    }

    @Transactional(readOnly = true)
    public GetCalculateResponseDto getAllCalculate(Long userId, Long currentUserId) {
        userRepository.findByIdOrElseThrow(userId);

        if (!userId.equals(currentUserId)) {
            throw new ApplicationException(FORBIDDEN_USER);
        }

        int totalAmount = ticketRepository.findTotalAmountByUserId(userId);
        return GetCalculateResponseDto.from(totalAmount);
    }

    public List<GetCalculateListResponseDto> getCalculateList(Long userId, Long currentUserId) {
        userRepository.findByIdOrElseThrow(userId);

        if (!userId.equals(currentUserId)) {
            throw new ApplicationException(FORBIDDEN_USER);
        }

        return ticketRepository.findTotalAmountGroupedByEvent(userId);
    }


    //재발급 방지를 위해 redis의 리프레시 토큰 삭제
    // 액세스 토큰 레디스에올리기
    // 쿠키 무효화
    public void logout(Long userId, String accessToken) {
        addBlackList(userId, accessToken);
    }

    @Transactional(readOnly = true)
    public User findUserEmail(String email) {
        return userRepository.findByEmailOrElseThrow(email);
    }

    public void addBlackList(Long userId, String accessToken) {
        String key = "user:blacklist:id:" + userId;
        //redis에 해당하는 리프레시 토큰 삭제
        redisTemplate.delete("user:refresh:id:" + userId);

        //액세스 토큰 레디스에 블랙리스트로 올리기
        redisTemplate.opsForValue()
                .set(key, accessToken, TOKEN_ACCESS_TIME, TimeUnit.MILLISECONDS);
    }

}
