package com.spotlightspace.core.event.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.event.dto.AddEventRequestDto;
import com.spotlightspace.core.event.dto.AddEventResponseDto;
import com.spotlightspace.core.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping()
    public ResponseEntity<AddEventResponseDto> addEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestPart AddEventRequestDto requestDto,
            @RequestPart(required = false) List<MultipartFile> files
            ) throws IOException {
        AddEventResponseDto addEventResponseDto = eventService.addEvent(requestDto, authUser, files);
        return new ResponseEntity<>(addEventResponseDto, HttpStatus.OK);
    }
}
