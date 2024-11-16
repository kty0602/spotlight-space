package com.spotlightspace.core.payment.service;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyInt;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.spotlightspace.core.data.UserTestData;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.domain.EventCategory;
import com.spotlightspace.core.event.dto.request.CreateEventRequestDto;
import com.spotlightspace.core.payment.domain.Payment;
import com.spotlightspace.core.payment.dto.response.PaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayApprovePaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayCancelPaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayPaymentErrorResponseDto;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayPaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.kakaopay.KakaopayReadyPaymentResponseDto;
import com.spotlightspace.core.paymentevent.domain.PaymentEvent;
import com.spotlightspace.core.paymentevent.repository.PaymentEventRepository;
import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.integration.kakaopay.KakaopayApi;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceFacadeTest {

    @Mock
    PaymentService paymentService;

    @Mock
    KakaopayApi kakaopayApi;

    @Mock
    PaymentEventRepository paymentEventRepository;

    @InjectMocks
    PaymentServiceFacade paymentServiceFacade;

    @Nested
    @DisplayName("결제 준비 시")
    class ReadyPayment {

        @Test
        @DisplayName("카카오페이 api 응답이 success이면 결제 준비가 된다.")
        void readyPaymentWithSuccess() {
            // given
            CreateEventRequestDto createEventRequestDto = getCreateEventRequestDtoWithMaxPeople(10);
            given(paymentService.createPayment(anyLong(), anyLong(), anyString(), anyLong(), anyInt()))
                    .willReturn(1L);
            given(paymentService.getPayment(anyLong()))
                    .willReturn(PaymentResponseDto.from(getPendingPayment(
                            getEvent(createEventRequestDto),
                            getUser(),
                            0,
                            null))
                    );
            given(paymentEventRepository.save(any()))
                    .willReturn(getPaymentEvent());
            given(kakaopayApi.readyPayment(anyString(), anyLong(), anyLong(), anyString(), anyLong(), anyInt()))
                    .willReturn(createSuccessKakaopayReadyPaymentResponseDto());

            // when
            paymentServiceFacade.readyPayment(1L, 1L, "cid", 1L, 0);

            // then
            verify(paymentService, times(1)).createPayment(anyLong(), anyLong(), anyString(), anyLong(), anyInt());
            verify(paymentService, times(1)).getPayment(anyLong());
            verify(paymentEventRepository, times(1)).save(any());
            verify(kakaopayApi, times(1)).readyPayment(anyString(), anyLong(), anyLong(), anyString(), anyLong(),
                    anyInt());
            verify(paymentService, times(1)).readyPayment(anyLong(), anyString(), anyLong());
            verify(paymentService, never()).failPayment(anyLong(), anyLong());
        }

        @Test
        @DisplayName("카카오페이 api 응답이 fail이면 결제 실패가 된다.")
        void readyPaymentWithFail() {
            // given
            CreateEventRequestDto createEventRequestDto = getCreateEventRequestDtoWithMaxPeople(10);
            given(paymentService.createPayment(anyLong(), anyLong(), anyString(), anyLong(), anyInt()))
                    .willReturn(1L);
            given(paymentService.getPayment(anyLong()))
                    .willReturn(PaymentResponseDto.from(getPendingPayment(
                            getEvent(createEventRequestDto),
                            getUser(),
                            0,
                            null))
                    );
            given(paymentEventRepository.save(any()))
                    .willReturn(getPaymentEvent());
            given(kakaopayApi.readyPayment(anyString(), anyLong(), anyLong(), anyString(), anyLong(), anyInt()))
                    .willReturn(createFailKakaopayPaymentResponseDto());

            // when
            paymentServiceFacade.readyPayment(1L, 1L, "cid", 1L, 0);

            // then
            verify(paymentService, times(1)).createPayment(anyLong(), anyLong(), anyString(), anyLong(), anyInt());
            verify(paymentService, times(1)).getPayment(anyLong());
            verify(paymentEventRepository, times(1)).save(any());
            verify(kakaopayApi, times(1)).readyPayment(anyString(), anyLong(), anyLong(), anyString(), anyLong(),
                    anyInt());
            verify(paymentService, times(1)).failPayment(anyLong(), anyLong());
            verify(paymentService, never()).readyPayment(anyLong(), anyString(), anyLong());
        }
    }

    @Nested
    @DisplayName("결제 승인 시")
    class ApprovePayment {

        @Test
        @DisplayName("카카오페이 api 응답이 success이면 결제 승인이 된다.")
        void approvePaymentWithSuccess() {
            // given
            CreateEventRequestDto createEventRequestDto = getCreateEventRequestDtoWithMaxPeople(10);
            given(paymentService.getPayment(anyString()))
                    .willReturn(PaymentResponseDto.from(getPendingPayment(
                            getEvent(createEventRequestDto),
                            getUser(),
                            0,
                            null))
                    );
            given(paymentEventRepository.save(any()))
                    .willReturn(getPaymentEvent());
            given(kakaopayApi.approvePayment(anyString(), anyString(), anyString(), anyLong(), anyLong()))
                    .willReturn(createSuccessKakaopayApprovePaymentResponseDto());

            // when
            paymentServiceFacade.approvePayment("pgToken", "tid");

            // then
            verify(paymentService, times(1)).getPayment(anyString());
            verify(paymentEventRepository, times(1)).save(any());
            verify(kakaopayApi, times(1)).approvePayment(anyString(), anyString(), anyString(), anyLong(), anyLong());
            verify(paymentService, times(1)).approvePayment(anyLong(), anyLong());
            verify(paymentService, never()).failPayment(anyLong(), anyLong());
        }

        @Test
        @DisplayName("카카오페이 api 응답이 fail이면 결제 승인이 된다.")
        void approvePaymentWithFail() {
            // given
            CreateEventRequestDto createEventRequestDto = getCreateEventRequestDtoWithMaxPeople(10);
            given(paymentService.getPayment(anyString()))
                    .willReturn(PaymentResponseDto.from(getPendingPayment(
                            getEvent(createEventRequestDto),
                            getUser(),
                            0,
                            null))
                    );
            given(paymentEventRepository.save(any()))
                    .willReturn(getPaymentEvent());
            given(kakaopayApi.approvePayment(anyString(), anyString(), anyString(), anyLong(), anyLong()))
                    .willReturn(createFailKakaopayPaymentResponseDto());

            // when
            paymentServiceFacade.approvePayment("pgToken", "tid");

            // then
            verify(paymentService, times(1)).getPayment(anyString());
            verify(paymentEventRepository, times(1)).save(any());
            verify(kakaopayApi, times(1)).approvePayment(anyString(), anyString(), anyString(), anyLong(), anyLong());
            verify(paymentService, times(1)).failPayment(anyLong(), anyLong());
            verify(paymentService, never()).approvePayment(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("결제 취소 시")
    class CancelPayment {

        @Test
        @DisplayName("카카오페이 api 응답이 success이면 결제 취소가 된다.")
        void cancelPaymentWithSuccess() {
            // given
            CreateEventRequestDto createEventRequestDto = getCreateEventRequestDtoWithMaxPeople(10);
            given(paymentService.getPayment(anyString()))
                    .willReturn(PaymentResponseDto.from(getApprovePayment(
                            getEvent(createEventRequestDto),
                            getUser(),
                            0,
                            null))
                    );
            given(paymentEventRepository.save(any()))
                    .willReturn(getPaymentEvent());
            given(kakaopayApi.cancelPayment(anyString(), anyString(), anyInt(), anyInt()))
                    .willReturn(createSuccessKakaopayCancelPaymentResponseDto());

            // when
            paymentServiceFacade.cancelPayment("tid");

            // then
            verify(paymentService, times(1)).getPayment(anyString());
            verify(paymentEventRepository, times(1)).save(any());
            verify(kakaopayApi, times(1)).cancelPayment(anyString(), anyString(), anyInt(), anyInt());
            verify(paymentService, times(1)).cancelPayment(anyLong(), anyLong());
            verify(paymentService, never()).failPayment(anyLong(), anyLong());
        }

        @Test
        @DisplayName("카카오페이 api 응답이 fail이면 결제 취소가 된다.")
        void cancelPaymentWithFail() {
            // given
            CreateEventRequestDto createEventRequestDto = getCreateEventRequestDtoWithMaxPeople(10);
            given(paymentService.getPayment(anyString()))
                    .willReturn(PaymentResponseDto.from(getApprovePayment(
                            getEvent(createEventRequestDto),
                            getUser(),
                            0,
                            null))
                    );
            given(paymentEventRepository.save(any()))
                    .willReturn(getPaymentEvent());
            given(kakaopayApi.cancelPayment(anyString(), anyString(), anyInt(), anyInt()))
                    .willReturn(createFailKakaopayPaymentResponseDto());

            // when
            paymentServiceFacade.cancelPayment("tid");

            // then
            verify(paymentService, times(1)).getPayment(anyString());
            verify(paymentEventRepository, times(1)).save(any());
            verify(kakaopayApi, times(1)).cancelPayment(anyString(), anyString(), anyInt(), anyInt());
            verify(paymentEventRepository, times(1)).deleteById(anyLong());
            verify(paymentService, never()).cancelPayment(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("결제 다건 조회 시")
    class GetPayments {

        @Test
        @DisplayName("특정 사용자의 결제들이 조회된다.")
        void getPayments() {
            // when
            assertThatNoException().isThrownBy(() -> paymentServiceFacade.getPayments(1L, PageRequest.of(0, 10)));

            // then
            verify(paymentService, times(1)).getPayments(anyLong(), any(PageRequest.class));
        }

    }

    PaymentEvent getPaymentEvent() {
        PaymentEvent readyEvent = PaymentEvent.createReadyEvent(1L);
        ReflectionTestUtils.setField(readyEvent, "id", 1L);
        return readyEvent;
    }

    User getUser() {
        User user = UserTestData.testUser();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    Event getEvent(CreateEventRequestDto createEventRequestDto) {
        Event event = Event.of(createEventRequestDto, getUser());
        ReflectionTestUtils.setField(event, "id", 1L);
        return event;
    }

    Payment getPendingPayment(Event event, User user, int price, Point point) {
        Payment payment = Payment.create("cid", event, user, price, price, null, point, 0);
        ReflectionTestUtils.setField(payment, "id", 1L);
        return payment;
    }

    Payment getApprovePayment(Event event, User user, int price, Point point) {
        Payment payment = Payment.create("cid", event, user, price, price, null, point, 0);
        ReflectionTestUtils.setField(payment, "id", 1L);
        ReflectionTestUtils.setField(payment, "tid", "tid");
        return payment;
    }

    KakaopayPaymentResponseDto createSuccessKakaopayReadyPaymentResponseDto() {
        return KakaopayPaymentResponseDto.ofSuccess(new KakaopayReadyPaymentResponseDto(
                "tid",
                "url",
                "url",
                "url",
                "url",
                "url",
                LocalDateTime.now()
        ));
    }

    KakaopayPaymentResponseDto createSuccessKakaopayApprovePaymentResponseDto() {
        return KakaopayPaymentResponseDto.ofSuccess(new KakaopayApprovePaymentResponseDto(
                "aid",
                "tid",
                "cid",
                "sid",
                "1",
                "itemName",
                "1",
                new KakaopayApprovePaymentResponseDto.Amount(1, 1, 1, 1, 1, 1),
                new KakaopayApprovePaymentResponseDto.CardInfo("", "", "", "", "", "", "", "", "", "", "", ""),
                "itemName",
                "1",
                1,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "payload"
        ));
    }

    KakaopayPaymentResponseDto createSuccessKakaopayCancelPaymentResponseDto() {
        return KakaopayPaymentResponseDto.ofSuccess(new KakaopayCancelPaymentResponseDto(
                "aid",
                "tid",
                "cid",
                "sid",
                "1",
                "itemName",
                "1",
                new KakaopayCancelPaymentResponseDto.Amount(1, 1, 1, 1, 1, 1),
                new KakaopayCancelPaymentResponseDto.ApprovedCancelAmount(1, 1, 1, 1, 1, 1),
                new KakaopayCancelPaymentResponseDto.CanceledAmount(1, 1, 1, 1, 1, 1),
                new KakaopayCancelPaymentResponseDto.CancelAvailableAmount(1, 1, 1, 1, 1, 1),
                "itemName",
                "itemCode",
                1,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                "payload"
        ));
    }

    KakaopayPaymentResponseDto createFailKakaopayPaymentResponseDto() {
        return KakaopayPaymentResponseDto.ofFail(new KakaopayPaymentErrorResponseDto(400, "error",
                new KakaopayPaymentErrorResponseDto.Extras("code", "message")));
    }

    CreateEventRequestDto getCreateEventRequestDtoWithMaxPeople(int maxPeople) {
        return CreateEventRequestDto.of("title", "content", "location",
                LocalDateTime.of(2024, 12, 12, 12, 0),
                LocalDateTime.of(2024, 12, 12, 14, 0), maxPeople, 10_000,
                EventCategory.ART, LocalDateTime.of(2024, 10, 10, 0, 0),
                LocalDateTime.of(2024, 12, 10, 0, 0));
    }
}
