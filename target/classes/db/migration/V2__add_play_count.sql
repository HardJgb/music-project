-- 添加播放次数字段
ALTER TABLE song ADD COLUMN play_count INT DEFAULT 0;

-- 更新现有数据，设置默认播放次数
UPDATE song SET play_count = 0 WHERE play_count IS NULL; 