package com.spotlightspace.core.pointhistory.repository;

import com.spotlightspace.core.pointhistory.domain.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

}
