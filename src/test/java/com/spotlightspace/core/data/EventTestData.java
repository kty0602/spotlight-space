package com.spotlightspace.core.data;

import com.spotlightspace.core.event.domain.EventCategory;
import com.spotlightspace.core.event.domain.EventElastic;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.event.dto.request.UpdateEventRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.test.util.ReflectionTestUtils;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.user.domain.User;

import java.time.LocalDateTime;

import static com.spotlightspace.core.data.UserTestData.testArtist;

@RequiredArgsConstructor
public class EventTestData {

    public static CreateEventRequestDto createDefaultEventRequestDto() {
        return CreateEventRequestDto.of(
                "test1",
                "test1",
                "서울",
                LocalDateTime.parse("2024-10-23T15:30:00"),
                LocalDateTime.parse("2024-10-23T18:30:00"),
                80,
                30000,
                EventCategory.COMMUNITY,
                LocalDateTime.parse("2024-10-23T12:00:00"),
                LocalDateTime.parse("2024-10-23T14:00:00")
        );
    }

    public static CreateEventRequestDto createDefaultEventRequestDto2() {
        return CreateEventRequestDto.of(
                "test2",
                "test2",
                "제주",
                LocalDateTime.parse("2024-10-23T15:30:00"),
                LocalDateTime.parse("2024-10-23T18:30:00"),
                20,
                15000,
                EventCategory.ART,
                LocalDateTime.parse("2024-10-23T12:00:00"),
                LocalDateTime.parse("2024-10-23T14:00:00")
        );
    }

    public static UpdateEventRequestDto updateDefaultEventRequestDto() {
        return UpdateEventRequestDto.of(
                "수정 test1",
                "수정 test1",
                "울산",
                LocalDateTime.parse("2024-10-23T15:30:00"),
                LocalDateTime.parse("2024-10-23T18:30:00"),
                40,
                29000,
                EventCategory.COMMUNITY,
                LocalDateTime.parse("2024-10-23T12:00:00"),
                LocalDateTime.parse("2024-10-23T14:00:00")
        );
    }

    public static Event testEvent() {
        CreateEventRequestDto eventRequestDto = createDefaultEventRequestDto();
        User user = testArtist();
        ReflectionTestUtils.setField(user, "id", 1L);
        Event event = Event.of(eventRequestDto, user);
        ReflectionTestUtils.setField(event, "id", 1L);
        return event;
    }

    public static Event testEvent2() {
        CreateEventRequestDto eventRequestDto = createDefaultEventRequestDto2();
        User user = testArtist();
        ReflectionTestUtils.setField(user, "id", 1L);
        Event event = Event.of(eventRequestDto, user);
        ReflectionTestUtils.setField(event, "id", 2L);
        return event;
    }

    public static EventElastic testEventElastic1() {
        Event event = testEvent();
        CreateEventRequestDto eventRequestDto = createDefaultEventRequestDto();
        EventElastic eventElastic = EventElastic.of(eventRequestDto, event.getId());
        ReflectionTestUtils.setField(eventElastic, "id", 1L);
        return eventElastic;
    }

    public static EventElastic testEventElastic2() {
        Event event = testEvent2();
        CreateEventRequestDto eventRequestDto = createDefaultEventRequestDto();
        EventElastic eventElastic = EventElastic.of(eventRequestDto, event.getId());
        ReflectionTestUtils.setField(eventElastic, "id", 2L);
        return eventElastic;
    }
}
