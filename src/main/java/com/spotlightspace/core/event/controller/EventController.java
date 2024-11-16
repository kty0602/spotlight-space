package com.spotlightspace.core.event.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.core.attachment.dto.response.GetAttachmentResponseDto;
import com.spotlightspace.core.attachment.service.AttachmentService;
import com.spotlightspace.core.event.domain.EventCategory;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.event.dto.request.SearchEventRequestDto;
import com.spotlightspace.core.event.dto.request.UpdateEventRequestDto;
import com.spotlightspace.core.event.dto.response.CreateEventResponseDto;
import com.spotlightspace.core.event.dto.response.GetEventElasticResponseDto;
import com.spotlightspace.core.event.dto.response.GetEventResponseDto;
import com.spotlightspace.core.event.dto.response.UpdateEventResponseDto;
import com.spotlightspace.core.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final AttachmentService attachmentService;

    /**
     * @param authUser
     * @param requestDto : title, content, location, price, etc... 값들을 받음
     * @param files      : 홍보용 이미지 혹은 첨부파일을 첨부 가능
     * @return
     * @throws IOException
     */
    @PostMapping()
    public ResponseEntity<CreateEventResponseDto> createEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestPart CreateEventRequestDto requestDto,
            @RequestPart(required = false) List<MultipartFile> files
    ) throws IOException, InterruptedException {

        CreateEventResponseDto createEventResponseDto = eventService.createEvent(requestDto, authUser, files);
        return new ResponseEntity<>(createEventResponseDto, HttpStatus.CREATED);
    }

    /**
     * @param authUser
     * @param requestDto : title, content, location, price, etc... 변경할 필드의 값을 받음
     * @param eventId    : 변경이 이뤄질 eventId값
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
     * 이벤트 상세 조회
     * @param eventId 해당 이벤트 id값
     * @return
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<GetEventResponseDto> getEvent(
            @PathVariable("eventId") Long eventId
    ) {
        GetEventResponseDto responseDto = eventService.getEvent(eventId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    /**
     * 이벤트 리스트 조회
     * @param page
     * @param size
     * @param title
     * @param maxPeople
     * @param location
     * @param category
     * @param recruitmentStartAt
     * @param recruitmentFinishAt
     * @param type  type은 테스트 시 체크하고 진행해야 합니다. 체크 상태에서 아무 값 안넣고 보내면 default로 빠지게 되어있음
     * @return
     */
    @GetMapping()
    public ResponseEntity<Page<GetEventResponseDto>> searchEvent(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "maxPeople", required = false) Integer maxPeople,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "category", required = false) EventCategory category,
            @RequestParam(value = "recruitmentStartAt", required = false) LocalDate recruitmentStartAt,
            @RequestParam(value = "recruitmentFinishAt", required = false) LocalDate recruitmentFinishAt,
            @RequestParam(value = "type") String type
    ) {

        // 입력값이 2024-09-01 이면 2024-09-01 00:00:00으로 변환
        // 기간 끝은 하루 더해서 자정값으로 변환
        LocalDateTime recruitmentStart = (recruitmentStartAt != null)
                ? recruitmentStartAt.atStartOfDay() : null;
        LocalDateTime recruitmentFinish = (recruitmentFinishAt != null)
                ? recruitmentFinishAt.plusDays(1).atStartOfDay() : null;

        SearchEventRequestDto requestDto =
                SearchEventRequestDto.of(title, maxPeople, location, category, recruitmentStart, recruitmentFinish);
        Page<GetEventResponseDto> responseDtoPage = eventService.getEvents(page, size, requestDto, type);
        return new ResponseEntity<>(responseDtoPage, HttpStatus.OK);
    }

    /**
     * 엘라스틱 서치를 위한 컨트롤러
     * @param page
     * @param size
     * @param title
     * @param maxPeople
     * @param location
     * @param category
     * @param recruitmentStartAt
     * @param recruitmentFinishAt
     * @param type  아무값도 없으면 수정 날짜로 desc, upprice, downprice, date값을 넣으면 각각에 맞는 정렬이 수행
     * @return
     * @throws IOException
     */
    @GetMapping("/search")
    public ResponseEntity<Page<GetEventElasticResponseDto>> searchElasticEvent(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "maxPeople", required = false) Integer maxPeople,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "category", required = false) EventCategory category,
            @RequestParam(value = "recruitmentStartAt", required = false) LocalDate recruitmentStartAt,
            @RequestParam(value = "recruitmentFinishAt", required = false) LocalDate recruitmentFinishAt,
            @RequestParam(value = "type") String type
    ) throws IOException {
        LocalDateTime recruitmentStart = (recruitmentStartAt != null)
                ? recruitmentStartAt.atStartOfDay() : null;
        LocalDateTime recruitmentFinish = (recruitmentFinishAt != null)
                ? recruitmentFinishAt.plusDays(1).atStartOfDay() : null;

        SearchEventRequestDto requestDto =
                SearchEventRequestDto.of(title, maxPeople, location, category, recruitmentStart, recruitmentFinish);

        Page<GetEventElasticResponseDto> responseDtoPage = eventService.getElasticEvents(page, size, requestDto, type);
        return new ResponseEntity<>(responseDtoPage, HttpStatus.OK);
    }

    /**
     * @param authUser
     * @param eventId  : 삭제가 진행될 eventId값
     * @return
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Map<String, String>> deleteEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("eventId") Long eventId
    ) {

        eventService.deleteEvent(eventId, authUser);
        Map<String, String> response = new HashMap<>();
        response.put("message", "성공적으로 삭제되었습니다!");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 해당 이벤트가 가지고 있는 첨부파일 리스트
     *
     * @param eventId
     * @return
     */
    @GetMapping("/{eventId}/attachments")
    public ResponseEntity<List<GetAttachmentResponseDto>> getAttachment(
            @PathVariable("eventId") Long eventId
    ) {

        List<GetAttachmentResponseDto> attachmentList = attachmentService.getAttachmentList(eventId, TableRole.EVENT);
        return new ResponseEntity<>(attachmentList, HttpStatus.OK);
    }
}
