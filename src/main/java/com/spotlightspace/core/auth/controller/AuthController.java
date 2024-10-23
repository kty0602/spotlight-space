package com.spotlightspace.core.auth.controller;

import com.spotlightspace.core.auth.dto.SigninUserRequestDto;
import com.spotlightspace.core.auth.dto.SignupUserRequestDto;
import com.spotlightspace.core.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    public final AuthService authService;

    @PostMapping("/auth/sign-up")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignupUserRequestDto signupUserRequestDto) {
        String accessToken = authService.saveUser(signupUserRequestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/sign-in")
    public ResponseEntity<Void> signIn(@Valid @RequestBody SigninUserRequestDto signInUserRequestDto) {
        String accessToken = authService.getUserWithEmailAndPassword(signInUserRequestDto);
        return ResponseEntity.ok().build();
    }
}
