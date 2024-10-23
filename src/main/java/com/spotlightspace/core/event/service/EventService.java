package com.spotlightspace.core.event.service;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.common.exception.ErrorCode;
import com.spotlightspace.core.attachment.service.AttachmentService;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.dto.AddEventRequestDto;
import com.spotlightspace.core.event.dto.AddEventResponseDto;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.domain.UserRole;
import com.spotlightspace.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AttachmentService attachmentService;

    @Transactional
    public AddEventResponseDto addEvent(AddEventRequestDto requestDto, AuthUser authUser, List<MultipartFile> files) throws IOException {
        // 유저 확인
        User user = checkUserExist(authUser.getUserId());
        // 유저 권한 확인
        validateUserRole(user.getRole());
        // 프로필 이미지가 있다면 저장 로직
        Event event = eventRepository.save(Event.of(requestDto, user));
        if (!files.isEmpty()) {
            attachmentService.addAttachmentList(files, event.getId(), TableRole.EVENT);
        }
        return AddEventResponseDto.from(event);
    }

    // 유저 존재 확인
    private User checkUserExist(Long id) {
        return userRepository.findByIdOrElseThrow(id);
    }

    // 유저 권한 확인
    private void validateUserRole(UserRole role) {
        if (role != UserRole.ROLE_ARTIST) {
            throw new ApplicationException(ErrorCode.USER_NOT_ARTIST);
        }
    }
}
