package com.spotlightspace.core.admin.repository;

import static com.spotlightspace.core.coupon.domain.QCoupon.coupon;
import static com.spotlightspace.core.event.domain.QEvent.event;
import static com.spotlightspace.core.review.domain.QReview.review;
import static com.spotlightspace.core.user.domain.QUser.user;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spotlightspace.core.admin.dto.requestdto.SearchAdminUserRequestDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminCouponResponseDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminEventResponseDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminReviewResponseDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminUserResponseDto;
import com.spotlightspace.core.coupon.domain.Coupon;
import com.spotlightspace.core.coupon.domain.QCoupon;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.domain.QEvent;
import com.spotlightspace.core.review.domain.QReview;
import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.user.domain.QUser;
import com.spotlightspace.core.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminQueryRepositoryImpl implements AdminQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminUserResponseDto> getAdminUsers(SearchAdminUserRequestDto searchAdminUserRequestDto,
            Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            PathBuilder<?> entityPath = new PathBuilder<>(user.getType(), user.getMetadata());
            orderSpecifiers.add(new OrderSpecifier(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    entityPath.get(order.getProperty())
            ));
        });

        BooleanExpression whereClause = createWhereClause(searchAdminUserRequestDto);

        // 쿼리 실행 및 페이징 적용
        List<Tuple> tuples = queryFactory
                .select(user.id, user.email, user.nickname, user.phoneNumber, user.role.stringValue(), user.isDeleted)
                .from(user)
                .where(whereClause)
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
                .where(whereClause)
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
                .map(tuple -> AdminEventResponseDto.of(
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
                .select(review.id, review.event.title, review.user.nickname, review.contents, review.rating,
                        review.isDeleted)
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

    @Override
    public Page<AdminCouponResponseDto> getAdminCoupons(String keyword, Pageable pageable) {
        QCoupon coupon = QCoupon.coupon;

        // 정렬을 위한 OrderSpecifier 목록 생성
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        // pageable 객체로부터 정렬 정보를 가져와 각 필드에 대해 정렬 조건을 설정
        pageable.getSort().forEach(order -> {
            // PathBuilder를 사용해 정렬할 엔티티의 필드를 설정
            // coupon.getType()과 coupon.getMetadata()를 통해 쿠폰 엔티티의 필드에 접근
            PathBuilder<?> entityPath = new PathBuilder<>(coupon.getType(), coupon.getMetadata());

            // OrderSpecifier를 생성하여 정렬 순서를 지정 (오름차순 또는 내림차순)
            // order.isAscending()이 참이면 오름차순, 거짓이면 내림차순으로 정렬
            orderSpecifiers.add(new OrderSpecifier(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    entityPath.get(order.getProperty()) // 정렬할 필드명을 가져와 정렬 조건에 사용
            ));
        });

        // 쿼리 실행 및 페이징 적용
        //여기서 Tuple은 여러 컬럼 값을 한 번에 가져오는 역할
        List<Tuple> tuples = queryFactory
                .select(
                        coupon.id, // coupon 테이블의 id 필드
                        coupon.discountAmount, // 할인 금액
                        coupon.expiredAt, // 쿠폰의 만료일
                        coupon.code, // 쿠폰 코드 값
                        coupon.isDeleted // 쿠폰 삭제 여부
                )
                .from(coupon)
                .where(keywordContainsForCoupon(keyword)) // 검색어 조건 추가
                .offset(pageable.getOffset())  // 페이징을 위해 시작 위치 설정
                .limit(pageable.getPageSize())  // 페이징을 위해 몇 개의 항목을 가져올지 설정
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0])) // 정렬 조건 적용
                .fetch();  // 쿼리 실행

        // Tuple 데이터를 DTO로 매핑
        List<AdminCouponResponseDto> results = tuples.stream()
                .map(tuple -> AdminCouponResponseDto.of(
                        // 튜플로 받아온 데이터를 DTO 필드에 매핑
                        tuple.get(coupon.id),
                        tuple.get(coupon.discountAmount),
                        tuple.get(coupon.expiredAt),
                        tuple.get(coupon.code),
                        tuple.get(coupon.isDeleted)
                ))
                .collect(Collectors.toList());

        long totalCount = queryFactory
                .select(Wildcard.count)
                .from(coupon)
                .where(keywordContainsForCoupon(keyword)) // 검색어 조건 추가
                .fetchOne();

        return new PageImpl<>(results, pageable, totalCount);  // 결과를 페이지로 감싸서 반환
    }


    // 유저 검색용 키워드 조건 추가 메서드
    private BooleanExpression createWhereClause(SearchAdminUserRequestDto searchAdminUserRequestDto) {
        BooleanExpression predicate = Expressions.asBoolean(true).isTrue();

        if (searchAdminUserRequestDto.getNickname() != null && !searchAdminUserRequestDto.getNickname().trim()
                .isEmpty()) {
            predicate = predicate.and(user.nickname.startsWith(searchAdminUserRequestDto.getNickname()));
        }
        if (searchAdminUserRequestDto.getEmail() != null && !searchAdminUserRequestDto.getEmail().trim().isEmpty()) {
            predicate = predicate.and(user.email.startsWith(searchAdminUserRequestDto.getEmail()));
        }
        if (searchAdminUserRequestDto.getPhoneNumber() != null && !searchAdminUserRequestDto.getPhoneNumber().trim()
                .isEmpty()) {
            predicate = predicate.and(user.phoneNumber.startsWith(searchAdminUserRequestDto.getPhoneNumber()));
        }
        if (searchAdminUserRequestDto.getRole() != null && !searchAdminUserRequestDto.getRole().trim().isEmpty()) {
            predicate = predicate.and(user.role.stringValue().startsWith(searchAdminUserRequestDto.getRole()));
        }
        if (searchAdminUserRequestDto.getLocation() != null && !searchAdminUserRequestDto.getLocation().trim()
                .isEmpty()) {
            predicate = predicate.and(user.location.startsWith(searchAdminUserRequestDto.getLocation()));
        }
        if (searchAdminUserRequestDto.isSocialLogin()) {
            predicate = predicate.and(user.isSocialLogin.eq(true));
        }
        if (searchAdminUserRequestDto.isDeleted()) {
            predicate = predicate.and(user.isDeleted.eq(true));
        }
        if (searchAdminUserRequestDto.getBirth() != null && !searchAdminUserRequestDto.getBirth().trim().isEmpty()) {
            predicate = predicate.and(user.birth.stringValue().startsWith(searchAdminUserRequestDto.getBirth()));
        }

        return predicate;
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

    private BooleanExpression keywordContainsForCoupon(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return coupon.code.startsWith(keyword)
                .or(coupon.discountAmount.stringValue().startsWith(keyword));
    }

    @Override
    public Optional<User> findUserById(Long id) {
        User user = queryFactory
                .selectFrom(QUser.user)
                .where(QUser.user.id.eq(id))
                .fetchOne();
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<Event> findEventById(Long id) {
        Event event = queryFactory
                .selectFrom(QEvent.event)
                .where(QEvent.event.id.eq(id))
                .fetchOne();
        return Optional.ofNullable(event);
    }

    @Override
    public Optional<Review> findReviewById(Long id) {
        Review review = queryFactory
                .selectFrom(QReview.review)
                .where(QReview.review.id.eq(id))
                .fetchOne();
        return Optional.ofNullable(review);
    }

    @Override
    public void saveCoupon(Coupon coupon) {
        // 쿠폰 저장 로직 구현 (JPA 사용)
    }

    @Override
    public Optional<Coupon> findCouponById(Long id) {
        Coupon coupon = queryFactory.selectFrom(QCoupon.coupon)
                .where(QCoupon.coupon.id.eq(id).and(QCoupon.coupon.isDeleted.isFalse()))
                .fetchOne();
        return Optional.ofNullable(coupon);
    }

    @Override
    public List<Tuple> findCoupons(String keyword, Pageable pageable) {
        BooleanExpression keywordCondition =
                keyword != null && !keyword.isEmpty() ? QCoupon.coupon.code.containsIgnoreCase(keyword) : null;

        return queryFactory.select(
                        QCoupon.coupon.id,
                        QCoupon.coupon.discountAmount,
                        QCoupon.coupon.expiredAt,
                        QCoupon.coupon.code,
                        QCoupon.coupon.isDeleted
                )
                .from(QCoupon.coupon)
                .where(keywordCondition, QCoupon.coupon.isDeleted.isFalse())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public long countCoupons(String keyword) {
        BooleanExpression keywordCondition =
                keyword != null && !keyword.isEmpty() ? QCoupon.coupon.code.containsIgnoreCase(keyword) : null;

        return queryFactory.select(Wildcard.count)
                .from(QCoupon.coupon)
                .where(keywordCondition, QCoupon.coupon.isDeleted.isFalse())
                .fetchOne();
    }


}

