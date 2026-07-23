package com.insurance.claimapi.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory idempotency store.
 * Stores requestId -> result for a configurable TTL window.
 * For production, replace with Redis.
 */
@Service
public class IdempotencyService {

    private static final Duration TTL = Duration.ofMinutes(10);

    private final ConcurrentHashMap<String, CacheEntry> store = new ConcurrentHashMap<>();

    public record CacheEntry(Object result, Instant expiresAt) {}

    /**
     * Returns existing result if requestId was already processed, otherwise null.
     */
    public Object getIfPresent(String requestId) {
        if (requestId == null) return null;
        cleanUp();
        CacheEntry entry = store.get(requestId);
        if (entry != null && entry.expiresAt().isAfter(Instant.now())) {
            return entry.result();
        }
        store.remove(requestId);
        return null;
    }

    /**
     * Mark requestId as processed with its result.
     */
    public void put(String requestId, Object result) {
        if (requestId == null) return;
        store.put(requestId, new CacheEntry(result, Instant.now().plus(TTL)));
    }

    private void cleanUp() {
        Instant now = Instant.now();
        store.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
    }
}
