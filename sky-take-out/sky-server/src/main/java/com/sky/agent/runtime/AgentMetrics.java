package com.sky.agent.runtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class AgentMetrics {

    private final ConcurrentHashMap<String, AtomicLong> agentCallCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> toolCallCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> agentErrorCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> totalLatencyMs = new ConcurrentHashMap<>();

    public void recordAgentCall(String agentName, long latencyMs) {
        agentCallCount.computeIfAbsent(agentName, k -> new AtomicLong()).incrementAndGet();
        totalLatencyMs.computeIfAbsent(agentName, k -> new AtomicLong()).addAndGet(latencyMs);
    }

    public void recordAgentError(String agentName) {
        agentErrorCount.computeIfAbsent(agentName, k -> new AtomicLong()).incrementAndGet();
    }

    public void recordToolCall(String toolName) {
        toolCallCount.computeIfAbsent(toolName, k -> new AtomicLong()).incrementAndGet();
    }

    public long getAgentCallCount(String agentName) {
        AtomicLong counter = agentCallCount.get(agentName);
        return counter != null ? counter.get() : 0;
    }

    public long getToolCallCount(String toolName) {
        AtomicLong counter = toolCallCount.get(toolName);
        return counter != null ? counter.get() : 0;
    }

    public double getAvgLatencyMs(String agentName) {
        AtomicLong calls = agentCallCount.get(agentName);
        AtomicLong latency = totalLatencyMs.get(agentName);
        if (calls == null || calls.get() == 0 || latency == null) return 0;
        return (double) latency.get() / calls.get();
    }

    public Map<String, Long> getAgentStats() {
        Map<String, Long> stats = new ConcurrentHashMap<>();
        for (String name : agentCallCount.keySet()) {
            stats.put(name + ".calls", agentCallCount.get(name).get());
            stats.put(name + ".errors", agentErrorCount.getOrDefault(name, new AtomicLong()).get());
            stats.put(name + ".avgLatencyMs", (long) getAvgLatencyMs(name));
        }
        return stats;
    }

    public Map<String, Long> getToolStats() {
        Map<String, Long> stats = new ConcurrentHashMap<>();
        for (String name : toolCallCount.keySet()) {
            stats.put(name, toolCallCount.get(name).get());
        }
        return stats;
    }
}
