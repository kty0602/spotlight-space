package com.spotlightspace.core.review.domain;

import com.spotlightspace.common.entity.Timestamped;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.review.dto.ReviewRequestDto;
import com.spotlightspace.core.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reviews")
public class Review extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @Column(nullable = false)
    private String contents;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "likes", nullable = false)
    private int likeCount;

    @OneToMany(cascade = CascadeType.PERSIST)
    private List<User> likeUsers;

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

    public void likeReview(User user) {
        this.likeUsers.add(user);
        likeCount++;
    }

    public void dislikeReview(User user) { this.likeUsers.remove(user);
        likeCount--;}

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
