package com.spotlightspace.core.point.service;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.point.dto.request.CreatePointRequestDto;
import com.spotlightspace.core.point.dto.response.CreatePointResponseDto;
import com.spotlightspace.core.point.dto.response.GetPointResponseDto;
import com.spotlightspace.core.point.repository.PointRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.spotlightspace.common.exception.ErrorCode.USER_NOT_FOUND;
import static com.spotlightspace.core.data.UserTestData.testAuthUser;
import static com.spotlightspace.core.data.UserTestData.testUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    private PointRepository pointRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private PointService pointService;

    @Test
    @DisplayName("포인트가 없을 때 새로운 데이터를 가지고 생성한다.")
    void createNewPoint() {
        // given
        User user = testUser();
        CreatePointRequestDto requestDto = CreatePointRequestDto.of(10000);
        int amount = (int) (requestDto.getPrice() * 0.005);

        Point point = Point.of(amount, user);

        given(pointRepository.findByUser(user)).willReturn(Optional.empty());
        given(pointRepository.save(any(Point.class))).willReturn(point);

        // when
        CreatePointResponseDto responseDto = pointService.createPoint(requestDto.getPrice(), user);

        // then
        assertNotNull(responseDto);
        assertEquals(responseDto.getAmount(), amount);
    }

    @Test
    @DisplayName("포인트가 있을 때 기존 데이터에 포인트를 추가한다.")
    void addPointsWhenPointExists() {
        // given
        User user = testUser();

        int initPoint = 100;
        int addedPoint = 50; // 추가할 포인트
        Point existingPoint = Point.of(initPoint, user);

        existingPoint.addPoint(addedPoint);

        CreatePointRequestDto requestDto = CreatePointRequestDto.of(addedPoint);

        given(pointRepository.findByUser(user)).willReturn(Optional.of(existingPoint));
        given(pointRepository.save(existingPoint)).willReturn(existingPoint);

        // when
        CreatePointResponseDto responseDto = pointService.createPoint(requestDto.getPrice(), user);

        // then
        assertNotNull(responseDto);
        assertEquals(responseDto.getAmount(), initPoint + addedPoint);
    }

    @Test
    @DisplayName("유저의 포인트 정보를 조회한다.")
    void getPointReturnsUserPoints() {
        // given
        AuthUser authUser = testAuthUser();
        User user = testUser();
        ReflectionTestUtils.setField(user, "id", authUser.getUserId());

        int existPoint = 100;
        Point existingPoint = Point.of(existPoint, user);

        given(userRepository.findByIdOrElseThrow(authUser.getUserId())).willReturn(user);
        given(pointRepository.findByUserOrElseThrow(user)).willReturn(existingPoint);

        // when
        GetPointResponseDto responseDto = pointService.getPoint(authUser);

        // then
        assertNotNull(responseDto);
        assertEquals(existPoint, responseDto.getAmount());
    }

    @Test
    @DisplayName("유저가 존재하지 않을 경우 예외를 발생시킨다.")
    void getPointThrowsExceptionWhenUserNotFound() {
        // given
        AuthUser authUser = testAuthUser();

        given(userRepository.findByIdOrElseThrow(authUser.getUserId()))
                .willThrow(new ApplicationException(USER_NOT_FOUND));

        // when
        ApplicationException exception = assertThrows(ApplicationException.class, () -> pointService.getPoint(authUser));

        // then
        assertEquals("존재하지 않는 유저입니다.", exception.getMessage());
    }
}
