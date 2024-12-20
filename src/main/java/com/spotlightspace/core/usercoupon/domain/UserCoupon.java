package com.spotlightspace.core.usercoupon.domain;

import com.spotlightspace.common.entity.Timestamped;
import com.spotlightspace.core.coupon.domain.Coupon;
import com.spotlightspace.core.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_coupons")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(nullable = false)
    private boolean isUsed = false;

    public static UserCoupon of(User user, Coupon coupon) {
        return new UserCoupon(null, user, coupon, false);
    }

    public void use() {
        this.isUsed = true;
    }

    public int getDiscountAmount() {
        return coupon.getDiscountAmount();
    }

    public void cancelUsage() {
        this.isUsed = false;
    }
}
