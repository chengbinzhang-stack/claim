package com.insurance.claimapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * In-memory idempotency store using Caffeine cache.
 * Stores requestId -> result for a configurable TTL window (default 10 minutes).
 */
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String CACHE_NAME = "idempotency";

    private final CacheManager cacheManager;

    /**
     * Returns existing result if requestId was already processed, otherwise null.
     */
    public Object getIfPresent(String requestId) {
        if (requestId == null) return null;
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) return null;
        Cache.ValueWrapper wrapper = cache.get(requestId);
        return wrapper != null ? wrapper.get() : null;
    }

    /**
     * Mark requestId as processed with its result.
     */
    public void put(String requestId, Object result) {
        if (requestId == null) return;
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(requestId, result);
        }
    }
}
