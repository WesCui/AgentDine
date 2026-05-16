package com.sky.service.impl;

import com.sky.service.CacheMetricsService;
import com.sky.vo.CacheMetricsVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Service
public class CacheMetricsServiceImpl implements CacheMetricsService {

    private static final String HIT_SUFFIX = ":hit";
    private static final String MISS_SUFFIX = ":miss";

    // 使用ConcurrentHashMap和LongAdder来高效地统计缓存命中和未命中的次数，避免了锁竞争，提高了性能
    private final Map<String, LongAdder> counters = new ConcurrentHashMap<>();

    @Override
    public void recordHit(String cacheName) {
        increment(cacheName + HIT_SUFFIX);
    }

    @Override
    public void recordMiss(String cacheName) {
        increment(cacheName + MISS_SUFFIX);
    }

    @Override
    public List<CacheMetricsVO> listMetrics() {
        List<CacheMetricsVO> metrics = new ArrayList<>();

        counters.keySet().stream()
                .map(this::extractCacheName)
                .distinct()
                .sorted()
                .forEach(cacheName -> {
                    long hitCount = getCount(cacheName + HIT_SUFFIX);
                    long missCount = getCount(cacheName + MISS_SUFFIX);
                    long requestCount = hitCount + missCount;
                    double hitRate = requestCount == 0 ? 0.0 : (double) hitCount / requestCount;

                    metrics.add(CacheMetricsVO.builder()
                            .cacheName(cacheName)
                            .hitCount(hitCount)
                            .missCount(missCount)
                            .requestCount(requestCount)
                            .hitRate(hitRate)
                            .build());
                });

        return metrics;
    }

    private void increment(String key) {
        counters.computeIfAbsent(key, unused -> new LongAdder()).increment();
    }

    private long getCount(String key) {
        LongAdder adder = counters.get(key);
        return adder == null ? 0L : adder.sum();
    }

    private String extractCacheName(String key) {
        if (key.endsWith(HIT_SUFFIX)) {
            return key.substring(0, key.length() - HIT_SUFFIX.length());
        }
        if (key.endsWith(MISS_SUFFIX)) {
            return key.substring(0, key.length() - MISS_SUFFIX.length());
        }
        return key;
    }
}
