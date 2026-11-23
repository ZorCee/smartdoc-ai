-- =================================================================
-- SmartDoc-AI 企业级智能知识库与 RAG 协同问答平台 - 数据库初始化脚本
-- =================================================================

CREATE DATABASE IF NOT EXISTS `smartdoc_ai` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `smartdoc_ai`;

-- 1. 系统用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(64) NOT NULL COMMENT '登录用户名',
    `password` VARCHAR(128) NOT NULL COMMENT '登录密码',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '用户昵称',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '电子邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN-管理员, USER-普通用户',
    `status` INT NOT NULL DEFAULT 1 COMMENT '账号状态: 1-正常, 0-禁用',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 知识库主表
CREATE TABLE IF NOT EXISTS `kb_knowledge_base` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '知识库主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '知识库名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '知识库简介描述',
    `cover_icon` VARCHAR(255) DEFAULT NULL COMMENT '知识库图标',
    `owner_id` BIGINT NOT NULL COMMENT '创建人/所属用户ID',
    `embedding_model` VARCHAR(64) NOT NULL DEFAULT 'text-embedding-v3' COMMENT '向量模型名称',
    `is_public` INT NOT NULL DEFAULT 0 COMMENT '可见性: 0-私有, 1-公开',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能知识库表';

-- 3. 知识库文档管理表
CREATE TABLE IF NOT EXISTS `kb_document` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文档主键ID',
    `kb_id` BIGINT NOT NULL COMMENT '所属知识库ID',
    `name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件在服务器磁盘的物理路径',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件字节大小',
    `file_type` VARCHAR(30) NOT NULL COMMENT '文件类型(pdf, docx, txt, md等)',
    `status` INT NOT NULL DEFAULT 0 COMMENT '处理状态: 0-已上传, 1-解析中, 2-向量化中, 3-已就绪, 4-失败',
    `word_count` INT NOT NULL DEFAULT 0 COMMENT '提取出的纯文字字数',
    `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '处理失败时的错误原因',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0-正常, 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_kb_id` (`kb_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档管理表';

-- 4. 文档文本切片表 (RAG 检索核心表)
CREATE TABLE IF NOT EXISTS `kb_document_chunk` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '切片主键ID',
    `kb_id` BIGINT NOT NULL COMMENT '所属知识库ID(冗余字段,加速按库检索)',
    `doc_id` BIGINT NOT NULL COMMENT '所属文档ID',
    `chunk_index` INT NOT NULL DEFAULT 0 COMMENT '切片在文档中的顺序编号(0, 1, 2...)',
    `content` TEXT NOT NULL COMMENT '切片纯文本内容',
    `char_count` INT NOT NULL DEFAULT 0 COMMENT '当前切片字符数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_kb_id` (`kb_id`),
    INDEX `idx_doc_id` (`doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档文本切片表';

-- 5. AI 问答历史记录表
CREATE TABLE IF NOT EXISTS `kb_chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '对话消息主键ID',
    `kb_id` BIGINT NOT NULL COMMENT '所属知识库ID',
    `user_id` BIGINT NOT NULL COMMENT '提问用户ID',
    `question` TEXT NOT NULL COMMENT '用户提出的问题',
    `answer` TEXT NOT NULL COMMENT 'AI基于知识库生成的回答',
    `matched_chunks` TEXT DEFAULT NULL COMMENT '本次回答引用的知识切片内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '对话时间',
    PRIMARY KEY (`id`),
    INDEX `idx_kb_user` (`kb_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库AI问答历史记录表';
