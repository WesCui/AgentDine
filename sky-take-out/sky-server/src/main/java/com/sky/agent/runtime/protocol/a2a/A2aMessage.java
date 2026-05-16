package com.sky.agent.runtime.protocol.a2a;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class A2aMessage {

    @Builder.Default
    private String messageId = UUID.randomUUID().toString().replace("-", "");

    private String fromAgent;
    private String toAgent;
    private String type; // QUERY, RESULT, TRANSFER, BROADCAST, ERROR
    private String content;
    @Builder.Default
    private Map<String, Object> context = new HashMap<>();
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
    private long timestamp;

    public static A2aMessage query(String from, String to, String content) {
        return A2aMessage.builder()
                .fromAgent(from)
                .toAgent(to)
                .type("QUERY")
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static A2aMessage result(String from, String to, String content) {
        return A2aMessage.builder()
                .fromAgent(from)
                .toAgent(to)
                .type("RESULT")
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static A2aMessage transfer(String from, String to, String content, Map<String, Object> context) {
        return A2aMessage.builder()
                .fromAgent(from)
                .toAgent(to)
                .type("TRANSFER")
                .content(content)
                .context(context)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static A2aMessage error(String from, String to, String errorContent) {
        return A2aMessage.builder()
                .fromAgent(from)
                .toAgent(to)
                .type("ERROR")
                .content(errorContent)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
