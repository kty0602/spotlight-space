package com.spotlightspace.core.payment.client;

import static com.spotlightspace.core.payment.constant.PaymentConstant.APPROVAL_URL;
import static com.spotlightspace.core.payment.constant.PaymentConstant.CANCEL_URL;
import static com.spotlightspace.core.payment.constant.PaymentConstant.CID;
import static com.spotlightspace.core.payment.constant.PaymentConstant.FAIL_URL;
import static com.spotlightspace.core.payment.constant.PaymentConstant.PAYMENT_APPROVE_URL;
import static com.spotlightspace.core.payment.constant.PaymentConstant.PAYMENT_READY_URL;

import com.spotlightspace.core.payment.domain.Payment;
import com.spotlightspace.core.payment.dto.response.ApprovePaymentResponseDto;
import com.spotlightspace.core.payment.dto.response.ReadyPaymentResponseDto;
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

    public ReadyPaymentResponseDto readyPayment(
            long partnerOrderId,
            long userId,
            String eventTitle,
            long eventId,
            int totalPrice
    ) {
        Map<String, String> parameters = getParametersForReadyPayment(
                partnerOrderId,
                userId,
                eventTitle,
                eventId,
                totalPrice
        );

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

    private Map<String, String> getParametersForReadyPayment(
            long partnerOrderId,
            long userId,
            String eventTitle,
            long eventId,
            int totalPrice
    ) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", CID);
        parameters.put("tax_free_amount", "0");
        parameters.put("partner_order_id", String.valueOf(partnerOrderId));
        parameters.put("partner_user_id", String.valueOf(userId));
        parameters.put("item_name", eventTitle);
        parameters.put("item_code", String.valueOf(eventId));
        parameters.put("quantity", "1");
        parameters.put("total_amount", String.valueOf(totalPrice));
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
