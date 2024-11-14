package com.spotlightspace.core.likes.repository;

import com.spotlightspace.core.likes.domain.Likes;
import com.spotlightspace.core.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LikesRepository extends JpaRepository<Likes, Long> {


    @Query("select l.user from Likes l where l.review.id = :reviewId")
    List<User> findLikeUsersByReviewId(Long reviewId);

}
