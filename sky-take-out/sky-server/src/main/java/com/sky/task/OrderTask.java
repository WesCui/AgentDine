package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {


    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单的定时任务
     */
    @Scheduled(cron = "0 * * * * ?") //每分钟执行一次 0 * * * * ?
    public void processTimeoutOrders() {
        log.info("处理超时订单的定时任务执行了...", LocalDateTime.now());
        //这里可以调用订单服务的方法来处理超时订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("超时未支付，系统自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }


        }
    }

    /**
     * 处理配送中的订单的定时任务
     */
    @Scheduled(cron = "0 0 1 * * ?") //每天凌晨1点执行一次 0 0 1 * * ?
    public void processDeliveryOrders() {
        log.info("处理配送中订单的定时任务执行了...", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusHours(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }


}
