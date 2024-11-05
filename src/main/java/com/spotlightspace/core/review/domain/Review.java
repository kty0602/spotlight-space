package com.spotlightspace.core.review.domain;

import com.spotlightspace.common.entity.Timestamped;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.review.dto.ReviewRequestDto;
import com.spotlightspace.core.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reviews")
public class Review extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    private String contents;

    private Integer rating;


    @Column(name = "is_deleted")
    private Boolean isDeleted = false;


    private Review(ReviewRequestDto reviewRequestDto, Event event, User user) {
        this.event = event;
        this.user = user;
        this.rating = reviewRequestDto.getRating();
        this.contents = reviewRequestDto.getContents();
    }

    //파일
    public static Review of(ReviewRequestDto reviewRequestDto, Event event, User user) {
        return new Review(reviewRequestDto, event, user);
    }

    public void changeRating(Integer rating) {
        this.rating = rating;
    }

    public void changeContents(String contents) {
        this.contents = contents;
    }

    public void changeIsDeleted() {
        this.isDeleted = true;
    }

}