package com.spotlightspace.core.user.service;

import static com.spotlightspace.common.constant.JwtConstant.TOKEN_ACCESS_TIME;
import static com.spotlightspace.common.exception.ErrorCode.FORBIDDEN_USER;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    public void updateUser(Long userId, AuthUser authUser, UpdateUserRequestDto updateUserRequestDto,
            MultipartFile file)
            throws IOException {
        User user = userRepository.findByIdOrElseThrow(userId);

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

        if (!userId.equals(currentUserId)) {
            throw new ApplicationException(FORBIDDEN_USER);
        }

        String url = attachmentService.getImageUrl(userId, TableRole.USER);
        return GetUserResponseDto.from(user, url);
    }

    public void deleteUser(Long userId, Long currentUserId) {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (!userId.equals(currentUserId)) {
            throw new ApplicationException(FORBIDDEN_USER);
        }

        user.delete();
    }

    public List<GetCouponResponseDto> getCoupons(Long userId, Long currentUserId) {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (!userId.equals(currentUserId)) {
            throw new ApplicationException(FORBIDDEN_USER);
        }

        return userCouponService.getUserCouponByUserId(userId);
    }

    //재발급 방지를 위해 redis의 리프레시 토큰 삭제
    // 액세스 토큰 레디스에올리기
    // 쿠키 무효화
    public void logout(Long userId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        //쿠키에 있는 리프레시토큰 무효화
        Cookie refreshTokenCookie = new Cookie("RefreshToken", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        httpServletResponse.addCookie(refreshTokenCookie);

        String accessToken = httpServletRequest.getHeader(AUTHORIZATION);
        String key = "user:blacklist:id:" + userId;
        //redis에 해당하는 리프레시 토큰 삭제
        redisTemplate.delete("user:refresh:id:" + userId);

        //액세스 토큰 레디스에 블랙리스트로 올리기
        redisTemplate.opsForValue()
                .set(key, accessToken, TOKEN_ACCESS_TIME, TimeUnit.MILLISECONDS);
    }

    @Transactional(readOnly = true)
    public void findUserEmail(String email) {
        userRepository.findByEmailOrElseThrow(email);
    }
}
