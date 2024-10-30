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
import com.spotlightspace.core.ticket.repository.TicketRepository;
import com.spotlightspace.core.eventticketstock.repository.EventTicketStockRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.domain.UserRole;
import com.spotlightspace.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.spotlightspace.common.exception.ErrorCode.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AttachmentService attachmentService;
    private final TicketRepository ticketRepository;
    private final EventTicketStockRepository eventTicketStockRepository;

    @Transactional
    public CreateEventResponseDto createEvent(CreateEventRequestDto requestDto, AuthUser authUser, List<MultipartFile> files) throws IOException {
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

        return CreateEventResponseDto.from(event);
    }

    @Transactional
    public UpdateEventResponseDto updateEvent(UpdateEventRequestDto requestDto, AuthUser authUser, Long id) {
        // 이벤트 존재 유무 검사
        Event event = checkEventExist(id);
        // 이벤트를 작성한 아티스트인가 검사
        checkEventAndUser(event, authUser);
        // 현재 이벤트에 결제된 티켓 수 조회
        int ticketCount = ticketRepository.countTicketByEvent(event.getId());

        // 수정 로직
        if (requestDto.getTitle() != null) {
            event.changeTitle(requestDto.getTitle());
        }
        if (requestDto.getContent() != null) {
            event.changeContent(requestDto.getContent());
        }
        if (requestDto.getLocation() != null) {
            event.changeLocation(requestDto.getLocation());
        }
        if (requestDto.getStartAt() != null) {
            event.changeStartAt(requestDto.getStartAt());
        }
        if (requestDto.getEndAt() != null) {
            event.changeEndAt(requestDto.getEndAt());
        }
        if (requestDto.getMaxPeople() != null) {
            // 변경하려는 maxPeople값이 이미 결제한 사람 언더일 때 exception 처리 해야함
            if (requestDto.getMaxPeople() < ticketCount) {
                throw new ApplicationException(CANNOT_MAX_PEOPLE_UPDATE);
            }
            event.changeMaxPeople(requestDto.getMaxPeople());
        }
        if (requestDto.getPrice() != null) {
            event.changePrice(requestDto.getPrice());
        }
        if (requestDto.getCategory() != null) {
            event.changeCategory(requestDto.getCategory());
        }
        if (requestDto.getRecruitmentStartAt() != null) {
            event.changeRecruitmentStartAt(requestDto.getRecruitmentStartAt());
        }
        if (requestDto.getRecruitmentFinishAt() != null) {
            event.changeRecruitmentFinishAt(requestDto.getRecruitmentFinishAt());
        }
        eventRepository.save(event);
        return UpdateEventResponseDto.from(event);
    }

    @Transactional
    public void deleteEvent(Long id, AuthUser authUser) {
        // 이벤트 존재 유무 검사
        Event event = checkEventExist(id);
        // 이벤트를 작성한 아티스트인가 검사
        checkEventAndUser(event, authUser);
        // 삭제 진행 시 결제한 사람 (포인트, 쿠폰)환불처리
        attachmentService.deleteAttachmentWithOtherTable(event.getId(), TableRole.EVENT);
        event.deleteEvent();
    }

    public GetEventResponseDto getEvent(Long eventId) {
        Event event = eventRepository.findByIdOrElseThrow(eventId);
        return GetEventResponseDto.from(event);
    }

    public Page<GetEventResponseDto> getEvents(int page, int size, SearchEventRequestDto searchEventRequestDto, String type) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<GetEventResponseDto> events = eventRepository.searchEvents(searchEventRequestDto, type, pageable);
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
}
