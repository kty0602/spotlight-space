package com.spotlightspace.core.point.repository;

import static com.spotlightspace.common.exception.ErrorCode.POINT_NOT_FOUND;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long>, PointQueryRepository {

    Optional<Point> findByUser(User user);

    default Point findByUserOrElseThrow(User user) {
        return findByUser(user).orElseThrow(() -> new ApplicationException(POINT_NOT_FOUND));
    }
}
