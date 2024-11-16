package com.spotlightspace.core.calculation.repository;

import com.spotlightspace.core.calculation.domain.Calculation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalculationRepository extends JpaRepository<Calculation, Long> {

}
