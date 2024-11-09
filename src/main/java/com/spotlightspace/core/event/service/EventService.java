package com.spotlightspace.core.event.service;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.attachment.service.AttachmentService;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.domain.EventElastic;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.event.dto.request.SearchEventRequestDto;
import com.spotlightspace.core.event.dto.request.UpdateEventRequestDto;
import com.spotlightspace.core.event.dto.response.CreateEventResponseDto;
import com.spotlightspace.core.event.dto.response.GetEventElasticResponseDto;
import com.spotlightspace.core.event.dto.response.GetEventResponseDto;
import com.spotlightspace.core.event.dto.response.UpdateEventResponseDto;
import com.spotlightspace.core.event.repository.EventElasticRepository;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.eventticketstock.domain.EventTicketStock;
import com.spotlightspace.core.payment.service.PaymentService;
import com.spotlightspace.core.ticket.repository.TicketRepository;
import com.spotlightspace.core.eventticketstock.repository.EventTicketStockRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.domain.UserRole;
import com.spotlightspace.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.spotlightspace.common.exception.ErrorCode.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AttachmentService attachmentService;
    private final PaymentService paymentService;
    private final TicketRepository ticketRepository;
    private final EventTicketStockRepository eventTicketStockRepository;
    private final EventElasticRepository eventElasticRepository;
    private final RedissonLockService redissonLockService;
    private static final String EVENT_LOCK_KEY = "lock:event:";

    @Transactional
    public CreateEventResponseDto createEvent(
            CreateEventRequestDto requestDto, AuthUser authUser, List<MultipartFile> files) throws IOException, InterruptedException {
        String key = EVENT_LOCK_KEY + authUser.getUserId();
        RLock lock = redissonLockService.lock(key);
        boolean isLocked = false;
        try {
            // 락을 얻을 수 있으면 isLocked가 true로 설정됨
            isLocked = lock.tryLock(0, 5, TimeUnit.SECONDS);

            if (!isLocked) {
                throw new ApplicationException(NO_HAVE_LOCK); // 락을 얻지 못한 경우 예외 발생
            }

            // 유저 확인
            User user = checkUserExist(authUser.getUserId());
            // 유저 권한 확인
            validateUserRole(user.getRole());
            // 프로필 이미지가 있다면 저장 로직
            Event event = eventRepository.save(Event.of(requestDto, user));
            if (files != null && !files.isEmpty()) {
                attachmentService.addAttachmentList(files, event.getId(), TableRole.EVENT);
            }

            eventTicketStockRepository.save(EventTicketStock.create(event));

            // 엘라스틱 이벤트 저장
            EventElastic eventElastic = EventElastic.from(requestDto, event.getId());
            eventElasticRepository.save(eventElastic);

            return CreateEventResponseDto.from(event);
        } catch (ApplicationException | InterruptedException exception) {
            log.error("에러 발생 : {}", exception.getMessage(), exception);
            throw exception;
        } finally {
            if (isLocked) {
                log.info("unlock 수행");
                redissonLockService.unlock(lock);
            }
        }
    }

    @Transactional
    public UpdateEventResponseDto updateEvent(UpdateEventRequestDto requestDto, AuthUser authUser, Long id) {
        // 이벤트 존재 유무 검사
        Event event = checkEventExist(id);
        // 이벤트를 작성한 아티스트인가 검사
        checkEventAndUser(event, authUser);
        // 현재 이벤트에 결제된 티켓 수 조회
        int ticketCount = ticketRepository.countTicketByEvent(event.getId());
        // 엘라스틱 이벤트 존재 확인
        EventElastic eventElastic = checkElasticExist(id);

        // 수정 로직
        if (requestDto.getTitle() != null) {
            event.changeTitle(requestDto.getTitle());
            eventElastic.changeTitle(requestDto.getTitle());
        }
        if (requestDto.getContent() != null) {
            event.changeContent(requestDto.getContent());
            eventElastic.changeContent(requestDto.getContent());
        }
        if (requestDto.getLocation() != null) {
            event.changeLocation(requestDto.getLocation());
            eventElastic.changeLocation(requestDto.getLocation());
        }
        if (requestDto.getStartAt() != null) {
            event.changeStartAt(requestDto.getStartAt());
            eventElastic.changeStartAt(requestDto.getStartAt());
        }
        if (requestDto.getEndAt() != null) {
            event.changeEndAt(requestDto.getEndAt());
            event.changeEndAt(requestDto.getEndAt());
        }
        if (requestDto.getMaxPeople() != null) {
            // 변경하려는 maxPeople값이 이미 결제한 사람 언더일 때 exception 처리 해야함
            if (requestDto.getMaxPeople() < ticketCount) {
                throw new ApplicationException(CANNOT_MAX_PEOPLE_UPDATE);
            }
            event.changeMaxPeople(requestDto.getMaxPeople());
            eventElastic.changeMaxPeople(requestDto.getMaxPeople());
        }
        if (requestDto.getPrice() != null) {
            event.changePrice(requestDto.getPrice());
            eventElastic.changePrice(requestDto.getPrice());
        }
        if (requestDto.getCategory() != null) {
            event.changeCategory(requestDto.getCategory());
            eventElastic.changeCategory(requestDto.getCategory());
        }
        if (requestDto.getRecruitmentStartAt() != null) {
            event.changeRecruitmentStartAt(requestDto.getRecruitmentStartAt());
            eventElastic.changeRecruitmentStartAt(requestDto.getRecruitmentStartAt());
        }
        if (requestDto.getRecruitmentFinishAt() != null) {
            event.changeRecruitmentFinishAt(requestDto.getRecruitmentFinishAt());
            eventElastic.changeRecruitmentFinishAt(requestDto.getRecruitmentFinishAt());
        }
        eventRepository.save(event);
        eventElasticRepository.save(eventElastic);
        return UpdateEventResponseDto.from(event);
    }

    @Transactional
    public void deleteEvent(Long id, AuthUser authUser) {
        // 이벤트 존재 유무 검사
        Event event = checkEventExist(id);
        // 이벤트를 작성한 아티스트인가 검사
        checkEventAndUser(event, authUser);
        // 엘라스틱 이벤트 존재 확인
        EventElastic eventElastic = checkElasticExist(id);
        // 삭제 진행 시 결제한 사람 (포인트, 쿠폰)환불처리
        if (LocalDateTime.now().isBefore(event.getStartAt())) {
            paymentService.cancelPayments(event);
        }
        attachmentService.deleteAttachmentWithOtherTable(event.getId(), TableRole.EVENT);
        event.deleteEvent();
        eventElastic.deleteEvent();
    }

    public GetEventResponseDto getEvent(Long eventId) {
        Event event = eventRepository.findByIdOrElseThrow(eventId);
        return GetEventResponseDto.from(event);
    }

    public Page<GetEventResponseDto> getEvents(
            int page, int size, SearchEventRequestDto searchEventRequestDto, String type) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<GetEventResponseDto> events = eventRepository.searchEvents(searchEventRequestDto, type, pageable);
        return events;
    }

    public Page<GetEventElasticResponseDto> getElasticEvents(
            int page, int size, SearchEventRequestDto requestDto, String type) throws IOException {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<GetEventElasticResponseDto> events = eventElasticRepository.searchElasticEvents(requestDto, type, pageable);
        return events;
    }

    // 유저 존재 확인
    private User checkUserExist(Long id) {
        return userRepository.findByIdOrElseThrow(id);
    }

    // 유저 권한 확인
    private void validateUserRole(UserRole role) {
        if (role != UserRole.ROLE_ARTIST) {
            throw new ApplicationException(USER_NOT_ARTIST);
        }
    }

    // 이벤트 존재 확인 (소프트 딜리트 검증 추가 완료)
    private Event checkEventExist(Long id) {
        return eventRepository.findByIdOrElseThrow(id);
    }

    // 이벤트를 작성한 사람인가 확인
    private void checkEventAndUser(Event event, AuthUser authUser) {
        if(!event.getUser().getId().equals(authUser.getUserId())) {
            throw new ApplicationException(USER_NOT_ACCESS_EVENT);
        }
    }

    // 엘라스틱 이벤트 존재 확인
    private EventElastic checkElasticExist(Long id) { return eventElasticRepository.findByIdOrElseThrow(id); }
}
