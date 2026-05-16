package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheMetricsVO implements Serializable {

    private String cacheName;

    private long hitCount;

    private long missCount;

    private long requestCount;

    private double hitRate;
}
