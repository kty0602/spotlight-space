package com.spotlightspace.core.payment.client;

import static com.spotlightspace.common.exception.ErrorCode.EVENT_PARTICIPANT_LIMIT_EXCEED;
import static com.spotlightspace.common.exception.ErrorCode.NOT_IN_EVENT_RECRUITMENT_PERIOD;
import static com.spotlightspace.core.payment.constant.PaymentConstant.APPROVAL_URL;
import static com.spotlightspace.core.payment.constant.PaymentConstant.CANCEL_URL;
import static com.spotlightspace.core.payment.constant.PaymentConstant.CID;
import static com.spotlightspace.core.payment.constant.PaymentConstant.FAIL_URL;
import static com.spotlightspace.core.payment.constant.PaymentConstant.PAYMENT_APPROVE_URL;
import static com.spotlightspace.core.payment.constant.PaymentConstant.PAYMENT_READY_URL;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.payment.domain.Payment;
import com.spotlightspace.core.payment.dto.response.ApprovePaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.ReadyPaymentResponseDto;
import com.spotlightspace.core.payment.repository.PaymentRepository;
import com.spotlightspace.core.ticket.service.TicketService;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KakaopayApi {

    private static final String SECRET_KEY_PREFIX = "SECRET_KEY ";

    @Value("${payment.secret.key}")
    private String secretKey;

    public ReadyPaymentResponseDto readyPayment(Payment payment, User user, Event event) {
        Map<String, String> parameters = getParametersForReadyPayment(payment, user, event);

        RestTemplate restTemplate = new RestTemplate();
        ReadyPaymentResponseDto responseDto = restTemplate.postForObject(
                PAYMENT_READY_URL,
                new HttpEntity<>(parameters, getHeaders()),
                ReadyPaymentResponseDto.class
        );

        return responseDto;
    }

    public ApprovePaymentResponseDto approvePayment(String pgToken, Payment payment) {
        Map<String, String> parameters = getParametersForApprovePayment(pgToken, payment);

        RestTemplate template = new RestTemplate();
        ApprovePaymentResponseDto responseDto = template.postForObject(
                PAYMENT_APPROVE_URL,
                new HttpEntity<>(parameters, getHeaders()),
                ApprovePaymentResponseDto.class
        );

        return responseDto;
    }

    private Map<String, String> getParametersForReadyPayment(Payment payment, User user, Event event) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", CID);
        parameters.put("tax_free_amount", "0");
        parameters.put("partner_order_id", String.valueOf(payment.getPartnerOrderId()));
        parameters.put("partner_user_id", String.valueOf(user.getId()));
        parameters.put("item_name", event.getTitle());
        parameters.put("item_code", String.valueOf(event.getId()));
        parameters.put("quantity", "1");
        parameters.put("total_amount", String.valueOf(event.getPrice()));
        parameters.put("approval_url", APPROVAL_URL);
        parameters.put("cancel_url", CANCEL_URL);
        parameters.put("fail_url", FAIL_URL);
        return parameters;
    }

    private Map<String, String> getParametersForApprovePayment(String pgToken, Payment payment) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", payment.getCid());
        parameters.put("tid", payment.getTid());
        parameters.put("partner_order_id", String.valueOf(payment.getPartnerOrderId()));
        parameters.put("partner_user_id", String.valueOf(payment.getPartnerUserId()));
        parameters.put("pg_token", pgToken);
        return parameters;
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", SECRET_KEY_PREFIX + secretKey);
        headers.set("Content-type", "application/json");
        return headers;
    }
}
