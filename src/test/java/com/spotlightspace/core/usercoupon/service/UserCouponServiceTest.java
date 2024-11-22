package com.spotlightspace.core.usercoupon.service;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.coupon.domain.Coupon;
import com.spotlightspace.core.coupon.repository.CouponRepository;
import com.spotlightspace.core.data.UserCouponTestData;
import com.spotlightspace.core.event.service.RedissonLockService;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.dto.response.GetCouponResponseDto;
import com.spotlightspace.core.user.repository.UserRepository;
import com.spotlightspace.core.usercoupon.domain.UserCoupon;
import com.spotlightspace.core.usercoupon.dto.request.UserCouponIssueRequestDto;
import com.spotlightspace.core.usercoupon.dto.response.UserCouponIssueResponseDto;
import com.spotlightspace.core.usercoupon.repository.UserCouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RLock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.spotlightspace.common.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserCouponServiceTest {


    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedissonLockService redissonLockService;

    @InjectMocks
    private UserCouponService userCouponService;

    private User user;
    private Coupon coupon;
    private AuthUser authUser;
    private UserCouponIssueRequestDto requestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 테스트 데이터 초기화
        user = UserCouponTestData.testUserWithId();
        coupon = UserCouponTestData.testCouponWithId();
        authUser = UserCouponTestData.testAuthUser();

        // requestDto 초기화
        requestDto = UserCouponIssueRequestDto.of(authUser.getUserId(), coupon.getId());
    }

    @Nested
    @DisplayName("쿠폰 발급 테스트")
    class IssueCouponTests {

        @Test
        @DisplayName("쿠폰 발급 성공")
        void issueCoupon_success() {
            // given
            when(userRepository.findByIdOrElseThrow(requestDto.getUserId())).thenReturn(user);
            when(couponRepository.findByIdOrElseThrow(requestDto.getCouponId())).thenReturn(coupon);
            when(userCouponRepository.save(any(UserCoupon.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            UserCouponIssueResponseDto result = userCouponService.issueCouponBasic(requestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(user.getId());
            assertThat(result.getCouponId()).isEqualTo(coupon.getId());
            verify(userRepository, times(1)).findByIdOrElseThrow(requestDto.getUserId());
            verify(couponRepository, times(1)).findByIdOrElseThrow(requestDto.getCouponId());
            verify(userCouponRepository, times(1)).save(any(UserCoupon.class));
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 쿠폰 수량 부족")
        void issueCoupon_couponCountExhausted() {
            // given
            coupon = Coupon.of(5000, LocalDate.now().plusDays(5), 0, "COUPON123"); // 수량 0인 쿠폰 생성
            when(userRepository.findByIdOrElseThrow(authUser.getUserId())).thenReturn(user);
            when(couponRepository.findByIdOrElseThrow(coupon.getId())).thenReturn(coupon);

            // when & then
            UserCouponIssueRequestDto requestDto = UserCouponIssueRequestDto.of(authUser.getUserId(), coupon.getId());
            assertThatThrownBy(() -> userCouponService.issueCouponBasic(requestDto))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(COUPON_COUNT_EXHAUSTED.getMessage());

            verify(userRepository, times(1)).findByIdOrElseThrow(authUser.getUserId());
            verify(couponRepository, times(1)).findByIdOrElseThrow(coupon.getId());
            verify(userCouponRepository, never()).save(any(UserCoupon.class));
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 쿠폰 유효기간 만료")
        void issueCoupon_couponExpired() {
            // given
            coupon = Coupon.of(5000, LocalDate.now().minusDays(1), 10, "EXPIRED1234"); // 이미 유효기간이 지난 쿠폰 생성
            when(userRepository.findByIdOrElseThrow(authUser.getUserId())).thenReturn(user);
            when(couponRepository.findByIdOrElseThrow(coupon.getId())).thenReturn(coupon);

            // when & then
            UserCouponIssueRequestDto requestDto = UserCouponIssueRequestDto.of(authUser.getUserId(), coupon.getId());
            assertThatThrownBy(() -> userCouponService.issueCouponBasic(requestDto))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(COUPON_EXPIRED.getMessage()); // 쿠폰 만료 메시지로 변경

            verify(userRepository, times(1)).findByIdOrElseThrow(authUser.getUserId());
            verify(couponRepository, times(1)).findByIdOrElseThrow(coupon.getId());
            verify(userCouponRepository, never()).save(any(UserCoupon.class));
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 쿠폰이 삭제됨")
        void issueCoupon_couponDeleted() {
            // given
            coupon.setAsUnusable(); // 쿠폰을 삭제된 상태로 만듦
            when(userRepository.findByIdOrElseThrow(authUser.getUserId())).thenReturn(user);
            when(couponRepository.findByIdOrElseThrow(coupon.getId())).thenReturn(coupon);

            // when & then
            UserCouponIssueRequestDto requestDto = UserCouponIssueRequestDto.of(authUser.getUserId(), coupon.getId());
            assertThatThrownBy(() -> userCouponService.issueCouponBasic(requestDto))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(COUPON_NOT_FOUND.getMessage()); // 삭제된 쿠폰 메시지로 변경

            verify(userRepository, times(1)).findByIdOrElseThrow(authUser.getUserId());
            verify(couponRepository, times(1)).findByIdOrElseThrow(coupon.getId());
            verify(userCouponRepository, never()).save(any(UserCoupon.class));
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 존재하지 않는 쿠폰")
        void issueCoupon_couponNotFound() {
            // given
            when(userRepository.findByIdOrElseThrow(authUser.getUserId())).thenReturn(user);
            when(couponRepository.findByIdOrElseThrow(coupon.getId()))
                    .thenThrow(new ApplicationException(COUPON_NOT_FOUND)); // 존재하지 않는 쿠폰 예외

            // when & then
            UserCouponIssueRequestDto requestDto = UserCouponIssueRequestDto.of(authUser.getUserId(), coupon.getId());
            assertThatThrownBy(() -> userCouponService.issueCouponBasic(requestDto))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(COUPON_NOT_FOUND.getMessage()); // 존재하지 않는 쿠폰 메시지로 변경

            verify(userRepository, times(1)).findByIdOrElseThrow(authUser.getUserId());
            verify(couponRepository, times(1)).findByIdOrElseThrow(coupon.getId());
            verify(userCouponRepository, never()).save(any(UserCoupon.class));
        }

        @Test
        @DisplayName("사용자가 이미 쿠폰을 가지고 있을 경우 예외 발생")
        void validateUserHasCoupon_alreadyHasCoupon() {
            // given
            User user = UserCouponTestData.testUserWithId();
            Coupon coupon = UserCouponTestData.testCouponWithId();
            when(userRepository.findByIdOrElseThrow(anyLong())).thenReturn(user);
            when(couponRepository.findByIdOrElseThrow(anyLong())).thenReturn(coupon);
            when(userCouponRepository.existsByUserAndCoupon(user, coupon)).thenReturn(true);

            // when & then
            UserCouponIssueRequestDto requestDto = UserCouponIssueRequestDto.of(user.getId(), coupon.getId());
            assertThatThrownBy(() -> userCouponService.issueCouponBasic(requestDto))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(COUPON_ALREADY_ISSUED.getMessage()); // 예외 메시지를 이미 발급된 쿠폰으로 변경

            verify(userCouponRepository, times(1)).existsByUserAndCoupon(user, coupon);
        }

        @Test
        @DisplayName("비관적 락을 이용한 쿠폰 발급 성공")
        void issueCouponWithPessimisticLock_success() {
            // given
            RLock redisLock = mock(RLock.class);
            when(redissonLockService.lock(anyString())).thenReturn(redisLock);
            try {
                when(redisLock.tryLock(3, 5, TimeUnit.SECONDS)).thenReturn(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            when(userRepository.findByIdOrElseThrow(requestDto.getUserId())).thenReturn(user);
            when(couponRepository.findByIdWithPessimisticLock(requestDto.getCouponId()))
                    .thenReturn(Optional.of(coupon));
            when(userCouponRepository.save(any(UserCoupon.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            UserCouponIssueResponseDto result = userCouponService.issueCouponWithPessimisticLockAndQueue(requestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(user.getId());
            assertThat(result.getCouponId()).isEqualTo(coupon.getId());
            verify(userRepository, times(1)).findByIdOrElseThrow(requestDto.getUserId());
            when(couponRepository.findByIdWithPessimisticLock(requestDto.getCouponId()))
                    .thenReturn(Optional.of(coupon));
            verify(userCouponRepository, times(1)).save(any(UserCoupon.class));
        }

        @Test
        @DisplayName("비관적 락을 이용한 쿠폰 발급 - 락 획득 실패")
        void issueCouponWithPessimisticLock_lockNotAcquired() throws InterruptedException {
            // given
            RLock redisLock = mock(RLock.class);
            when(redissonLockService.lock(anyString())).thenReturn(redisLock);
            when(redisLock.tryLock(3, 5, TimeUnit.SECONDS)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> userCouponService.issueCouponWithPessimisticLockAndQueue(requestDto))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(LOCK_NOT_ACQUIRED.getMessage());

            verify(redissonLockService, times(1)).lock(anyString());
            verify(redisLock, times(1)).tryLock(3, 5, TimeUnit.SECONDS);
            when(couponRepository.findByIdWithPessimisticLock(requestDto.getCouponId()))
                    .thenReturn(Optional.of(coupon));
            verify(userCouponRepository, never()).save(any(UserCoupon.class));
        }

        @Test
        @DisplayName("비관적 락을 이용한 쿠폰 발급 - 동시 요청 처리")
        void issueCouponWithPessimisticLock_concurrentRequests() throws InterruptedException {
            // given
            RLock redisLock = mock(RLock.class);
            when(redissonLockService.lock(anyString())).thenReturn(redisLock);
            try {
                when(redisLock.tryLock(3, 5, TimeUnit.SECONDS)).thenReturn(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            when(userRepository.findByIdOrElseThrow(authUser.getUserId())).thenReturn(user);
            when(couponRepository.findByIdWithPessimisticLock(requestDto.getCouponId()))
                    .thenReturn(Optional.of(coupon));
            when(userCouponRepository.save(any(UserCoupon.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // 동시 요청을 테스트하기 위한 스레드 작업
            Runnable requestTask = () -> {
                try {
                    UserCouponIssueRequestDto requestDto = UserCouponIssueRequestDto.of(authUser.getUserId(), coupon.getId());
                    userCouponService.issueCouponWithPessimisticLockAndQueue(requestDto);
                } catch (ApplicationException e) {
                    System.out.println("Exception caught in thread: " + e.getMessage());
                }
            };

            // 여러 스레드를 실행하여 동시 요청을 시뮬레이션
            Thread thread1 = new Thread(requestTask);
            Thread thread2 = new Thread(requestTask);
            Thread thread3 = new Thread(requestTask);

            thread1.start();
            thread2.start();
            thread3.start();

            thread1.join();
            thread2.join();
            thread3.join();

            // 동시성 테스트 검증
            verify(redissonLockService, times(3)).lock(anyString());
            verify(redisLock, times(3)).tryLock(3, 5, TimeUnit.SECONDS);
            when(couponRepository.findByIdWithPessimisticLock(requestDto.getCouponId()))
                    .thenReturn(Optional.of(coupon));
            verify(userCouponRepository, atMost(3)).save(any(UserCoupon.class));
        }
    }

    @Test
    @DisplayName("비관적 락 획득 실패 시 예외 발생")
    void issueCouponWithPessimisticLock_lockNotAcquiredDueToInterruptedException() throws InterruptedException {
        // given
        RLock redisLock = mock(RLock.class);
        when(redissonLockService.lock(anyString())).thenReturn(redisLock);
        when(redisLock.tryLock(3, 5, TimeUnit.SECONDS)).thenThrow(InterruptedException.class);

        // when & then
        assertThatThrownBy(() -> userCouponService.issueCouponWithPessimisticLockAndQueue(requestDto))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(LOCK_NOT_ACQUIRED.getMessage());

        verify(redissonLockService, times(1)).lock(anyString());
    }

    @Test
    @DisplayName("비관적 락 해제 확인")
    void issueCouponWithPessimisticLock_lockReleaseTest() throws InterruptedException {
        // given
        RLock redisLock = mock(RLock.class);
        when(redissonLockService.lock(anyString())).thenReturn(redisLock);
        when(redisLock.tryLock(3, 5, TimeUnit.SECONDS)).thenReturn(true);
        when(redisLock.isLocked()).thenReturn(true);
        when(redisLock.isHeldByCurrentThread()).thenReturn(true); // 추가된 부분

        when(userRepository.findByIdOrElseThrow(requestDto.getUserId())).thenReturn(user);
        when(couponRepository.findByIdWithPessimisticLock(requestDto.getCouponId())).thenReturn(Optional.of(coupon));
        when(userCouponRepository.save(any(UserCoupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        userCouponService.issueCouponWithPessimisticLockAndQueue(requestDto);

        // then
        verify(redisLock, times(1)).unlock();
    }

    @Test
    @DisplayName("사용자의 쿠폰 조회 성공 - 여러 개의 쿠폰 조회")
    void getUserCoupons_success() {
        // given
        List<UserCoupon> userCoupons = UserCouponTestData.testUserCouponsWithId(user);
        when(userCouponRepository.findAllByUserId(user.getId())).thenReturn(userCoupons);

        // when
        List<GetCouponResponseDto> result = userCouponService.getUserCouponByUserId(user.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(userCoupons.size());
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getId()).isEqualTo(userCoupons.get(i).getId()); // 필드명에 맞게 수정
        }
        verify(userCouponRepository, times(1)).findAllByUserId(user.getId());
    }
}
