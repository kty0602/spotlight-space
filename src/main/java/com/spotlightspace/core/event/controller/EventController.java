package com.spotlightspace.core.event.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.event.dto.AddEventRequestDto;
import com.spotlightspace.core.event.dto.AddEventResponseDto;
import com.spotlightspace.core.event.dto.UpdateEventRequestDto;
import com.spotlightspace.core.event.dto.UpdateEventResponseDto;
import com.spotlightspace.core.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * @param authUser
     * @param requestDto : title, content, location, price, etc... 값들을 받음
     * @param files : 홍보용 이미지 혹은 첨부파일을 첨부 가능
     * @return
     * @throws IOException
     */
    @PostMapping()
    public ResponseEntity<AddEventResponseDto> addEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestPart AddEventRequestDto requestDto,
            @RequestPart(required = false) List<MultipartFile> files
            ) throws IOException {
        AddEventResponseDto addEventResponseDto = eventService.addEvent(requestDto, authUser, files);
        return new ResponseEntity<>(addEventResponseDto, HttpStatus.CREATED);
    }

    /**
     * @param authUser
     * @param requestDto : title, content, location, price, etc... 변경할 필드의 값을 받음
     * @param eventId : 변경이 이뤄질 eventId값
     * @return
     */
    @PatchMapping("/{eventId}")
    public ResponseEntity<UpdateEventResponseDto> updateEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody UpdateEventRequestDto requestDto,
            @PathVariable("eventId") Long eventId
            ) {
        UpdateEventResponseDto updateEventResponseDto = eventService.updateEvent(requestDto, authUser, eventId);
        return new ResponseEntity<>(updateEventResponseDto, HttpStatus.OK);
    }

    /**
     * @param authUser
     * @param eventId : 삭제가 진행될 eventId값
     * @return
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<String> deleteEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("eventId") Long eventId
    ) {
        eventService.deleteEvent(eventId, authUser);
        return new ResponseEntity<>("성공적으로 삭제가 되었습니다.", HttpStatus.OK);
    }

//    @GetMapping("/{eventId}/attachments")
//    public ResponseEntity<GetAttachmentResponseDto> getAttachment(
//            @AuthenticationPrincipal AuthUser authUser
//    ) {
//
//    }
}
