package com.spotlightspace.core.event.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spotlightspace.core.event.domain.QEvent;
import com.spotlightspace.core.event.dto.response.GetEventResponseDto;
import com.spotlightspace.core.event.dto.request.SearchEventRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EventQueryRepositoryImpl implements EventQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<GetEventResponseDto> searchEvents(SearchEventRequestDto requestDto, String type, Pageable pageable) {
        QEvent event = QEvent.event;
        LocalDateTime now = LocalDateTime.now();

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(event.isDeleted.eq(false));

        // goe -> >=
        // loe -> <=
        // lt -> <
        // gt -> >
        if (requestDto.getTitle() != null) {
            builder.and(event.title.startsWith(requestDto.getTitle()));
        }
        if (requestDto.getMaxPeople() != null) {
            builder.and(event.maxPeople.loe(requestDto.getMaxPeople()));
        }
        if (requestDto.getLocation() != null) {
            builder.and(event.location.startsWith(requestDto.getLocation()));
        }
        if (requestDto.getCategory() != null) {
            builder.and(event.category.eq(requestDto.getCategory()));
        }
        if (requestDto.getRecruitmentStartAt() != null) {
            builder.and(event.recruitmentStartAt.goe(requestDto.getRecruitmentStartAt()));
        }
        if (requestDto.getRecruitmentFinishAt() != null) {
            builder.and(event.recruitmentFinishAt.lt(requestDto.getRecruitmentFinishAt()));
        }

        // 정렬 모집기간 정렬 임박 순, 낮은, 높은 가격 순
        OrderSpecifier<?> orderSpecifier;
        switch (type) {
            case "upprice": // 가격 오름차순
                orderSpecifier = event.price.asc();
                break;
            case "downprice": // 가격 내림차순
                orderSpecifier = event.price.desc();
                break;
            case "date":
                builder.and(event.recruitmentFinishAt.goe(now));
                orderSpecifier = event.recruitmentFinishAt.asc();
                break;
            default:
                orderSpecifier = event.id.desc();
                break;
        }

        List<GetEventResponseDto> results = jpaQueryFactory
                .selectFrom(event)
                .where(builder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream()
                .map(GetEventResponseDto::from)
                .collect(Collectors.toList());

        long total = jpaQueryFactory
                .select(event.count())
                .from(event)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, total);
    }
}
