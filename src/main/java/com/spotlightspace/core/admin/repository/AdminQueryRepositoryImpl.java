package com.spotlightspace.core.admin.repository;


import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spotlightspace.core.admin.dto.responsedto.AdminEventResponseDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminReviewResponseDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.spotlightspace.core.event.domain.QEvent.event;
import static com.spotlightspace.core.review.domain.QReview.review;
import static com.spotlightspace.core.user.domain.QUser.user;

@Repository
@RequiredArgsConstructor
public class AdminQueryRepositoryImpl implements AdminQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminUserResponseDto> getAdminUsers(String keyword, Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            PathBuilder<?> entityPath = new PathBuilder<>(user.getType(), user.getMetadata());
            orderSpecifiers.add(new OrderSpecifier(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    entityPath.get(order.getProperty())
            ));
        });

        // 쿼리 실행 및 페이징 적용
        List<Tuple> tuples = queryFactory
                .select(user.id, user.email, user.nickname, user.phoneNumber, user.role.stringValue(), user.isDeleted)
                .from(user)
                .where(keywordContainsForUser(keyword))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .fetch();

        // Tuple 데이터를 DTO로 매핑
        List<AdminUserResponseDto> results = tuples.stream()
                .map(tuple -> AdminUserResponseDto.of(
                        tuple.get(user.id),
                        tuple.get(user.email),
                        tuple.get(user.nickname),
                        tuple.get(user.phoneNumber),
                        tuple.get(user.role.stringValue()),
                        tuple.get(user.isDeleted)))
                .collect(Collectors.toList());

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
            PathBuilder<?> entityPath = new PathBuilder<>(event.getType(), event.getMetadata());
            orderSpecifiers.add(new OrderSpecifier(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    entityPath.get(order.getProperty())
            ));
        });

        // 쿼리 실행 및 페이징 적용
        List<Tuple> tuples = queryFactory
                .select(event.id, event.title, event.content, event.location, event.startAt, event.endAt,
                        event.maxPeople, event.price, event.category.stringValue(), event.recruitmentStartAt,
                        event.recruitmentFinishAt, event.isDeleted)
                .from(event)
                .where(keywordContainsForEvent(keyword)) // 검색어 조건 추가
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0])) // 정렬 조건 적용
                .fetch();

        // Tuple 데이터를 DTO로 매핑
        List<AdminEventResponseDto> results = tuples.stream()
                .map(tuple -> AdminEventResponseDto.from(
                        tuple.get(event.id),
                        tuple.get(event.title),
                        tuple.get(event.content),
                        tuple.get(event.location),
                        tuple.get(event.startAt),
                        tuple.get(event.endAt),
                        tuple.get(event.maxPeople),
                        tuple.get(event.price),
                        tuple.get(event.category.stringValue()),
                        tuple.get(event.recruitmentStartAt),
                        tuple.get(event.recruitmentFinishAt),
                        tuple.get(event.isDeleted)))
                .collect(Collectors.toList());

        long totalCount = queryFactory
                .select(Wildcard.count)
                .from(event)
                .where(keywordContainsForEvent(keyword))
                .fetchOne();

        return new PageImpl<>(results, pageable, totalCount);
    }


    @Override
    public Page<AdminReviewResponseDto> getAdminReviews(String keyword, Pageable pageable) {
        // 정렬을 위한 OrderSpecifier 목록 생성
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            PathBuilder<?> entityPath = new PathBuilder<>(review.getType(), review.getMetadata());
            orderSpecifiers.add(new OrderSpecifier(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    entityPath.get(order.getProperty())
            ));
        });

        // 쿼리 실행 및 페이징 적용
        List<Tuple> tuples = queryFactory
                .select(review.id, review.event.title, review.user.nickname, review.contents, review.rating, review.isDeleted)
                .from(review)
                .where(keywordContainsForReview(keyword)) // 검색어 조건 추가
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0])) // 정렬 조건 적용
                .fetch();

        // Tuple 데이터를 DTO로 매핑
        List<AdminReviewResponseDto> results = tuples.stream()
                .map(tuple -> AdminReviewResponseDto.of(
                        tuple.get(review.id),
                        tuple.get(review.event.title),
                        tuple.get(review.user.nickname),
                        tuple.get(review.contents),
                        tuple.get(review.rating),
                        tuple.get(review.isDeleted)))
                .collect(Collectors.toList());

        long totalCount = queryFactory
                .select(Wildcard.count)
                .from(review)
                .where(keywordContainsForReview(keyword)) // 검색어 조건 추가
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

    // 리뷰 검색용 키워드 조건 추가 메서드
    private BooleanExpression keywordContainsForReview(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return review.contents.startsWith(keyword)
                .or(review.user.nickname.startsWith(keyword));
    }

}

