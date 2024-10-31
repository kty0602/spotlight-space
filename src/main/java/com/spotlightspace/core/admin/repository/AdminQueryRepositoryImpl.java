package com.spotlightspace.core.admin.repository;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spotlightspace.core.admin.dto.responsedto.AdminEventResponseDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.spotlightspace.core.event.domain.QEvent.event;
import static com.spotlightspace.core.user.domain.QUser.user;

@Repository
@RequiredArgsConstructor
public class AdminQueryRepositoryImpl implements AdminQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminUserResponseDto> getAdminUsers(String keyword, Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            PathBuilder<Object> pathBuilder = new PathBuilder<>(user.getType(), user.getMetadata());
            orderSpecifiers.add(new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(order.getProperty())));
        });

        List<AdminUserResponseDto> results = queryFactory
                .select(
                        Projections.constructor(
                                AdminUserResponseDto.class,
                                user.id,
                                user.email,
                                user.nickname,
                                user.phoneNumber,
                                user.role.stringValue(),
                                user.isDeleted
                        )
                )
                .from(user)
                .where(keywordContainsForUser(keyword))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0])) // 정렬 적용
                .fetch();

        long totalCount = queryFactory
                .select(Wildcard.count)
                .from(user)
                .where(keywordContainsForUser(keyword))
                .fetchOne();

        return new PageImpl<>(results, pageable, totalCount);
    }

    @Override
    public Page<AdminEventResponseDto> getAdminEvents(String keyword, Pageable pageable) {
        // 정렬을 위한 OrderSpecifier 목록 생성
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            PathBuilder<Object> pathBuilder = new PathBuilder<>(event.getType(), event.getMetadata());
            orderSpecifiers.add(new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(order.getProperty())));
        });

        // 쿼리 실행 및 페이징 적용
        List<AdminEventResponseDto> results = queryFactory
                .select(
                        Projections.constructor(
                                AdminEventResponseDto.class,
                                event.id,
                                event.title,
                                event.content,
                                event.location,
                                event.startAt,
                                event.endAt,
                                event.maxPeople,
                                event.price,
                                event.category.stringValue(),
                                event.recruitmentStartAt,
                                event.recruitmentFinishAt,
                                event.isDeleted
                        )
                )
                .from(event)
                .where(keywordContainsForEvent(keyword)) // 검색어 조건 추가
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0])) // 정렬 조건 적용
                .fetch();

        long totalCount = queryFactory
                .select(Wildcard.count)
                .from(event)
                .where(keywordContainsForEvent(keyword)) // 검색어 조건 추가
                .fetchOne();

        return new PageImpl<>(results, pageable, totalCount);
    }


    // 유저 검색용 키워드 조건 추가 메서드
    private BooleanExpression keywordContainsForUser(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return user.nickname.startsWith(keyword)
                .or(user.email.startsWith(keyword))
                .or(user.phoneNumber.startsWith(keyword))
                .or(user.role.stringValue().startsWith(keyword));
    }

    // 이벤트 검색용 키워드 조건 추가 메서드
    private BooleanExpression keywordContainsForEvent(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return event.title.startsWith(keyword)
                .or(event.content.startsWith(keyword))
                .or(event.location.startsWith(keyword));
    }
}
