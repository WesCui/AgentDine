package com.sky.service.impl;

import com.sky.constant.CacheConstants;
import com.sky.entity.Setmeal;
import com.sky.service.CacheMetricsService;
import com.sky.service.HotDataCacheService;
import com.sky.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Service
public class HotDataCacheServiceImpl implements HotDataCacheService {

    private static final long DISH_CACHE_MIN_TTL_MINUTES = 30L;
    private static final long DISH_CACHE_MAX_TTL_MINUTES = 60L;
    private static final long SETMEAL_CACHE_MIN_TTL_MINUTES = 45L;
    private static final long SETMEAL_CACHE_MAX_TTL_MINUTES = 90L;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CacheMetricsService cacheMetricsService;
    /**
     * 菜品数据更新较频繁，设置较短的过期时间，减少缓存穿透风险
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<DishVO> getDishList(Long categoryId, Supplier<List<DishVO>> loader) {
        String key = CacheConstants.DISH_CACHE_PREFIX + categoryId;
        return (List<DishVO>) getOrLoad(
                CacheConstants.DISH_CACHE_NAME,
                key,
                loader,
                randomDurationMinutes(DISH_CACHE_MIN_TTL_MINUTES, DISH_CACHE_MAX_TTL_MINUTES)
        );
    }
    /**
     * 套餐数据相对菜品更稳定，可以设置更长的过期时间，减少缓存穿透风险
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Setmeal> getSetmealList(Long categoryId, Supplier<List<Setmeal>> loader) {
        String key = CacheConstants.SETMEAL_CACHE_PREFIX + categoryId;
        return (List<Setmeal>) getOrLoad(
                CacheConstants.SETMEAL_CACHE_NAME,
                key,
                loader,
                randomDurationMinutes(SETMEAL_CACHE_MIN_TTL_MINUTES, SETMEAL_CACHE_MAX_TTL_MINUTES)
        );
    }
    /**
     * 店铺状态变化不频繁，且对实时性要求较高，因此不设置过期时间，直到下一次更新时才会覆盖旧值
     */
    @Override
    public Integer getShopStatus(Supplier<Integer> loader) {
        Object cached = redisTemplate.opsForValue().get(CacheConstants.SHOP_STATUS_KEY);
        if (cached != null) {
            cacheMetricsService.recordHit(CacheConstants.SHOP_CACHE_NAME);
            return (Integer) cached;
        }

        cacheMetricsService.recordMiss(CacheConstants.SHOP_CACHE_NAME);
        Integer status = loader.get();
        redisTemplate.opsForValue().set(CacheConstants.SHOP_STATUS_KEY, status);
        return status;
    }
    /**
     * 店铺状态变化不频繁，且对实时性要求较高，因此不设置过期时间，直到下一次更新时才会覆盖旧值
     */
    @Override
    public void updateShopStatus(Integer status) {
        redisTemplate.opsForValue().set(CacheConstants.SHOP_STATUS_KEY, status);
    }

    @Override
    public void evictDishCategory(Long categoryId) {
        redisTemplate.delete(CacheConstants.DISH_CACHE_PREFIX + categoryId);
    }

    @Override
    public void evictAllDish() {
        evictByPattern(CacheConstants.DISH_CACHE_PREFIX + "*");
    }

    @Override
    public void evictAllSetmeal() {
        evictByPattern(CacheConstants.SETMEAL_CACHE_PREFIX + "*");
    }

    private Object getOrLoad(String cacheName, String key, Supplier<?> loader, Duration ttl) {
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            cacheMetricsService.recordHit(cacheName);
            return cached;
        }

        cacheMetricsService.recordMiss(cacheName);
        Object loaded = loader.get();
        redisTemplate.opsForValue().set(key, loaded, ttl);
        return loaded;
    }

    private void evictByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private Duration randomDurationMinutes(long minMinutes, long maxMinutes) {
        long ttl = ThreadLocalRandom.current().nextLong(minMinutes, maxMinutes + 1);
        return Duration.ofMinutes(ttl);
    }
}
