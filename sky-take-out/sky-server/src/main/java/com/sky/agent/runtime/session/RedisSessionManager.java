package com.sky.agent.runtime.session;

import com.alibaba.fastjson.JSON;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "sky.agent.session.backend", havingValue = "redis")
@Slf4j
public class RedisSessionManager implements SessionManager {

    private static final String MSG_KEY_PREFIX = "agent:session:%s:messages";
    private static final String STATE_KEY_PREFIX = "agent:session:%s:state:%s";

    private final StringRedisTemplate redisTemplate;

    public RedisSessionManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<ChatMessage> getMessages(String sessionId) {
        String key = String.format(MSG_KEY_PREFIX, sessionId);
        List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);
        if (jsonList == null || jsonList.isEmpty()) {
            return new ArrayList<>();
        }
        return jsonList.stream()
                .map(ChatMessageDeserializer::messageFromJson)
                .collect(Collectors.toList());
    }

    @Override
    public void addMessage(String sessionId, ChatMessage message) {
        String key = String.format(MSG_KEY_PREFIX, sessionId);
        String json = ChatMessageSerializer.messageToJson(message);
        redisTemplate.opsForList().rightPush(key, json);
    }

    @Override
    public void clearSession(String sessionId) {
        String msgKey = String.format(MSG_KEY_PREFIX, sessionId);
        redisTemplate.delete(msgKey);
        log.debug("Redis session cleared: {}", sessionId);
    }

    @Override
    public void setState(String sessionId, String key, Object value) {
        String stateKey = String.format(STATE_KEY_PREFIX, sessionId, key);
        redisTemplate.opsForValue().set(stateKey, JSON.toJSONString(value));
    }

    @Override
    public Object getState(String sessionId, String key) {
        String stateKey = String.format(STATE_KEY_PREFIX, sessionId, key);
        String json = redisTemplate.opsForValue().get(stateKey);
        return json != null ? JSON.parse(json) : null;
    }

    @Override
    public void removeState(String sessionId, String key) {
        String stateKey = String.format(STATE_KEY_PREFIX, sessionId, key);
        redisTemplate.delete(stateKey);
    }

    @Override
    public void expireSession(String sessionId, long ttlSeconds) {
        String msgKey = String.format(MSG_KEY_PREFIX, sessionId);
        redisTemplate.expire(msgKey, ttlSeconds, TimeUnit.SECONDS);
    }
}
