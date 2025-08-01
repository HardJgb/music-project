-- 创建用户歌曲播放统计表
CREATE TABLE IF NOT EXISTS `user_song_play_count` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `song_id` int(11) NOT NULL COMMENT '歌曲ID',
  `play_count` int(11) NOT NULL DEFAULT 1 COMMENT '播放次数',
  `last_play_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后播放时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_song` (`user_id`, `song_id`),
  KEY `idx_user_count` (`user_id`, `play_count` DESC),
  KEY `idx_last_play_time` (`user_id`, `last_play_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户歌曲播放统计'; 