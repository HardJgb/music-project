-- 检查status字段是否存在，如果不存在则添加
ALTER TABLE user ADD COLUMN IF NOT EXISTS status INT DEFAULT 1 COMMENT '用户状态: 0-禁用, 1-正常'; 