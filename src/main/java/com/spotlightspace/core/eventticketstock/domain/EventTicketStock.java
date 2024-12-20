package com.spotlightspace.core.eventticketstock.domain;

import com.spotlightspace.common.entity.Timestamped;
import com.spotlightspace.core.event.domain.Event;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "event_ticket_stocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventTicketStock extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_ticket_stock_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private int stock;

    private EventTicketStock(Event event, int stock) {
        this.event = event;
        this.stock = stock;
    }

    public static EventTicketStock create(Event event) {
        return new EventTicketStock(event, event.getMaxPeople());
    }

    public boolean isOutOfStock() {
        return stock == 0;
    }

    public void decreaseStock() {
        this.stock--;
    }

    public void decreaseStock(long count) {
        this.stock -= count;
    }

    public void increaseStock() {
        this.stock++;
    }
}
