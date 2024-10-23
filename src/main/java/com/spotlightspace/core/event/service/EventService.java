package com.spotlightspace.core.event.service;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.event.dto.AddEventRequestDto;
import com.spotlightspace.core.event.dto.EventResponseDto;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

//    @Transactional
//    public EventResponseDto addEvent(AddEventRequestDto requestDto, AuthUser authUser) {
//        // 유저 확인
//        User user = checkUserExist(authUser.getUserId());
//        // 유저 권한 확인
//
//
//    }

    // 유저 존재 확인
    private User checkUserExist(Long id) {
        return userRepository.findByIdOrElseThrow(id);
    }

    // 유저 권한 확인
    private void validateUserRole() {

    }
}
