package com.spotlightspace.core.event.service;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedissonLockService {
    private final RedissonClient redissonClient;

    public RLock lock(String key) {
        RLock lock = redissonClient.getLock(key);
        lock.lock(5, TimeUnit.SECONDS); // 5초 동안 락 유지
        return lock;
    }

    // 현재 스레드가 해당 락을 소유하고 있는가?
    public void unlock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
