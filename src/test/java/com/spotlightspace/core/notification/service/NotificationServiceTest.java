package com.spotlightspace.core.notification.service;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.ticket.domain.Ticket;
import com.spotlightspace.core.ticket.repository.TicketRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.spotlightspace.core.data.UserTestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Map<Long, SseEmitter> userEmitters;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("알림 구독 완료_emitter 반환")
    void subscribe_createEmitter() {

        // given
        AuthUser authUser = testArtistAuthUser();
        User user = testArtist();
        ReflectionTestUtils.setField(user, "id", authUser.getUserId());

        given(userRepository.findByIdOrElseThrow(authUser.getUserId())).willReturn(user);

        // when
        SseEmitter emitter = notificationService.subscribe(authUser);

        // then
        assertNotNull(emitter);
    }
}
