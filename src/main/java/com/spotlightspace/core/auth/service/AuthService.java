package com.spotlightspace.core.auth.service;

import com.spotlightspace.config.JwtUtil;
import com.spotlightspace.core.auth.dto.SigninUserRequestDto;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
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

    public String saveUser(SigninUserRequestDto signinUserRequestDto) {
        boolean isExistUser = userRepository.existsByEmail(signinUserRequestDto.getEmail());

        if (isExistUser) {
            //todo :예외처리
        }

        String encryptPassword = passwordEncoder.encode(signinUserRequestDto.getPassword());
        User user = User.of(encryptPassword, signinUserRequestDto);

        User savedUser = userRepository.save(user);

        return jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getAuthority());
    }

    @Transactional(readOnly = true)
    public String getUserWithEmailAndPassword(SigninUserRequestDto signinUserRequestDto) {
        User user = userRepository.findByEmailOrElseThrow(signinUserRequestDto.getEmail());

        if (!passwordEncoder.matches(signinUserRequestDto.getPassword(), user.getPassword())) {
            //todo : 예외처리
        }

        return jwtUtil.createToken(user.getId(), user.getEmail(), user.getAuthority());
    }
}

