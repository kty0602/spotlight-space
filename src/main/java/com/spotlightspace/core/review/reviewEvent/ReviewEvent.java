package com.spotlightspace.core.review.reviewEvent;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;

@RequiredArgsConstructor

public class ReviewEvent {

    private final RedissonClient redissonClient;

}
