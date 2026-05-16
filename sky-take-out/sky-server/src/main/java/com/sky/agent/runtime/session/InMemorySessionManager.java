package com.sky.agent.runtime.session;

import dev.langchain4j.data.message.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "sky.agent.session.backend", havingValue = "memory", matchIfMissing = true)
@Slf4j
public class InMemorySessionManager implements SessionManager {

    private final ConcurrentHashMap<String, List<ChatMessage>> messages = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> states = new ConcurrentHashMap<>();

    @Override
    public List<ChatMessage> getMessages(String sessionId) {
        return messages.getOrDefault(sessionId, new ArrayList<>());
    }

    @Override
    public void addMessage(String sessionId, ChatMessage message) {
        messages.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
    }

    @Override
    public void clearSession(String sessionId) {
        messages.remove(sessionId);
        states.remove(sessionId);
        log.debug("Session cleared: {}", sessionId);
    }

    @Override
    public void setState(String sessionId, String key, Object value) {
        states.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    @Override
    public Object getState(String sessionId, String key) {
        ConcurrentHashMap<String, Object> sessionStates = states.get(sessionId);
        return sessionStates != null ? sessionStates.get(key) : null;
    }

    @Override
    public void removeState(String sessionId, String key) {
        ConcurrentHashMap<String, Object> sessionStates = states.get(sessionId);
        if (sessionStates != null) {
            sessionStates.remove(key);
        }
    }

    @Override
    public void expireSession(String sessionId, long ttlSeconds) {
        // InMemory mode: no TTL support, rely on manual clearSession
    }
}
