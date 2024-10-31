package com.spotlightspace.core.ticket.repository;

import static com.spotlightspace.core.event.domain.QEvent.event;
import static com.spotlightspace.core.ticket.domain.QTicket.ticket;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spotlightspace.core.user.dto.response.GetCalculateListResponseDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TicketQueryRepositoryImpl implements TicketQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<GetCalculateListResponseDto> findTotalAmountGroupedByEvent(Long userId) {
        return jpaQueryFactory
                .select(event.title, ticket.price.sum())
                .from(ticket)
                .join(ticket.event, event)
                .where(event.user.id.eq(userId)
                        .and(ticket.isCanceled.isFalse())
                        .and(event.isCalculated.isFalse()))
                .groupBy(event.id)
                .fetch()
                .stream()
                .map(tuple -> GetCalculateListResponseDto.of(
                        tuple.get(event.title),
                        tuple.get(ticket.price.sum()).intValue()))
                .collect(Collectors.toList());
    }
}
