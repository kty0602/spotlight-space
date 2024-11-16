package com.spotlightspace.core.point.service;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.point.dto.request.CreatePointRequestDto;
import com.spotlightspace.core.point.dto.response.CreatePointResponseDto;
import com.spotlightspace.core.point.dto.response.GetPointResponseDto;
import com.spotlightspace.core.point.repository.PointRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreatePointResponseDto createPoint(int price, User user) {

        // 0.5% 포인트 변환
        int rewardPoint = (int) (price * 0.005);
        // 포인트 테이블에 맴버 id값으로 데이터가 존재하는 지 확인
        Point point = pointRepository.findByUser(user).orElse(null);

        // 포인트 데이터가 없다면 새로 생성
        if (point == null) {
            point = Point.of(rewardPoint, user);
        }
        if (point != null) {
            // 이미 포인트 데이터가 있는 경우
            point.addPoint(rewardPoint);
        }

        Point savePoint = pointRepository.save(point);
        return CreatePointResponseDto.from(savePoint);
    }

    public GetPointResponseDto getPoint(AuthUser authUser) {

        // 맴버 존재하는지 확인
        User user = checkUserData(authUser.getUserId());

        // 해당 회원이 포인트 정보를 가지고 있는지 확인
        Point point = pointRepository.findByUserOrElseThrow(user);

        return GetPointResponseDto.from(point);
    }


    private User checkUserData(Long userId) {

        return userRepository.findByIdOrElseThrow(userId);
    }

    public void deleteUserPoint(Long userId) {
        pointRepository.deleteByUserId(userId);
    }
}