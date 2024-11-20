package com.spotlightspace.core.ticket.repository;

import com.spotlightspace.core.user.dto.response.GetSettlementListResponseDto;
import java.util.List;

public interface TicketQueryRepository {

    List<GetSettlementListResponseDto> findTotalAmountGroupedByEvent(Long userId);

}
