package com.spotlightspace.core.event.service;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.attachment.service.AttachmentService;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.event.repository.EventElasticRepository;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.eventticketstock.domain.EventTicketStock;
import com.spotlightspace.core.eventticketstock.repository.EventTicketStockRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.spotlightspace.core.data.EventTestData.createDefaultEventRequestDto;
import static com.spotlightspace.core.data.UserTestData.testArtist;
import static com.spotlightspace.core.data.UserTestData.testArtistAuthUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventRedissionLockTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AttachmentService attachmentService;

    @Mock
    private EventTicketStockRepository eventTicketStockRepository;

    @Mock
    private EventElasticRepository eventElasticRepository;

    @Mock
    private RedissonLockService redissonLockService;

    @Test
    @DisplayName("따닥 테스트")
    void dadak_error() throws IOException, InterruptedException {

        // given
        AuthUser authUser = testArtistAuthUser();
        User user = testArtist();
        CreateEventRequestDto requestDto = createDefaultEventRequestDto();
        List<MultipartFile> files = List.of(mock(MultipartFile.class));
        Event event = Event.of(requestDto, user);

        given(userRepository.findByIdOrElseThrow(authUser.getUserId())).willReturn(user);
        given(eventRepository.save(any(Event.class))).willReturn(event);
        given(eventTicketStockRepository.save(any(EventTicketStock.class))).willReturn(EventTicketStock.create(event));

        RLock lock = mock(RLock.class);
        given(redissonLockService.lock(anyString())).willReturn(lock);

        // 첫 번째 요청에서 락 획득 성공, 두 번째에서 락 획득 실패
        given(lock.tryLock(0, 5, TimeUnit.SECONDS))
                .willReturn(true)
                .willReturn(false);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // 첫 번째 요청 when
        executorService.execute(() -> {
            try {
                eventService.createEvent(requestDto, authUser, files);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 두 번째 요청 when
        executorService.execute(() -> {
            assertThrows(ApplicationException.class, () ->
                    eventService.createEvent(requestDto, authUser, files));
        });

        executorService.shutdown();
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }

        // then
        verify(lock, times(2)).tryLock(0, 5, TimeUnit.SECONDS); // 락이 두 번 시도 되었는가?
        verify(redissonLockService, times(1)).unlock(lock);
    }
}
