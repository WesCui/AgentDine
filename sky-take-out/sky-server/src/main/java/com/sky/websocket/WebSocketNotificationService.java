package com.sky.websocket;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketNotificationService {

    @Autowired
    private WebSocketServer webSocketServer;

    @Async("notificationExecutor")
    public void notifyNewOrder(Long orderId, String orderNumber) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", 1);
        payload.put("orderId", orderId);
        payload.put("content", "订单号：" + orderNumber);
        webSocketServer.sendToAllClient(JSON.toJSONString(payload));
    }

    @Async("notificationExecutor")
    public void notifyReminder(Long orderId, String orderNumber) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", 2);
        payload.put("orderId", orderId);
        payload.put("content", "订单号：" + orderNumber + "已催单，请尽快处理！");
        webSocketServer.sendToAllClient(JSON.toJSONString(payload));
    }
}
