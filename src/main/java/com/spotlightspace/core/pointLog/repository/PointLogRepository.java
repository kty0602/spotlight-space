package com.spotlightspace.core.pointLog.repository;

import com.spotlightspace.core.pointLog.domain.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {

}
