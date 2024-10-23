package com.spotlightspace.core.point.repository;

import com.spotlightspace.core.point.domain.Point;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long>, PointQueryRepository {

}
