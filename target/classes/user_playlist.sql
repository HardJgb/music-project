-- 个人歌单表
CREATE TABLE IF NOT EXISTS `user_playlist` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `name` varchar(100) NOT NULL COMMENT '歌单名称',
  `cover_img` varchar(255) DEFAULT NULL COMMENT '歌单封面',
  `description` text COMMENT '歌单描述',
  `is_private` tinyint(1) DEFAULT '0' COMMENT '是否私密歌单',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户创建的歌单';

-- 个人歌单歌曲关联表
CREATE TABLE IF NOT EXISTS `user_playlist_song` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_playlist_id` int(11) NOT NULL COMMENT '用户歌单ID',
  `song_id` int(11) NOT NULL COMMENT '歌曲ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_playlist_song` (`user_playlist_id`,`song_id`),
  KEY `idx_user_playlist_id` (`user_playlist_id`),
  KEY `idx_song_id` (`song_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户歌单歌曲关联表';

-- 初始数据示例（可选）
-- INSERT INTO `user_playlist` (`user_id`, `name`, `description`, `is_private`) VALUES
-- (1, '我喜欢的音乐', '我最喜欢的歌曲集合', 0),
-- (1, '我的私人歌单', '只有我能看到的歌单', 1); 