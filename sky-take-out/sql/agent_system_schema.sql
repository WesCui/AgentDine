-- ============================================================
-- Sky Takeout Multi-Agent System Schema
-- 多Agent智能外卖平台数据库表
-- ============================================================

-- Agent 会话表
CREATE TABLE IF NOT EXISTS agent_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL UNIQUE COMMENT '会话唯一标识',
    agent_name VARCHAR(64) NOT NULL COMMENT '最后处理的Agent名称',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status TINYINT DEFAULT 1 COMMENT '1:active, 0:archived',
    message_count INT DEFAULT 0 COMMENT '消息总数',
    total_tokens_used INT DEFAULT 0 COMMENT '消耗Token总数',
    last_message_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '最后消息时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_agent (user_id, agent_name),
    INDEX idx_session (session_id),
    INDEX idx_last_message (last_message_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent会话表';

-- Agent 消息历史表（用于 MySQL 会话后端）
CREATE TABLE IF NOT EXISTS agent_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    role VARCHAR(16) NOT NULL COMMENT 'system/user/assistant/tool',
    content TEXT NOT NULL COMMENT '消息内容',
    tool_name VARCHAR(128) COMMENT '关联的工具名称（仅tool类型消息）',
    tool_call_id VARCHAR(64) COMMENT '工具调用ID',
    token_count INT DEFAULT 0 COMMENT 'Token估算数',
    metadata JSON COMMENT '扩展元数据',
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '精确到毫秒',
    INDEX idx_session_time (session_id, created_at),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent消息历史表';

-- 用户偏好记忆表
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    taste_preferences JSON COMMENT '口味偏好 {"spicy":0.8, "sweet":0.3}',
    favorite_categories JSON COMMENT '收藏分类ID列表',
    budget_range VARCHAR(32) COMMENT '预算区间 15-30',
    dietary_restrictions JSON COMMENT '饮食禁忌',
    order_frequency INT DEFAULT 0 COMMENT '下单频次（次/月）',
    avg_order_amount DECIMAL(10,2) COMMENT '平均订单金额',
    preferred_time_slots JSON COMMENT '偏好时段',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好记忆表';

-- Agent 工具调用审计日志
CREATE TABLE IF NOT EXISTS agent_tool_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    agent_name VARCHAR(64) NOT NULL COMMENT 'Agent名称',
    tool_name VARCHAR(128) NOT NULL COMMENT '工具名称',
    tool_input TEXT COMMENT '工具输入参数（JSON）',
    tool_output MEDIUMTEXT COMMENT '工具输出结果',
    execution_time_ms INT COMMENT '执行耗时（毫秒）',
    status VARCHAR(16) DEFAULT 'success' COMMENT 'success/error/timeout',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '精确到毫秒',
    INDEX idx_session (session_id),
    INDEX idx_agent_time (agent_name, created_at),
    INDEX idx_tool (tool_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent工具调用审计日志';

-- 客服工单表（CustomerServiceAgent 使用）
CREATE TABLE IF NOT EXISTS support_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(64) COMMENT '关联的Agent会话ID',
    ticket_type VARCHAR(32) DEFAULT 'other' COMMENT 'complaint/refund/feedback/other',
    status VARCHAR(16) DEFAULT 'open' COMMENT 'open/processing/resolved/closed/escalated',
    priority TINYINT DEFAULT 2 COMMENT '1:low, 2:normal, 3:high, 4:urgent',
    title VARCHAR(256) NOT NULL COMMENT '工单标题',
    description TEXT COMMENT '问题描述',
    resolution TEXT COMMENT '处理结果',
    assigned_to VARCHAR(64) COMMENT '处理人',
    escalated_to_human BOOLEAN DEFAULT FALSE COMMENT '是否转人工',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME COMMENT '解决时间',
    INDEX idx_user (user_id),
    INDEX idx_status (status, priority),
    INDEX idx_type (ticket_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服工单表';
