package com.spotlightspace.core.auth.service;

import static com.spotlightspace.common.constant.JwtConstant.USER_EMAIL;
import static com.spotlightspace.common.constant.JwtConstant.USER_ROLE;
import static com.spotlightspace.common.exception.ErrorCode.EMAIL_DUPLICATED;
import static com.spotlightspace.common.exception.ErrorCode.INVALID_PASSWORD_OR_EMAIL;
import static com.spotlightspace.common.exception.ErrorCode.REFRESH_TOKEN_NOT_FOUND;
import static com.spotlightspace.common.exception.ErrorCode.USER_NOT_FOUND;

import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.config.JwtUtil;
import com.spotlightspace.core.attachment.service.AttachmentService;
import com.spotlightspace.core.auth.dto.request.SignInUserRequestDto;
import com.spotlightspace.core.auth.dto.request.SignUpUserRequestDto;
import com.spotlightspace.core.auth.dto.response.SaveTokenResponseDto;
import com.spotlightspace.core.point.repository.PointRepository;
import com.spotlightspace.core.point.service.PointService;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.domain.UserRole;
import com.spotlightspace.core.user.dto.request.UpdatePasswordUserRequestDto;
import com.spotlightspace.core.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AttachmentService attachmentService;
    private final PointService pointService;
    private final RedisTemplate<String, String> redisTemplate;

    public void signUp(SignUpUserRequestDto signupUserRequestDto, MultipartFile file) throws IOException {
        boolean isExistUser = userRepository.existsByEmail(signupUserRequestDto.getEmail());

        if (isExistUser) {
            throw new ApplicationException(EMAIL_DUPLICATED);
        }

        String encryptPassword = passwordEncoder.encode(signupUserRequestDto.getPassword());
        User user = User.of(encryptPassword, signupUserRequestDto);

        User savedUser = userRepository.save(user);
        pointService.generatePoint(savedUser);

        if (file != null) {
            attachmentService.addAttachment(file, savedUser.getId(), TableRole.USER);
        }
    }

    @Transactional(readOnly = true)
    public SaveTokenResponseDto signIn(SignInUserRequestDto signinUserRequestDto) {
        User user = userRepository.findByEmailOrElseThrow(signinUserRequestDto.getEmail());

        if (!passwordEncoder.matches(signinUserRequestDto.getPassword(), user.getPassword())) {
            throw new ApplicationException(INVALID_PASSWORD_OR_EMAIL);
        }

        if (user.isDeleted()) {
            throw new ApplicationException(USER_NOT_FOUND);
        }
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail(), user.getRole());

        SaveTokenResponseDto saveTokenResponseDto = SaveTokenResponseDto.of(accessToken, refreshToken);
        return saveTokenResponseDto;
    }

    public void updatePassword(UpdatePasswordUserRequestDto updateUserRequestDto) {

        User user = userRepository.findByEmailOrElseThrow(updateUserRequestDto.getEmail());

        String encryptPassword = passwordEncoder.encode(updateUserRequestDto.getNewPassword());

        user.updatePassword(encryptPassword);
    }

    public String getAccessToken(String refreshToken) throws UnsupportedEncodingException {

        //barrer%20에서 %20을 공백으로 제거하는 메서드입니다
        refreshToken = URLDecoder.decode(refreshToken, "UTF-8");
        //barrer을 제거하는 메서드입니다
        refreshToken = jwtUtil.substringToken(refreshToken);

        //jwt토큰에서 유저 정보를 가져오는 메서드입니다
        Claims claims = jwtUtil.getUserInfoFromToken(refreshToken);
        //redis에서 키에 대한 값을 가져오기위해 id값을 가져옵니다
        Long userId = Long.valueOf(claims.getSubject());
        //redis 키는 user:id:1 같은 형식이반다
        String redisKey = "user:refresh:id:" + userId;

        //redis에서 키에 해당하는 값을 가져옵니다
        String redisToken = redisTemplate.opsForValue().get(redisKey);
        //redis에 저장된 형식은 barrer ~형식이기때문에 앞의 문자를 제거합니다
        redisToken = jwtUtil.substringToken(redisToken);

        //없거나, 일치하지않다면 에러를 던집니다.
        if (redisToken == null || !redisToken.equals(refreshToken)) {
            throw new ApplicationException(REFRESH_TOKEN_NOT_FOUND);
        }

        //토큰에서 새로운 acceesToken을 생성하기위해 email과 role을 가져옵니다.
        String email = claims.get(USER_EMAIL, String.class);
        String userRole = claims.get(USER_ROLE, String.class);

        return jwtUtil.createAccessToken(userId, email, UserRole.valueOf(userRole));
    }

    public boolean alreadySignUp(String email) {
        return userRepository.existsByEmail(email);
    }

    public void signUpKakaoUser(Long id, String nickname, String email, String image) {
        String password = UUID.randomUUID().toString();
        password = passwordEncoder.encode(password);
        String birth = LocalDate.now().toString();
        SignUpUserRequestDto signupUserRequestDto = new SignUpUserRequestDto(email, password, nickname, "ROLE_USER",
                birth, true, id.toString(), "한국");
        User user = User.of(password, signupUserRequestDto);

        User savedUser = userRepository.save(user);
        pointService.generatePoint(savedUser);

        if (image != null) {
            attachmentService.addAttachmentWithUrl(image, savedUser.getId(), TableRole.USER);
        }
    }

    public void signUpNaverUser(long id, String nickname, String email, String mobile) {

        String password = String.valueOf(id);
        password = passwordEncoder.encode(password);

        String birth = LocalDate.now().toString();

        SignUpUserRequestDto signupUserRequestDto = new SignUpUserRequestDto(email, password, nickname, "ROLE_USER",
                birth, true, mobile, "한국");

        User user = User.of(password, signupUserRequestDto);

        User savedUser = userRepository.save(user);
        pointService.generatePoint(savedUser);

        if (mobile != null) {
            attachmentService.addAttachmentWithUrl(mobile, savedUser.getId(), TableRole.USER);
        }
    }
}
