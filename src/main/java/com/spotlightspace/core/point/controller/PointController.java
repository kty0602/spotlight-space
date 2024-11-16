package com.spotlightspace.core.point.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.point.dto.request.CreatePointRequestDto;
import com.spotlightspace.core.point.dto.response.CreatePointResponseDto;
import com.spotlightspace.core.point.dto.response.GetPointResponseDto;
import com.spotlightspace.core.point.service.PointService;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private final UserRepository userRepository;

    /**
     * 포인트 등록 컨트롤러
     *
     * @param authUser
     * @param requestDto amount 적립할 포인트 값이 들어간다.
     * @return
     */
    @PostMapping()
    public ResponseEntity<CreatePointResponseDto> createPoint(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody CreatePointRequestDto requestDto
    ) {

        User user = userRepository.findByIdOrElseThrow(authUser.getUserId());
        int price = requestDto.getPrice();
        CreatePointResponseDto createPointResponseDto = pointService.createPoint(price, user);
        return new ResponseEntity<>(createPointResponseDto, HttpStatus.OK);
    }

    /**
     * 포인트 조회
     * @param authUser
     * @return
     */
    @GetMapping()
    public ResponseEntity<GetPointResponseDto> getPoint(
            @AuthenticationPrincipal AuthUser authUser
    ) {

        GetPointResponseDto getPointResponseDto = pointService.getPoint(authUser);
        return new ResponseEntity<>(getPointResponseDto, HttpStatus.OK);
    }
}
