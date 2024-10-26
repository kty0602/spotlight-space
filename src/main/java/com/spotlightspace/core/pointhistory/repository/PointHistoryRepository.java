package com.spotlightspace.core.pointhistory.repository;

import static com.spotlightspace.common.exception.ErrorCode.POINT_HISTORY_NOT_FOUND;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.pointhistory.domain.PointHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    void deleteByPoint(Point point);

    Optional<PointHistory> findByPoint(Point point);

    default PointHistory findByPointOrElseThrow(Point point) {
        return findByPoint(point).orElseThrow(() -> new ApplicationException(POINT_HISTORY_NOT_FOUND));
    }
}
