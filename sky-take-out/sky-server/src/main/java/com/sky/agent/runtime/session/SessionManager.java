package com.sky.agent.runtime.session;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public interface SessionManager {

    List<ChatMessage> getMessages(String sessionId);

    void addMessage(String sessionId, ChatMessage message);

    void clearSession(String sessionId);

    void setState(String sessionId, String key, Object value);

    Object getState(String sessionId, String key);

    void removeState(String sessionId, String key);

    void expireSession(String sessionId, long ttlSeconds);
}
