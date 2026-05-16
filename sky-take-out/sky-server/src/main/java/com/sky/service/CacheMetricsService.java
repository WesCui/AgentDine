package com.sky.service;

import com.sky.vo.CacheMetricsVO;

import java.util.List;

public interface CacheMetricsService {

    void recordHit(String cacheName);

    void recordMiss(String cacheName);

    List<CacheMetricsVO> listMetrics();
}
