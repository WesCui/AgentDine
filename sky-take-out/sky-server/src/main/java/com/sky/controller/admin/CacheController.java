package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.CacheMetricsService;
import com.sky.vo.CacheMetricsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/cache")
@Api(tags = "缓存监控接口")
public class CacheController {

    @Autowired
    private CacheMetricsService cacheMetricsService;

    @GetMapping("/metrics")
    @ApiOperation("查看热点缓存命中指标")
    public Result<List<CacheMetricsVO>> metrics() {
        return Result.success(cacheMetricsService.listMetrics());
    }
}
