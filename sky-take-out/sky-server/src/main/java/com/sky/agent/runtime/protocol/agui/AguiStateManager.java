package com.sky.agent.runtime.protocol.agui;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class AguiStateManager {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> states = new ConcurrentHashMap<>();

    public void setState(String sessionId, String key, Object value) {
        states.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>()).put(key, value);
        log.debug("AG-UI state set: session={}, key={}", sessionId, key);
    }

    public Object getState(String sessionId, String key) {
        ConcurrentHashMap<String, Object> sessionStates = states.get(sessionId);
        return sessionStates != null ? sessionStates.get(key) : null;
    }

    public Map<String, Object> getAllStates(String sessionId) {
        return states.getOrDefault(sessionId, new ConcurrentHashMap<>());
    }

    public void removeState(String sessionId, String key) {
        ConcurrentHashMap<String, Object> sessionStates = states.get(sessionId);
        if (sessionStates != null) {
            sessionStates.remove(key);
        }
    }

    public void clearSession(String sessionId) {
        states.remove(sessionId);
        log.debug("AG-UI state cleared: session={}", sessionId);
    }
}
