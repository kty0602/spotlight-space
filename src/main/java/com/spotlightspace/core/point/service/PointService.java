package com.spotlightspace.core.point.service;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.point.dto.CreatePointRequestDto;
import com.spotlightspace.core.point.dto.CreatePointResponseDto;
import com.spotlightspace.core.point.dto.GetPointResponseDto;
import com.spotlightspace.core.point.repository.PointRepository;
import com.spotlightspace.core.pointhistory.domain.PointHistory;
import com.spotlightspace.core.pointhistory.repository.PointHistoryRepository;
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
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public CreatePointResponseDto createPoint(CreatePointRequestDto requestDto, AuthUser authUser) {

        // 맴버 존재하는지 확인
        User user = checkUserData(authUser);

        // 0.5% 포인트 변환
        int rewardPoint = (int) (requestDto.getPrice() * 0.005);
        // 포인트 테이블에 맴버 id값으로 데이터가 존재하는 지 확인
        Point point = pointRepository.findByUser(user).orElseGet(() -> {
            // 포인트 데이터가 없다면 새로 생성
            return pointRepository.save(Point.of(rewardPoint, user));
        });
        // 없다면 새로운 포인트 적립
        if (point != null && point.getId() != null) { // 이미 포인트 데이터가 있는 경우
            point.addPoint(rewardPoint);
        }
        return CreatePointResponseDto.from(point);
    }

    public GetPointResponseDto getPoint(AuthUser authUser) {

        // 맴버 존재하는지 확인
        User user = checkUserData(authUser);

        // 해당 회원이 포인트 정보를 가지고 있는지 확인
        Point point = pointRepository.findByUserOrElseThrow(user);

        return GetPointResponseDto.from(point);
    }


    private User checkUserData(AuthUser authUser) {

        return userRepository.findByIdOrElseThrow(authUser.getUserId());
    }

    public void cancelPointUsage(Point point) {
        PointHistory pointHistory = pointHistoryRepository.findByPointOrElseThrow(point);
        point.cancelUsage(pointHistory.getAmount());
        pointHistory.cancelPointUsage();
    }
}
