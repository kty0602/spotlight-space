package com.spotlightspace.core.notification.service;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.ticket.domain.Ticket;
import com.spotlightspace.core.ticket.repository.TicketRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final Map<Long, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    private final TicketRepository ticketRepository;

    private final UserRepository userRepository;

    private static final String NOTIFICATION_CRON_EVERY_MINUTE = "0 */1 * * * *";

    private static final String DAY_BEFORE_NOTIFICATION = "공연 시작 하루 전 입니다.";

    private static final String HOUR_BEFORE_NOTIFICATION = "공연 시작 1시간 전 입니다.";

    // 클라이언트가 SSE 구독을 요청하면 SseEmitter를 생성하여 반환
    // (결제 성공 시 해당 요청을 보내 SSE 구독을 해놓는다.)
    public SseEmitter subscribe(AuthUser authUser) {
        User user = userRepository.findByIdOrElseThrow(authUser.getUserId());

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        userEmitters.put(user.getId(), emitter);

        try {
            emitter.send(SseEmitter.event().name("connect").data("Connected to notifications"));
        } catch (Exception e) {
            throw new RuntimeException("SSE 구독 연결 중 오류 발생", e);
        }

        // 상황별 emitter 삭제 처리
        // 완료 시, 타임 아웃 시, 에러 발생 시
        emitter.onCompletion(() -> userEmitters.remove(user.getId()));
        emitter.onTimeout(() -> userEmitters.remove(user.getId()));
        emitter.onError(e -> userEmitters.remove(user.getId()));

        return emitter;
    }

    // 매 분마다 작업 실행
    @Scheduled(cron = NOTIFICATION_CRON_EVERY_MINUTE)
    public void sendNotification() {
        LocalDateTime now = LocalDateTime.now();

        // 공연 시작 하루 전 +- 30초씩 -> 이렇게 하는 이유 나노밀리 세컨드 까지 고려함
        LocalDateTime dayBeforeStart = now.plusDays(1).minusSeconds(30);
        LocalDateTime dayBeforeEnd = now.plusDays(1).plusSeconds(30);

        // 공연 시작 1시간 전 +- 30초씩
        LocalDateTime hourBeforeStart = now.plusHours(1).minusSeconds(30);
        LocalDateTime hourBeforeEnd = now.plusHours(1).plusSeconds(30);

        List<Ticket> dayBeforeTickets = ticketRepository.findTicketsByEventStartAt(dayBeforeStart, dayBeforeEnd);
        dayBeforeTickets.forEach(ticket -> {
            String message = DAY_BEFORE_NOTIFICATION;
            sendSseNotification(ticket.getUser().getId(), message);
        });

        List<Ticket> hourBeforeTickets = ticketRepository.findTicketsByEventStartAt(hourBeforeStart, hourBeforeEnd);
        hourBeforeTickets.forEach(ticket -> {
            String message = HOUR_BEFORE_NOTIFICATION;
            sendSseNotification(ticket.getUser().getId(), message);
        });
    }

    private void sendSseNotification(Long userId, String message) {
        SseEmitter emitter = userEmitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("connect").data(message));
                System.out.println("알림 전송 성공: 사용자 ID=" + userId + ", 메시지=" + message);
            } catch (Exception exception) {
                userEmitters.remove(userId);
            }
        }
    }
}
