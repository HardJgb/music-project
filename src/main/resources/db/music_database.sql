-- 修改playlist表，添加category字段
ALTER TABLE `playlist` ADD COLUMN `category` varchar(50) DEFAULT NULL COMMENT '歌单分类';

-- 更新现有歌单的分类
UPDATE `playlist` SET `category` = '私人推荐' WHERE `id` = 1;
UPDATE `playlist` SET `category` = '每日推荐' WHERE `id` = 2;
UPDATE `playlist` SET `category` = '怀旧华语' WHERE `id` = 3;
UPDATE `playlist` SET `category` = '欧美流行' WHERE `id` = 4;
UPDATE `playlist` SET `category` = 'K-Pop' WHERE `id` = 5;

-- 添加新的分类歌单
INSERT INTO `playlist` VALUES (6, '伤感歌单', '/public/image/sad-playlist.jpg', '适合一个人静静听的歌', '2025-04-07 10:00:00', '2025-04-07 10:00:00', 12345, 1, '伤感');
INSERT INTO `playlist` VALUES (7, '励志歌单', '/public/image/motivational.jpg', '给你满满的正能量', '2025-04-07 10:00:00', '2025-04-07 10:00:00', 23456, 1, '励志');
INSERT INTO `playlist` VALUES (8, '流行热歌', '/public/image/popular.jpg', '当下最热门的流行歌曲', '2025-04-07 10:00:00', '2025-04-07 10:00:00', 34567, 1, '流行');
INSERT INTO `playlist` VALUES (9, '经典老歌', '/public/image/classics.jpg', '经典永不过时', '2025-04-07 10:00:00', '2025-04-07 10:00:00', 45678, 1, '经典');
INSERT INTO `playlist` VALUES (10, '轻音乐', '/public/image/light-music.jpg', '轻松舒缓的音乐', '2025-04-07 10:00:00', '2025-04-07 10:00:00', 56789, 1, '轻音乐');

-- 为新歌单添加歌曲关联
INSERT INTO `playlist_song` VALUES (9, 6, 1);  -- 伤感歌单 - 日落大道
INSERT INTO `playlist_song` VALUES (10, 6, 4); -- 伤感歌单 - 起风了
INSERT INTO `playlist_song` VALUES (11, 7, 5); -- 励志歌单 - Dynamite
INSERT INTO `playlist_song` VALUES (12, 8, 2); -- 流行热歌 - Lemon
INSERT INTO `playlist_song` VALUES (13, 8, 5); -- 流行热歌 - Dynamite
INSERT INTO `playlist_song` VALUES (14, 9, 1); -- 经典老歌 - 日落大道
INSERT INTO `playlist_song` VALUES (15, 10, 3); -- 轻音乐 - Shape of You 