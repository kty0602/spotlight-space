package com.spotlightspace.core.event.service;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.attachment.service.AttachmentService;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.event.dto.request.SearchEventRequestDto;
import com.spotlightspace.core.event.dto.request.UpdateEventRequestDto;
import com.spotlightspace.core.event.dto.response.CreateEventResponseDto;
import com.spotlightspace.core.event.dto.response.GetEventResponseDto;
import com.spotlightspace.core.event.dto.response.UpdateEventResponseDto;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.eventticketstock.domain.EventTicketStock;
import com.spotlightspace.core.eventticketstock.repository.EventTicketStockRepository;
import com.spotlightspace.core.ticket.repository.TicketRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.spotlightspace.common.exception.ErrorCode.EVENT_NOT_FOUND;
import static com.spotlightspace.common.exception.ErrorCode.USER_NOT_FOUND;
import static com.spotlightspace.core.data.EventTestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.spotlightspace.core.data.UserTestData.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventTicketStockRepository eventTicketStockRepository;

    @Mock
    private AttachmentService attachmentService;

    @InjectMocks
    private EventService eventService;

    @Nested
    @DisplayName("이벤트 등록 시")
    class CreateEventTests {

        @Test
        @DisplayName("이벤트를 정상적으로 생성한다.")
        void createEvent_success() throws IOException {

            // given
            AuthUser authUser = testArtistAuthUser();
            User user = testArtist();
            CreateEventRequestDto requestDto = createDefaultEventRequestDto();
            Event event = Event.of(requestDto, user);

            given(userRepository.findByIdOrElseThrow(authUser.getUserId())).willReturn(user);
            given(eventRepository.save(any(Event.class))).willReturn(event);
            given(eventTicketStockRepository.save(any(EventTicketStock.class))).willReturn(EventTicketStock.create(event));

            MultipartFile file = mock(MultipartFile.class);
            List<MultipartFile> files = List.of(file);

            // when
            CreateEventResponseDto responseDto = eventService.createEvent(requestDto, authUser, files);

            // then
            assertNotNull(responseDto);
        }

        @Test
        @DisplayName("이벤트 등록하려는 회원을 찾을 수 없음")
        void createEvent_userNotFound() {

            // given
            AuthUser authUser = testArtistAuthUser();
            CreateEventRequestDto requestDto = createDefaultEventRequestDto();

            given(userRepository.findByIdOrElseThrow(authUser.getUserId()))
                    .willThrow(new ApplicationException(USER_NOT_FOUND));

            // when
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                eventService.createEvent(requestDto, authUser, Collections.emptyList());
            });

            // then
            assertEquals("존재하지 않는 유저입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("일반 유저는 이벤트를 생성할 수 없다.")
        void createEvent_userNotArtist() {

            // given
            AuthUser authUser = testAuthUser(); // 일반 유저
            User user = testUser();
            CreateEventRequestDto requestDto = createDefaultEventRequestDto();

            given(userRepository.findByIdOrElseThrow(authUser.getUserId())).willReturn(user);

            // when
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                eventService.createEvent(requestDto, authUser, Collections.emptyList());
            });

            // then
            assertEquals("일반 유저는 해당 작업을 수행할 수 없습니다.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("이벤트 수정 시")
    class updateEventTests {

        @Test
        @DisplayName("이벤트를 정상적으로 수정한다.")
        void updateEvent_success() {

            // given
            AuthUser authUser = testArtistAuthUser();
            Event event = testEvent();

            UpdateEventRequestDto updateEventRequestDto = updateDefaultEventRequestDto();

            given(eventRepository.findByIdOrElseThrow(event.getId())).willReturn(event);
            given(eventRepository.save(event)).willReturn(event);

            // when
            UpdateEventResponseDto responseDto =
                    eventService.updateEvent(updateEventRequestDto, authUser, event.getId());

            // then
            assertNotNull(responseDto);
            assertEquals("수정 test1", responseDto.getTitle());
            assertEquals("수정 test1", responseDto.getContent());
            assertEquals("울산", responseDto.getLocation());
            assertEquals(40, responseDto.getMaxPeople());
            assertEquals(29000, responseDto.getPrice());
        }

        @Test
        @DisplayName("수정하려는 이벤트가 존재하지 않음")
        void updateEvent_NotFound() {

            // given
            AuthUser authUser = testArtistAuthUser();
            Long eventId = 1L;
            UpdateEventRequestDto requestDto = updateDefaultEventRequestDto();

            given(eventRepository.findByIdOrElseThrow(eventId)).willThrow(new ApplicationException(EVENT_NOT_FOUND));

            // when
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                eventService.updateEvent(requestDto, authUser, eventId);
            });

            // then
            assertEquals("존재하지 않는 이벤트입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("이벤트 작성자가 아닌 사용자가 수정을 요청")
        void updateEvent_UserNotAuthor() {

            // given
            Event event = testEvent();
            AuthUser authUser1 = testAnotherArtistAuthUser();
            UpdateEventRequestDto requestDto = updateDefaultEventRequestDto();

            given(eventRepository.findByIdOrElseThrow(event.getId())).willReturn(event);

            // when
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                eventService.updateEvent(requestDto, authUser1, event.getId());
            });

            // then
            assertEquals("해당 사용자가 등록한 이벤트가 아닙니다.", exception.getMessage());
        }

        @Test
        @DisplayName("결제한 인원보다 낮은 인원 수로 제한 인원을 설정하려고 할 때")
        void updateEvent_MaxPeople_LessThanTickerCount() {

            // given
            AuthUser authUser = testArtistAuthUser();
            Event event = testEvent();
            UpdateEventRequestDto requestDto = updateDefaultEventRequestDto();

            given(ticketRepository.countTicketByEvent(event.getId())).willReturn(50);
            given(eventRepository.findByIdOrElseThrow(event.getId())).willReturn(event);

            // when
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                eventService.updateEvent(requestDto, authUser, event.getId());
            });

            // then
            assertEquals("현제 결제된 인원보다 작은 수로 인원 설정이 불가합니다.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("이벤트 조회 시")
    class getEvents {

        @Test
        @DisplayName("event 단건 조회")
        void getEvent_success() {

            // given
            Event event = testEvent();

            given(eventRepository.findByIdOrElseThrow(event.getId())).willReturn(event);

            // when
            GetEventResponseDto responseDto = eventService.getEvent(event.getId());

            // then
            assertNotNull(responseDto);
        }

        @Test
        @DisplayName("event 다건 조회")
        void getEvents_success() {

            // given
            Event event1 = testEvent();
            Event event2 = testEvent2();
            List<Event> events = List.of(event1, event2);

            SearchEventRequestDto searchRequest = new SearchEventRequestDto();
            String type = "";
            int page = 1;
            int size = 10;
            Pageable pageable = PageRequest.of(page - 1, size);

            Page<GetEventResponseDto> expectedPage = new PageImpl<>(
                    events.stream()
                            .map(GetEventResponseDto::from)
                            .toList(),
                    pageable,
                    events.size()
            );

            given(eventRepository.searchEvents(searchRequest, type, pageable)).willReturn(expectedPage);

            // when
            Page<GetEventResponseDto> result = eventService.getEvents(page, size, searchRequest, type);

            // then
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(1, result.getTotalPages());
            assertEquals("test1", result.getContent().get(0).getTitle());
            assertEquals("test2", result.getContent().get(1).getTitle());
        }
    }

    @Nested
    @DisplayName("이벤트 삭제 접근 시")
    class deleteEvents {

        @Test
        @DisplayName("이벤트 삭제 성공")
        void deleteEvent_success() {

            // given
            AuthUser authUser = testArtistAuthUser();
            Event event = testEvent();

            given(eventRepository.findByIdOrElseThrow(event.getId())).willReturn(event);
            doNothing().when(attachmentService).deleteAttachmentWithOtherTable(event.getId(), TableRole.EVENT);

            // when
            eventService.deleteEvent(event.getId(), authUser);

            // then
            verify(eventRepository, times(1)).findByIdOrElseThrow(event.getId());
            assertTrue(event.getIsDeleted());
        }
    }




}
