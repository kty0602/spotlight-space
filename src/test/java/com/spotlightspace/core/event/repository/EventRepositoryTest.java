package com.spotlightspace.core.event.repository;

import com.spotlightspace.common.annotation.AuthUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.spotlightspace.common.exception.ErrorCode.EVENT_NOT_FOUND;
import static com.spotlightspace.core.data.UserTestData.testAuthUser;
import static org.mockito.Mockito.when;

@SpringBootTest
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    @DisplayName("접근하려는 event가 없음")
    void findByIdOrElseThrow() {
        // given
        long eventId = 1L;
        AuthUser authUser = testAuthUser();

        // when & then
        Assertions.assertThatThrownBy(() -> eventRepository.findByIdAndUserIdOrElseThrow(eventId, authUser.getUserId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(EVENT_NOT_FOUND.getMessage());
    }
}
