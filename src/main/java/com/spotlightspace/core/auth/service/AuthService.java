package com.spotlightspace.core.auth.service;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.common.exception.ErrorCode;
import com.spotlightspace.config.JwtUtil;
import com.spotlightspace.core.auth.dto.SigninUserRequestDto;
import com.spotlightspace.core.auth.dto.SignupUserRequestDto;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import java.security.InvalidParameterException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public String saveUser(SignupUserRequestDto signupUserRequestDto) {
        boolean isExistUser = userRepository.existsByEmail(signupUserRequestDto.getEmail());

        if (isExistUser) {
            throw new ApplicationException(ErrorCode.USER_NOT_FOUND);
        }

        String encryptPassword = passwordEncoder.encode(signupUserRequestDto.getPassword());
        User user = User.from(encryptPassword, signupUserRequestDto);

        User savedUser = userRepository.save(user);

        return jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
    }

    @Transactional(readOnly = true)
    public String getUserWithEmailAndPassword(SigninUserRequestDto signinUserRequestDto) {
        User user = userRepository.findByEmailOrElseThrow(signinUserRequestDto.getEmail());

        if (!passwordEncoder.matches(signinUserRequestDto.getPassword(), user.getPassword())) {
            throw new ApplicationException(ErrorCode.INVALID_PASSWORD_OR_EMAIL);
        }

        return jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole());
    }
}

