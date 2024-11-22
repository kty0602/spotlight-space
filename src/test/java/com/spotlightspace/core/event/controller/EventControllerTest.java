package com.spotlightspace.core.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.common.exception.GlobalExceptionHandler;
import com.spotlightspace.core.attachment.domain.Attachment;
import com.spotlightspace.core.attachment.dto.response.GetAttachmentResponseDto;
import com.spotlightspace.core.attachment.service.AttachmentService;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.domain.EventCategory;
import com.spotlightspace.core.event.domain.EventElastic;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.event.dto.request.SearchEventRequestDto;
import com.spotlightspace.core.event.dto.request.UpdateEventRequestDto;
import com.spotlightspace.core.event.dto.response.CreateEventResponseDto;
import com.spotlightspace.core.event.dto.response.GetEventElasticResponseDto;
import com.spotlightspace.core.event.dto.response.GetEventResponseDto;
import com.spotlightspace.core.event.dto.response.UpdateEventResponseDto;
import com.spotlightspace.core.event.service.EventService;
import com.spotlightspace.core.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.spotlightspace.core.data.EventTestData.createDefaultEventRequestDto;
import static com.spotlightspace.core.data.EventTestData.updateDefaultEventRequestDto;
import static com.spotlightspace.core.data.UserTestData.testArtist;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = EventController.class)
@ContextConfiguration(classes = {EventController.class, GlobalExceptionHandler.class})
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private AttachmentService attachmentService;

    @Autowired
    WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @Test
    @DisplayName("이벤트 생성")
    void createEvent_Success() throws Exception {

        // given
        User user = testArtist();
        CreateEventRequestDto requestDto = createDefaultEventRequestDto();
        Event event = Event.create(requestDto,user);

        when(eventService.createEvent(any(), any(), any())).thenReturn(CreateEventResponseDto.from(event));

        MockMultipartFile imageFile = new MockMultipartFile(
                "files", // 파라미터 이름
                "image.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47}
        );

        // requestDto는 JSON으로 변환하여 전송
        MockMultipartFile jsonPart = new MockMultipartFile(
                "requestDto", // 파라미터 이름
                "requestDto.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(requestDto)
        );

        // when & then
        mockMvc.perform(multipart("/api/v1/event")
                        .file(imageFile)
                        .file(jsonPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isCreated())
                        .andDo(print());
    }

    @Test
    @DisplayName("이벤트 수정_성공")
    void updateEvent_Success() throws Exception {

        // given
        Long eventId = 1L;
        User user = testArtist();
        CreateEventRequestDto createEventRequestDto = createDefaultEventRequestDto();
        UpdateEventRequestDto updateEventRequestDto = updateDefaultEventRequestDto();
        Event event = Event.create(createEventRequestDto, user);
        ReflectionTestUtils.setField(event, "id", 1L);

        when(eventService.updateEvent(any(UpdateEventRequestDto.class), any(AuthUser.class), anyLong()))
                .thenReturn(UpdateEventResponseDto.from(event));

        // when & then
        mockMvc.perform(patch("/api/v1/event/{eventId}", eventId)
                .content(objectMapper.writeValueAsString(updateEventRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("이벤트 상세 조회")
    void getEvent_Success() throws Exception {

        // given
        Long eventId = 1L;
        User user = testArtist();
        CreateEventRequestDto requestDto = createDefaultEventRequestDto();
        Event event = Event.create(requestDto, user);

        when(eventService.getEvent(any())).thenReturn(GetEventResponseDto.from(event));

        // when & then
        mockMvc.perform(get("/api/v1/event/{eventId}", eventId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("이벤트 리스트 조회")
    void searchEvent_Success() throws Exception {

        // given
        User user = testArtist();
        CreateEventRequestDto createEventRequestDto = createDefaultEventRequestDto();
        Event event = Event.create(createEventRequestDto, user);

        int page = 1;
        int size = 10;
        String title = "test";
        Integer maxPeople = 100;
        String location = "서울";
        EventCategory category = EventCategory.COMMUNITY;
        LocalDate recruitmentStartAt = LocalDate.of(2024, 10, 1);
        LocalDate recruitmentFinishAt = LocalDate.of(2024, 10, 31);
        String type = "";

        SearchEventRequestDto requestDto = SearchEventRequestDto.of(
                title, maxPeople, location, category,
                recruitmentStartAt.atStartOfDay(), recruitmentFinishAt.plusDays(1).atStartOfDay()
        );

        Page<GetEventResponseDto> eventPage = new PageImpl<>(Collections.singletonList(GetEventResponseDto.from(event)));
        when(eventService.getEvents(eq(page), eq(size), eq(requestDto), eq(type))).thenReturn(eventPage);

        // when & then
        mockMvc.perform(get("/api/v1/event")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("title", title)
                        .param("maxPeople", String.valueOf(maxPeople))
                        .param("location", location)
                        .param("category", category.name())
                        .param("recruitmentStartAt", recruitmentStartAt.toString())
                        .param("recruitmentFinishAt", recruitmentFinishAt.toString())
                        .param("type", type)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("엘라스틱 서치 조회")
    void searchElasticEvent_Success() throws Exception {

        // given
        Long eventId = 1L;
        CreateEventRequestDto createEventRequestDto = createDefaultEventRequestDto();
        EventElastic eventElastic = EventElastic.create(createEventRequestDto, eventId);

        int page = 1;
        int size = 10;
        String title = "test";
        Integer maxPeople = 100;
        String location = "서울";
        EventCategory category = EventCategory.COMMUNITY;
        LocalDate recruitmentStartAt = LocalDate.of(2024, 10, 1);
        LocalDate recruitmentFinishAt = LocalDate.of(2024, 10, 31);
        String type = "";

        SearchEventRequestDto requestDto = SearchEventRequestDto.of(
                title, maxPeople, location, category,
                recruitmentStartAt.atStartOfDay(), recruitmentFinishAt.plusDays(1).atStartOfDay()
        );

        Page<GetEventElasticResponseDto> eventElasticPage =
                new PageImpl<>(Collections.singletonList(GetEventElasticResponseDto.from(eventElastic)));
        when(eventService.getElasticEvents(eq(page), eq(size), eq(requestDto), eq(type))).thenReturn(eventElasticPage);

        // when & then
        mockMvc.perform(get("/api/v1/event/search")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("title", title)
                        .param("maxPeople", String.valueOf(maxPeople))
                        .param("location", location)
                        .param("category", category.name())
                        .param("recruitmentStartAt", recruitmentStartAt.toString())
                        .param("recruitmentFinishAt", recruitmentFinishAt.toString())
                        .param("type", type)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("이벤트 삭제")
    void deleteEvent_Success() throws Exception {

        // given
        Long eventId = 1L;
        User user = testArtist();
        CreateEventRequestDto requestDto = createDefaultEventRequestDto();
        Event event = Event.create(requestDto, user);

        when(eventService.getEvent(any())).thenReturn(GetEventResponseDto.from(event));

        // when & then
        mockMvc.perform(delete("/api/v1/event/{eventId}", eventId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("이벤트 관련_첨부파일 가져오기")
    void getAttachment_Success() throws Exception {

        // given
        Long eventId = 1L;
        Attachment attachment1 = Attachment.create("https://example.com/file1.png", TableRole.EVENT, eventId);
        Attachment attachment2 = Attachment.create("https://example.com/file2.jpg", TableRole.EVENT, eventId);

        GetAttachmentResponseDto attachmentResponse1 = GetAttachmentResponseDto.from(attachment1);
        GetAttachmentResponseDto attachmentResponse2 = GetAttachmentResponseDto.from(attachment2);

        List<GetAttachmentResponseDto> attachments = Arrays.asList(attachmentResponse1, attachmentResponse2);

        when(attachmentService.getAttachmentList(eq(eventId), eq(TableRole.EVENT))).thenReturn(attachments);

        // when & then
        mockMvc.perform(get("/api/v1/event/{eventId}/attachments", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].url").value("https://example.com/file1.png"))
                .andExpect(jsonPath("$[1].url").value("https://example.com/file2.jpg"))
                .andDo(print());
    }

}
