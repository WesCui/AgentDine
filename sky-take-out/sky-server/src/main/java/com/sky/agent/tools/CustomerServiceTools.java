package com.sky.agent.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class CustomerServiceTools {

    private final ConcurrentHashMap<Long, Map<String, Object>> tickets = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Tool("创建客服工单。userId是用户ID, ticketType是工单类型(complaint/refund/feedback/other), title是标题, description是描述")
    public String createSupportTicket(Long userId, String ticketType, String title, String description) {
        if (userId == null || title == null) {
            return "创建工单失败: 用户ID和标题不能为空";
        }
        long ticketId = idGenerator.getAndIncrement();
        Map<String, Object> ticket = new HashMap<>();
        ticket.put("id", ticketId);
        ticket.put("userId", userId);
        ticket.put("type", ticketType != null ? ticketType : "other");
        ticket.put("title", title);
        ticket.put("description", description);
        ticket.put("status", "open");
        ticket.put("createdAt", System.currentTimeMillis());
        tickets.put(ticketId, ticket);

        log.info("Support ticket created: id={}, userId={}, type={}, title={}", ticketId, userId, ticketType, title);

        return String.format("工单创建成功！工单号: %d, 类型: %s, 标题: %s, 状态: 处理中。我们会在24小时内处理您的反馈。",
                ticketId, ticketType, title);
    }

    @Tool("根据用户请求进行人工客服转接。记录转接请求并返回转接信息")
    public String escalateToHuman(Long userId, String reason) {
        log.info("Escalating to human agent: userId={}, reason={}", userId, reason);
        return String.format("已记录您的人工客服转接请求。原因: %s。客服人员将在工作时间内与您联系，请保持手机畅通。工作时间: 每日 9:00-21:00。", reason != null ? reason : "用户请求");
    }

    @Tool("获取退款政策说明")
    public String getRefundPolicy() {
        return "退款政策概要:\n"
                + "1. 待付款取消 — 自动取消，即时\n"
                + "2. 待接单取消 — 全额退款，1-3工作日\n"
                + "3. 已接单取消 — 50-100%退款（商家审核），1-5工作日\n"
                + "4. 配送中超时30分钟 — 10-30%补偿，1-3工作日\n"
                + "5. 菜品质量问题（有证据）— 80-100%退款，1-3工作日\n"
                + "6. 撒漏破损 — 全额退款，1-3工作日\n"
                + "如需申请退款，请提供订单号和问题描述（附照片）。";
    }
}
