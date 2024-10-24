package com.spotlightspace.core.auth.controller;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.spotlightspace.core.auth.dto.SigninUserRequestDto;
import com.spotlightspace.core.auth.dto.SignupUserRequestDto;
import com.spotlightspace.core.auth.service.AuthService;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    public final AuthService authService;

    @PostMapping("/auth/signup")
    public ResponseEntity<String> signUp(
            @Valid @RequestPart SignupUserRequestDto signupUserRequestDto,
            @RequestPart(required = false) MultipartFile file) throws IOException {
        String accessToken = authService.saveUser(signupUserRequestDto, file);
        return ResponseEntity.ok()
                .header(AUTHORIZATION, accessToken)
                .build();
    }

    @PostMapping("/auth/signin")
    public ResponseEntity<String> signIn(@Valid @RequestBody SigninUserRequestDto signInUserRequestDto) {
        String accessToken = authService.signin(signInUserRequestDto);
        return ResponseEntity.ok()
                .header(AUTHORIZATION, accessToken)
                .build();
    }
}
