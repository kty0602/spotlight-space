package com.spotlightspace.core.point.service;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.auth.dto.SignupUserRequestDto;
import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.point.dto.CreatePointRequestDto;
import com.spotlightspace.core.point.dto.CreatePointResponseDto;
import com.spotlightspace.core.point.repository.PointRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.domain.UserRole;
import com.spotlightspace.core.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    private PointRepository pointRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private PointService pointService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("포인트가 없을 때 새로운 데이터를 가지고 생성한다.")
    public void createNewPoint() {
        // given
        AuthUser authUser = new AuthUser(1L, "test1@example.com", UserRole.ROLE_USER);
        User user = createUser();
        CreatePointRequestDto requestDto = new CreatePointRequestDto(10000);

        given(userRepository.findByIdOrElseThrow(authUser.getUserId())).willReturn(user);
        given(pointRepository.findByUser(user)).willReturn(Optional.empty());

        // when
        CreatePointResponseDto responseDto = pointService.createPoint(requestDto, authUser);

        // then
        assertNotNull(responseDto);
        assertEquals(responseDto.getAmount(), (int) (10000 * 0.005));
    }

    private User createUser() {
        SignupUserRequestDto requestDto = new SignupUserRequestDto(
                "test1@email.com",
                "rawPassword",
                "test1",
                "ROLE_USER",
                "1999-05-12",
                "010-1234-1234"
        );
        return userRepository.save(User.of("encryptPassword", requestDto));
    }




}
