package com.sky.agent.runtime.protocol.agui;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AguiEvent {

    private String event; // text_delta, tool_call_start, tool_call_result, state_delta, ui_render, done, error
    private String data;
    private String agentName;
    private String toolName;
    private Map<String, Object> state;
    private String uiComponent; // FoodRecommendCard, OrderStatusCard, QuickActions, TextBubble
    private Map<String, Object> uiProps;

    public String toSseString() {
        StringBuilder sb = new StringBuilder();
        sb.append("event: ").append(event).append("\n");
        sb.append("data: ").append(JSON.toJSONString(this)).append("\n\n");
        return sb.toString();
    }

    public static AguiEvent textDelta(String text) {
        return AguiEvent.builder().event("text_delta").data(text).build();
    }

    public static AguiEvent toolCallStart(String agentName, String toolName) {
        return AguiEvent.builder()
                .event("tool_call_start")
                .agentName(agentName)
                .toolName(toolName)
                .build();
    }

    public static AguiEvent toolCallResult(String toolName, String result) {
        return AguiEvent.builder()
                .event("tool_call_result")
                .toolName(toolName)
                .data(result)
                .build();
    }

    public static AguiEvent stateDelta(Map<String, Object> state) {
        return AguiEvent.builder().event("state_delta").state(state).build();
    }

    public static AguiEvent uiRender(String component, Map<String, Object> props) {
        return AguiEvent.builder()
                .event("ui_render")
                .uiComponent(component)
                .uiProps(props)
                .build();
    }

    public static AguiEvent done() {
        return AguiEvent.builder().event("done").build();
    }

    public static AguiEvent error(String message) {
        return AguiEvent.builder().event("error").data(message).build();
    }
}
