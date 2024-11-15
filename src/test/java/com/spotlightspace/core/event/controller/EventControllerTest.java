package com.spotlightspace.core.event.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.attachment.service.AttachmentService;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.domain.EventCategory;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.event.dto.response.CreateEventResponseDto;
import com.spotlightspace.core.event.service.EventService;
import com.spotlightspace.core.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static com.spotlightspace.core.data.EventTestData.createDefaultEventRequestDto;
import static com.spotlightspace.core.data.UserTestData.testArtist;
import static com.spotlightspace.core.data.UserTestData.testAuthUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@WebMvcTest(EventController.class)
@ExtendWith(SpringExtension.class)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private AttachmentService attachmentService;

    @Test
    @DisplayName("이벤트 생성")
    void createEvent_Success() throws Exception {

        // given
        AuthUser authUser = testAuthUser();
        User user = testArtist();
        CreateEventRequestDto requestDto = createDefaultEventRequestDto();
        Event event = Event.of(requestDto, user);

        List<MultipartFile> files = List.of(
                new MockMultipartFile("file1", "test1.png", "image/png", "test content".getBytes()),
                new MockMultipartFile("file2", "test2.png", "image/png", "test content".getBytes())
        );

        CreateEventResponseDto responseDto = CreateEventResponseDto.from(event);

        given(eventService.createEvent(any(CreateEventRequestDto.class), any(AuthUser.class), anyList()))
                .willReturn(responseDto);
    }
}
