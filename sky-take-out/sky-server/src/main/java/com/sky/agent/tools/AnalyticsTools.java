package com.sky.agent.tools;

import com.sky.service.CacheMetricsService;
import com.sky.service.ReportService;
import com.sky.vo.*;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AnalyticsTools {

    @Autowired
    private ReportService reportService;

    @Autowired
    private CacheMetricsService cacheMetricsService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Tool("查询指定日期范围内的营业额统计数据。begin和end格式为yyyy-MM-dd")
    public String getTurnoverStatistics(String begin, String end) {
        try {
            LocalDate beginDate = LocalDate.parse(begin, DATE_FMT);
            LocalDate endDate = LocalDate.parse(end, DATE_FMT);
            TurnoverReportVO report = reportService.getTurnoverStatistics(beginDate, endDate);

            StringBuilder sb = new StringBuilder();
            sb.append("=== 营业额统计 (").append(begin).append(" ~ ").append(end).append(") ===\n");
            sb.append("日期列表: ").append(report.getDateList()).append("\n");
            sb.append("营业额列表: ").append(report.getTurnoverList()).append("\n");

            double total = 0;
            String[] turnovers = report.getTurnoverList().split(",");
            for (String t : turnovers) {
                try { total += Double.parseDouble(t); } catch (NumberFormatException ignored) {}
            }
            sb.append("总营业额: ¥").append(String.format("%.2f", total));
            return sb.toString();
        } catch (Exception e) {
            log.error("Failed to get turnover statistics", e);
            return "查询营业额统计失败: " + e.getMessage();
        }
    }

    @Tool("查询指定日期范围内的用户统计数据。begin和end格式为yyyy-MM-dd")
    public String getUserStatistics(String begin, String end) {
        try {
            LocalDate beginDate = LocalDate.parse(begin, DATE_FMT);
            LocalDate endDate = LocalDate.parse(end, DATE_FMT);
            UserReportVO report = reportService.getUserStatistics(beginDate, endDate);

            return String.format("=== 用户统计 (%s ~ %s) ===\n新增用户: %s\n总用户: %s",
                    begin, end, report.getNewUserList(), report.getTotalUserList());
        } catch (Exception e) {
            log.error("Failed to get user statistics", e);
            return "查询用户统计失败: " + e.getMessage();
        }
    }

    @Tool("查询指定日期范围内的订单统计数据。begin和end格式为yyyy-MM-dd")
    public String getOrderStatistics(String begin, String end) {
        try {
            LocalDate beginDate = LocalDate.parse(begin, DATE_FMT);
            LocalDate endDate = LocalDate.parse(end, DATE_FMT);
            OrderReportVO report = reportService.getOrderStatistics(beginDate, endDate);

            return String.format("=== 订单统计 (%s ~ %s) ===\n总订单数: %d\n有效订单数: %d\n订单完成率: %.1f%%\n每日订单: %s\n每日有效订单: %s",
                    begin, end,
                    report.getTotalOrderCount(), report.getValidOrderCount(),
                    report.getOrderCompletionRate(),
                    report.getOrderCountList(), report.getValidOrderCountList());
        } catch (Exception e) {
            log.error("Failed to get order statistics", e);
            return "查询订单统计失败: " + e.getMessage();
        }
    }

    @Tool("查询指定日期范围内销量Top10的商品。begin和end格式为yyyy-MM-dd")
    public String getSalesTop10(String begin, String end) {
        try {
            LocalDate beginDate = LocalDate.parse(begin, DATE_FMT);
            LocalDate endDate = LocalDate.parse(end, DATE_FMT);
            SalesTop10ReportVO report = reportService.getSalesTop10(beginDate, endDate);

            StringBuilder sb = new StringBuilder();
            sb.append("=== 销量Top10 (").append(begin).append(" ~ ").append(end).append(") ===\n");
            String[] names = report.getNameList().split(",");
            String[] numbers = report.getNumberList().split(",");
            for (int i = 0; i < Math.min(names.length, numbers.length); i++) {
                sb.append(String.format("%d. %s — %s单\n", i + 1, names[i], numbers[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Failed to get sales top10", e);
            return "查询销量排行失败: " + e.getMessage();
        }
    }

    @Tool("查询当前缓存命中率等指标")
    public String getCacheMetrics() {
        try {
            List<CacheMetricsVO> metrics = cacheMetricsService.listMetrics();
            if (metrics == null || metrics.isEmpty()) {
                return "暂无缓存指标数据";
            }
            StringBuilder sb = new StringBuilder("=== 缓存指标 ===\n");
            for (CacheMetricsVO m : metrics) {
                sb.append(String.format("%s: 命中率 %.1f%% (命中%d, 未命中%d, 总请求%d)\n",
                        m.getCacheName(), m.getHitRate(), m.getHitCount(), m.getMissCount(), m.getRequestCount()));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Failed to get cache metrics", e);
            return "查询缓存指标失败: " + e.getMessage();
        }
    }
}
