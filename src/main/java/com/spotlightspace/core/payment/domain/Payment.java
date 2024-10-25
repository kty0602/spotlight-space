package com.spotlightspace.core.payment.domain;

import static com.spotlightspace.core.payment.domain.PaymentStatus.APPROVED;
import static com.spotlightspace.core.payment.domain.PaymentStatus.READY;

import com.spotlightspace.common.entity.Timestamped;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.usercoupon.domain.UserCoupon;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends Timestamped {

    @Id
    @Column(name = "payment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String tid;

    @Column(nullable = false)
    private String cid;

    @JoinColumn(name = "event_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private int amount;

    @OneToOne(fetch = FetchType.LAZY)
    private UserCoupon userCoupon;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Payment(
            String tid,
            String cid,
            Event event,
            User user,
            int amount,
            UserCoupon userCoupon,
            PaymentStatus status
    ) {
        this.tid = tid;
        this.cid = cid;
        this.event = event;
        this.user = user;
        this.amount = amount;
        this.userCoupon = userCoupon;
        this.status = status;
    }

    public static Payment create(String cid, Event event, User user, int amount) {
        return new Payment(null, cid, event, user, amount, null, READY);
    }

    public static Payment createWithCoupon(String cid, Event event, User user, int amount, UserCoupon userCoupon) {
        return new Payment(null, cid, event, user, amount, userCoupon, READY);
    }

    public Long getPartnerOrderId() {
        return event.getId();
    }

    public Long getPartnerUserId() {
        return user.getId();
    }

    public void approve() {
        this.status = APPROVED;
        if (this.userCoupon != null) {
            this.userCoupon.use();
        }
    }
}
