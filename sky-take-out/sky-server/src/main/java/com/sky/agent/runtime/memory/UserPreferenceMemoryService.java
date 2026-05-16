package com.sky.agent.runtime.memory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserPreferenceMemoryService implements MemoryManager {

    private static final String PREF_KEY_PREFIX = "agent:user:%s:preferences";
    private static final long DEFAULT_TTL = 1209600; // 14 days

    private final StringRedisTemplate redisTemplate;
    private final boolean redisAvailable;

    // fallback in-memory storage
    private final ConcurrentHashMap<Long, Map<String, Object>> memoryStore = new ConcurrentHashMap<>();

    public UserPreferenceMemoryService(StringRedisTemplate redisTemplate,
                                       @org.springframework.beans.factory.annotation.Value("${sky.agent.memory.backend:redis}") String backend) {
        this.redisTemplate = redisTemplate;
        this.redisAvailable = "redis".equals(backend);
    }

    @Override
    public void savePreference(Long userId, String key, Object value) {
        if (redisAvailable) {
            Map<String, Object> prefs = getAllPreferences(userId);
            prefs.put(key, value);
            String redisKey = String.format(PREF_KEY_PREFIX, userId);
            redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(prefs), DEFAULT_TTL, TimeUnit.SECONDS);
        } else {
            memoryStore.computeIfAbsent(userId, k -> new HashMap<>()).put(key, value);
        }
    }

    @Override
    public Object getPreference(Long userId, String key) {
        Map<String, Object> prefs = getAllPreferences(userId);
        return prefs.get(key);
    }

    @Override
    public Map<String, Object> getAllPreferences(Long userId) {
        if (redisAvailable) {
            String redisKey = String.format(PREF_KEY_PREFIX, userId);
            String json = redisTemplate.opsForValue().get(redisKey);
            if (json != null) {
                return JSON.parseObject(json, new TypeReference<Map<String, Object>>() {});
            }
            return new HashMap<>();
        }
        return memoryStore.getOrDefault(userId, new HashMap<>());
    }

    @Override
    public void clearPreferences(Long userId) {
        if (redisAvailable) {
            String redisKey = String.format(PREF_KEY_PREFIX, userId);
            redisTemplate.delete(redisKey);
        } else {
            memoryStore.remove(userId);
        }
    }
}
