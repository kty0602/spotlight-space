package com.spotlightspace.core.admin.repository;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

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

    // 키워드 검색용 메서드들 수정
    private BooleanExpression keywordContainsForUser(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return user.nickname.startsWith(keyword)
                .or(user.email.startsWith(keyword))
                .or(user.phoneNumber.startsWith(keyword))
                .or(user.role.stringValue().startsWith(keyword));
    }
}