package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = buildDateRange(begin, end);
        List<Double> turnoverList = new ArrayList<>();

        for (LocalDate date : dateList) {
            Map<String, Object> queryMap = buildDayQueryMap(date);
            queryMap.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(queryMap);
            turnoverList.add(turnover == null ? 0.0 : turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = buildDateRange(begin, end);
        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            Map<String, Object> totalQueryMap = new HashMap<>();
            totalQueryMap.put("endTime", LocalDateTime.of(date, LocalTime.MAX));
            Integer totalUserCount = userMapper.countByMap(totalQueryMap);
            totalUserList.add(totalUserCount == null ? 0 : totalUserCount);

            Map<String, Object> newUserQueryMap = buildDayQueryMap(date);
            Integer newUserCount = userMapper.countByMap(newUserQueryMap);
            newUserList.add(newUserCount == null ? 0 : newUserCount);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = buildDateRange(begin, end);
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        int totalOrderCount = 0;
        int validOrderCount = 0;

        for (LocalDate date : dateList) {
            Map<String, Object> orderQueryMap = buildDayQueryMap(date);
            Integer orderCount = orderMapper.countByMap(orderQueryMap);
            orderCount = orderCount == null ? 0 : orderCount;
            orderCountList.add(orderCount);
            totalOrderCount += orderCount;

            Map<String, Object> validOrderQueryMap = buildDayQueryMap(date);
            validOrderQueryMap.put("status", Orders.COMPLETED);
            Integer validCount = orderMapper.countByMap(validOrderQueryMap);
            validCount = validCount == null ? 0 : validCount;
            validOrderCountList.add(validCount);
            validOrderCount += validCount;
        }

        double orderCompletionRate = totalOrderCount == 0 ? 0.0 :
                (double) validOrderCount / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesList = orderDetailMapper.getSalesTop10(beginTime, endTime, Orders.COMPLETED);
        List<String> nameList = salesList.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.toList());
        List<Integer> numberList = salesList.stream()
                .map(GoodsSalesDTO::getNumber)
                .collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    private List<LocalDate> buildDateRange(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }

    private Map<String, Object> buildDayQueryMap(LocalDate date) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("beginTime", LocalDateTime.of(date, LocalTime.MIN));
        queryMap.put("endTime", LocalDateTime.of(date, LocalTime.MAX));
        return queryMap;
    }
}
