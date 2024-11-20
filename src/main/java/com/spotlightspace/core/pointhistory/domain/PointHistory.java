package com.spotlightspace.core.pointhistory.domain;

import com.spotlightspace.common.entity.Timestamped;
import com.spotlightspace.core.payment.domain.Payment;
import com.spotlightspace.core.point.domain.Point;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "point_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory extends Timestamped {

    @Id
    @GeneratedValue
    @Column(name = "point_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id", nullable = false)
    private Point point;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointHistoryStatus status;

    private PointHistory(Point point, Payment payment, int amount) {
        this.point = point;
        this.payment = payment;
        this.amount = amount;
        this.status = PointHistoryStatus.USED;
    }

    public static PointHistory create(Payment payment, Point point, int amount) {
        return new PointHistory(point, payment, amount);
    }

    public void cancelPointUsage() {
        this.status = PointHistoryStatus.CANCELED;
    }
}
