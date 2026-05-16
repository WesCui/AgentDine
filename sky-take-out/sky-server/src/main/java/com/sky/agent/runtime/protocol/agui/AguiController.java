package com.sky.agent.runtime.protocol.agui;

import com.sky.agent.orchestrator.AgentOrchestrator;
import com.sky.agent.orchestrator.OrchestrationResult;
import com.sky.agent.service.AgentChatService;
import com.sky.agent.service.FoodRecommendationAgent;
import com.sky.context.BaseContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/agui")
@Api(tags = "AG-UI 协议接口")
@Slf4j
public class AguiController {

    @Autowired
    private AgentChatService agentChatService;

    @Autowired(required = false)
    private AgentOrchestrator agentOrchestrator;

    @Autowired
    private AguiStateManager stateManager;

    /**
     * 流式对话接口 (AG-UI 协议)
     * 使用 SSE (Server-Sent Events) 实现流式响应
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperation("AG-UI 流式对话")
    public SseEmitter chat(@RequestBody Map<String, String> body) {
        String userMessage = body.getOrDefault("message", "");
        String sessionId = body.getOrDefault("sessionId", UUID.randomUUID().toString().replace("-", ""));

        SseEmitter emitter = new SseEmitter(300000L); // 5 min timeout

        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            userId = 1L;
        }

        final Long finalUserId = userId;
        final String finalSessionId = sessionId;

        // Execute agent call in a separate thread to support SSE streaming
        new Thread(() -> {
            try {
                // Send initial event
                emitter.send(SseEmitter.event()
                        .name("session")
                        .data("{\"sessionId\":\"" + finalSessionId + "\"}"));

                // Send text_delta event with user message echo
                emitter.send(SseEmitter.event()
                        .name("text_delta")
                        .data(AguiEvent.textDelta("正在为您处理...").toSseString()));

                String response;
                String agentName = "food-recommendation";

                // Try orchestrator first
                if (agentOrchestrator != null) {
                    OrchestrationResult result = agentOrchestrator.orchestrate(finalUserId, userMessage);
                    if (result.isRouted()) {
                        response = result.getResponse();
                        agentName = result.getTargetAgentName();
                    } else {
                        response = agentChatService.chat(finalUserId, finalUserId, userMessage);
                    }
                } else {
                    response = agentChatService.chat(finalUserId, finalUserId, userMessage);
                }

                // Send response as text_delta
                emitter.send(SseEmitter.event()
                        .name("text_delta")
                        .data(AguiEvent.textDelta(response).toSseString()));

                // Update state
                stateManager.setState(finalSessionId, "lastAgent", agentName);
                stateManager.setState(finalSessionId, "lastResponse", response);

                // Send state_delta
                emitter.send(SseEmitter.event()
                        .name("state_delta")
                        .data(AguiEvent.stateDelta(stateManager.getAllStates(finalSessionId)).toSseString()));

                // Send done event
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data(AguiEvent.done().toSseString()));

                emitter.complete();
            } catch (Exception e) {
                log.error("AG-UI chat error", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(AguiEvent.error(e.getMessage()).toSseString()));
                    emitter.completeWithError(e);
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        }, "agui-chat-" + finalSessionId).start();

        return emitter;
    }

    /**
     * 状态读取接口
     */
    @PostMapping("/getState")
    @ApiOperation("获取会话状态")
    public Map<String, Object> getState(@RequestBody Map<String, String> body) {
        String sessionId = body.getOrDefault("sessionId", "");
        String key = body.get("key");

        if (key != null) {
            Object value = stateManager.getState(sessionId, key);
            return Map.of("key", key, "value", value);
        }
        return stateManager.getAllStates(sessionId);
    }

    /**
     * 状态写入接口
     */
    @PostMapping("/setState")
    @ApiOperation("设置会话状态")
    public String setState(@RequestBody Map<String, Object> body) {
        String sessionId = (String) body.getOrDefault("sessionId", "");
        String key = (String) body.get("key");
        Object value = body.get("value");

        stateManager.setState(sessionId, key, value);
        return "ok";
    }
}
