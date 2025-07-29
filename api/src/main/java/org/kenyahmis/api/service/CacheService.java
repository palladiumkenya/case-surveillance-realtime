package org.kenyahmis.api.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addEntry(String key, String value) {
        redisTemplate.opsForValue().set(key, value, Duration.ofHours(10)); // TTL: 10 hours
    }

    public boolean entryExists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public Object getEntry(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
