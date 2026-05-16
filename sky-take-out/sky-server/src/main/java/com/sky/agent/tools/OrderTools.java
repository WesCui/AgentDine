package com.sky.agent.tools;

import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderVO;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class OrderTools {

    @Autowired
    private OrderService orderService;

    @Tool("查询指定用户的所有订单，返回订单列表概要。userId是用户ID，status可选(1-6): 1待付款,2待接单,3已接单,4配送中,5已完成,6已取消")
    @SuppressWarnings("unchecked")
    public List<String> getUserOrders(Long userId, Integer status) {
        if (userId == null) {
            return new ArrayList<>();
        }
        PageResult pageResult = orderService.pageQuery4User(userId, 1, 20, status);
        List<OrderVO> orders = (List<OrderVO>) pageResult.getRecords();
        List<String> result = new ArrayList<>();
        if (orders != null) {
            for (OrderVO order : orders) {
                result.add(String.format("订单号: %s, 状态: %s, 金额: %.2f, 时间: %s",
                        order.getNumber(),
                        statusName(order.getStatus()),
                        order.getAmount() != null ? order.getAmount().doubleValue() : 0,
                        order.getOrderTime()));
            }
        }
        if (result.isEmpty()) {
            result.add("暂无符合条件的订单");
        }
        return result;
    }

    @Tool("查询指定订单的详细信息。orderId是订单ID")
    public String getOrderDetail(Long orderId) {
        if (orderId == null) {
            return "请提供有效的订单ID";
        }
        try {
            OrderVO order = orderService.details(orderId);
            if (order == null) {
                return "未找到该订单";
            }
            return String.format("订单号: %s, 状态: %s, 金额: %.2f, 下单时间: %s, 收货地址: %s",
                    order.getNumber(),
                    statusName(order.getStatus()),
                    order.getAmount() != null ? order.getAmount().doubleValue() : 0,
                    order.getOrderTime(),
                    order.getAddress());
        } catch (Exception e) {
            log.error("Failed to get order detail for orderId={}", orderId, e);
            return "查询订单详情失败: " + e.getMessage();
        }
    }

    @Tool("用户催单。orderId是订单ID")
    public String remindOrder(Long orderId) {
        if (orderId == null) {
            return "请提供有效的订单ID";
        }
        try {
            orderService.reminder(orderId);
            return "催单成功！已向商家发送提醒，请耐心等待。";
        } catch (Exception e) {
            log.error("Failed to remind order {}", orderId, e);
            return "催单失败: " + e.getMessage();
        }
    }

    @Tool("用户取消订单。orderId是订单ID")
    public String cancelOrder(Long orderId) {
        if (orderId == null) {
            return "请提供有效的订单ID";
        }
        try {
            orderService.userCancelById(orderId);
            return "订单已成功取消。如果是待接单状态，订单款项将在1-3个工作日内原路退回。";
        } catch (Exception e) {
            log.error("Failed to cancel order {}", orderId, e);
            return "取消订单失败: " + e.getMessage();
        }
    }

    private String statusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "待付款";
            case 2: return "待接单";
            case 3: return "已接单";
            case 4: return "配送中";
            case 5: return "已完成";
            case 6: return "已取消";
            default: return "未知状态";
        }
    }
}
