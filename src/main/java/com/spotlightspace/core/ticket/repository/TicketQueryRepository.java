package com.spotlightspace.core.ticket.repository;

import com.spotlightspace.core.user.dto.response.GetCalculateListResponseDto;
import java.util.List;

public interface TicketQueryRepository {

    List<GetCalculateListResponseDto> findTotalAmountGroupedByEvent(Long userId);

}
