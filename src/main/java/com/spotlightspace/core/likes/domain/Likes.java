package com.spotlightspace.core.likes.domain;

import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Likes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Review review;

    private Likes(User user, Review review) {
        this.user = user;
        this.review = review;
    }

    public static Likes of(User user, Review review) {
        return new Likes(user, review);
    }
}