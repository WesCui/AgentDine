package com.sky.agent.controller;

import com.sky.agent.service.AgentChatService;
import com.sky.context.BaseContext;
import com.sky.dto.AgentChatDTO;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/agent")
@Api(tags = "智能推荐接口")
@Slf4j
public class AgentController {

    @Autowired
    private AgentChatService agentChatService;

    /**
     * 智能对话接口
     * 接收用户的文本消息，调用智能体进行处理，并返回回复
     *
     * @param dto 包含用户消息的传输对象
     * @return 智能体的回复内容
     */
    @PostMapping("/chat")
    @ApiOperation("智能对话")
    public Result<String> chat(@RequestBody AgentChatDTO dto) {
        Long userId = BaseContext.getCurrentId();
        // 如果无法获取当前用户ID（例如未登录状态下测试），则使用默认用户ID 1L
        // 这是为了方便演示和测试，生产环境应严格校验登录状态
        if (userId == null) {
             userId = 1L;
        }

        log.info("用户 {} 发起智能对话请求: {}", userId, dto.getMessage());
        try {
            String response = agentChatService.chat(userId, userId, dto.getMessage());
            return Result.success(response);
        } catch (Exception e) {
            log.error("智能对话服务异常", e);
            return Result.error("AI 服务暂不可用: " + e.getMessage());
        }
    }
}
