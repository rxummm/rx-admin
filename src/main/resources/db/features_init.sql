-- =====================================================
-- RX Admin 新功能数据库初始化脚本
-- 包含: IP黑白名单、站内消息、快捷收藏
-- =====================================================

-- IP黑白名单表
CREATE TABLE IF NOT EXISTS sys_ip_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(50) NOT NULL COMMENT 'IP地址或网段',
    rule_type VARCHAR(10) NOT NULL DEFAULT 'BLACK' COMMENT '规则类型: BLACK/WHITE',
    description VARCHAR(255) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IP黑白名单规则';

-- 站内消息表
CREATE TABLE IF NOT EXISTS sys_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT COMMENT '发送者ID，0=系统消息',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    sender_name VARCHAR(50) COMMENT '发送者名称',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    message_type VARCHAR(20) DEFAULT 'system' COMMENT '消息类型: system/notice/info',
    is_read TINYINT DEFAULT 0 COMMENT '0=未读 1=已读',
    read_time DATETIME COMMENT '阅读时间',
    link_path VARCHAR(200) COMMENT '关联跳转链接',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_receiver_read (receiver_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息';

-- 快捷收藏表
CREATE TABLE IF NOT EXISTS sys_user_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    menu_id BIGINT COMMENT '关联菜单ID',
    name VARCHAR(100) NOT NULL COMMENT '收藏名称',
    path VARCHAR(200) NOT NULL COMMENT '路由路径',
    icon VARCHAR(100) COMMENT '图标名称',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户快捷收藏';
