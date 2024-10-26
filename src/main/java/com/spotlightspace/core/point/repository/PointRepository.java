package com.spotlightspace.core.point.repository;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static com.spotlightspace.common.exception.ErrorCode.POINT_NOT_FOUND;

public interface PointRepository extends JpaRepository<Point, Long>, PointQueryRepository {

    Optional<Point> findByUser(User user);

    default Point findByUserIdOrElseThrow(User user) {
        return findByUser(user)
                .orElseThrow(() -> new ApplicationException(POINT_NOT_FOUND));
    }
}
