package com.spotlightspace.core.pointhistory.service;

import com.spotlightspace.core.payment.domain.Payment;
import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.pointhistory.domain.PointHistory;
import com.spotlightspace.core.pointhistory.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;

    public void createPointHistory(Payment payment, Point point, int amount) {
        PointHistory pointHistory = PointHistory.create(payment, point, amount);
        pointHistoryRepository.save(pointHistory);
    }
}
