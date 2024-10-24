package com.spotlightspace.core.auth.service;

import static com.spotlightspace.common.exception.ErrorCode.USER_NOT_FOUND;

import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.common.exception.ErrorCode;
import com.spotlightspace.config.JwtUtil;
import com.spotlightspace.core.attachment.service.AttachmentService;
import com.spotlightspace.core.auth.dto.SigninUserRequestDto;
import com.spotlightspace.core.auth.dto.SignupUserRequestDto;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
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

    public String saveUser(SignupUserRequestDto signupUserRequestDto, MultipartFile file) throws IOException {
        boolean isExistUser = userRepository.existsByEmail(signupUserRequestDto.getEmail());

        if (isExistUser) {
            throw new ApplicationException(USER_NOT_FOUND);
        }

        String encryptPassword = passwordEncoder.encode(signupUserRequestDto.getPassword());
        User user = User.of(encryptPassword, signupUserRequestDto);

        User savedUser = userRepository.save(user);

        if(file != null) {
            attachmentService.addAttachment(file,savedUser.getId(), TableRole.USER);
        }

        return jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
    }

    @Transactional(readOnly = true)
    public String signin(SigninUserRequestDto signinUserRequestDto) {
        User user = userRepository.findByEmailOrElseThrow(signinUserRequestDto.getEmail());

        if (!passwordEncoder.matches(signinUserRequestDto.getPassword(), user.getPassword())) {
            throw new ApplicationException(ErrorCode.INVALID_PASSWORD_OR_EMAIL);
        }

        if(user.isDeleted())
        {
            throw new ApplicationException(USER_NOT_FOUND);
        }

        return jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole());
    }
}

