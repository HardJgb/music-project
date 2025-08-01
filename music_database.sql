/*
 Navicat Premium Data Transfer

 Source Server         : jing
 Source Server Type    : MySQL
 Source Server Version : 80035
 Source Host           : localhost:3306
 Source Schema         : music_database

 Target Server Type    : MySQL
 Target Server Version : 80035
 File Encoding         : 65001

 Date: 07/04/2025 10:59:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for artist
-- ----------------------------
DROP TABLE IF EXISTS `artist`;
CREATE TABLE `artist`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of artist
-- ----------------------------
INSERT INTO `artist` VALUES (1, '梁博', 'http://localhost:8085/public/image/liangbo.jpg', '防弹少年团（Base Transceiver Station），是娱乐公司 BigHit 2013年6月13日推出的 韩国男子音乐团体');
INSERT INTO `artist` VALUES (2, '米津玄师', 'http://localhost:8085/public/image/mijinxuanshi.jpg', NULL);
INSERT INTO `artist` VALUES (3, 'Ed Sheeran', 'http://localhost:8085/public/image/EdSheeran.jpg', NULL);
INSERT INTO `artist` VALUES (4, '买辣椒也用券', 'http://localhost:8085/public/image/买辣椒也用券.jpg', NULL);
INSERT INTO `artist` VALUES (5, 'BTS', 'http://localhost:8085/public/image/BTS.jpg', NULL);

-- ----------------------------
-- Table structure for c_user
-- ----------------------------
DROP TABLE IF EXISTS `c_user`;
CREATE TABLE `c_user`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `password` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of c_user
-- ----------------------------
INSERT INTO `c_user` VALUES (1, 'wjj', '123456');

-- ----------------------------
-- Table structure for favorites
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `user_id` int(0) NOT NULL,
  `song_id` int(0) NOT NULL,
  `song_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `artist` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `album` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_favorite`(`user_id`, `song_id`) USING BTREE,
  INDEX `idx_favorites_user`(`user_id`) USING BTREE,
  INDEX `idx_favorites_song`(`song_id`) USING BTREE,
  CONSTRAINT `fk_favorites_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`uid`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorites
-- ----------------------------
INSERT INTO `favorites` VALUES (1, 6, 1, '日落大道', '周杰伦', '', 'http://localhost:8085/public/image/日落大道.jpg', '2025-04-04 14:49:44');
INSERT INTO `favorites` VALUES (2, 6, 4, '起风了', '买辣椒也用券', '', 'http://localhost:8085/public/image/起风了.jpg', '2025-04-04 15:02:15');
INSERT INTO `favorites` VALUES (3, 16, 1, '日落大道', '周杰伦', '', 'http://localhost:8085/public/image/日落大道.jpg', '2025-04-04 16:41:18');
INSERT INTO `favorites` VALUES (4, 16, 4, '起风了', '买辣椒也用券', '', 'http://localhost:8085/public/image/起风了.jpg', '2025-04-04 16:41:19');
INSERT INTO `favorites` VALUES (5, 16, 2, 'Lemon', '米津玄师', '', 'http://localhost:8085/public/image/Lemon.jpg', '2025-04-07 09:03:36');
INSERT INTO `favorites` VALUES (6, 16, 3, 'Shape of You', 'Ed Sheeran', '', 'http://localhost:8085/public/image/Shape of You.jpg', '2025-04-07 09:03:41');
INSERT INTO `favorites` VALUES (7, 16, 7, '男孩', '梁博', '', 'http://localhost:8085/public/image/男孩.jpg', '2025-04-07 10:51:17');

-- ----------------------------
-- Table structure for playlist
-- ----------------------------
DROP TABLE IF EXISTS `playlist`;
CREATE TABLE `playlist`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '歌单名称',
  `cover_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '歌单封面图片地址',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '歌单描述',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `play_count` int(0) NULL DEFAULT 0 COMMENT '播放次数',
  `is_recommend` tinyint(1) NULL DEFAULT 0 COMMENT '是否推荐，0-否，1-是',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '歌单分类',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of playlist
-- ----------------------------
INSERT INTO `playlist` VALUES (1, '私人雷达', '/public/image/jielun.jpg', '根据你的口味生成的私人歌单', '2025-04-04 10:17:23', '2025-04-07 09:07:07', 56789, 1, '私人推荐');
INSERT INTO `playlist` VALUES (2, '每日推荐', '/public/image/please-2697951_1280.jpg', '每天都有新音乐推荐', '2025-04-04 10:17:23', '2025-04-07 09:07:07', 123456, 1, '每日推荐');
INSERT INTO `playlist` VALUES (3, '怀旧华语经典', '/public/image/artist-3480274_1280.jpg', '那些年我们一起听过的华语经典', '2025-04-04 10:17:23', '2025-04-07 09:07:07', 789012, 1, '怀旧华语');
INSERT INTO `playlist` VALUES (4, '欧美流行', '/public/image/concert-1854113_1280.jpg', '最热门的欧美流行歌曲', '2025-04-04 10:17:23', '2025-04-07 09:07:07', 234567, 1, '欧美流行');
INSERT INTO `playlist` VALUES (5, 'K-Pop精选', '/public/image/band-4671748_1280.jpg', '韩流音乐精选集', '2025-04-04 10:17:23', '2025-04-07 09:07:07', 345678, 1, 'K-Pop');
INSERT INTO `playlist` VALUES (6, '伤感歌单', '/public/image/伤感封面.jpg', '适合一个人静静听的歌', '2025-04-07 10:00:00', '2025-04-07 09:21:15', 12345, 1, '伤感');
INSERT INTO `playlist` VALUES (7, '励志歌单', '/public/image/motivational.jpg', '给你满满的正能量', '2025-04-07 10:00:00', '2025-04-07 10:00:00', 23456, 1, '励志');
INSERT INTO `playlist` VALUES (8, '流行热歌', '/public/image/popular.jpg', '当下最热门的流行歌曲', '2025-04-07 10:00:00', '2025-04-07 10:00:00', 34567, 1, '流行');
INSERT INTO `playlist` VALUES (9, '经典老歌', '/public/image/classics.png', '经典永不过时', '2025-04-07 10:00:00', '2025-04-07 09:23:44', 45678, 1, '经典');
INSERT INTO `playlist` VALUES (10, '轻音乐', '/public/image/light-music.jpg', '轻松舒缓的音乐', '2025-04-07 10:00:00', '2025-04-07 10:00:00', 56789, 1, '轻音乐');

-- ----------------------------
-- Table structure for playlist_song
-- ----------------------------
DROP TABLE IF EXISTS `playlist_song`;
CREATE TABLE `playlist_song`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `playlist_id` int(0) NOT NULL,
  `song_id` int(0) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `playlist_id`(`playlist_id`) USING BTREE,
  INDEX `song_id`(`song_id`) USING BTREE,
  CONSTRAINT `playlist_song_ibfk_1` FOREIGN KEY (`playlist_id`) REFERENCES `playlist` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `playlist_song_ibfk_2` FOREIGN KEY (`song_id`) REFERENCES `song` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of playlist_song
-- ----------------------------
INSERT INTO `playlist_song` VALUES (1, 1, 1);
INSERT INTO `playlist_song` VALUES (2, 1, 4);
INSERT INTO `playlist_song` VALUES (3, 2, 2);
INSERT INTO `playlist_song` VALUES (4, 2, 3);
INSERT INTO `playlist_song` VALUES (5, 3, 1);
INSERT INTO `playlist_song` VALUES (6, 3, 4);
INSERT INTO `playlist_song` VALUES (7, 4, 3);
INSERT INTO `playlist_song` VALUES (8, 5, 5);
INSERT INTO `playlist_song` VALUES (9, 6, 1);
INSERT INTO `playlist_song` VALUES (10, 6, 4);
INSERT INTO `playlist_song` VALUES (11, 7, 5);
INSERT INTO `playlist_song` VALUES (12, 8, 2);
INSERT INTO `playlist_song` VALUES (13, 8, 5);
INSERT INTO `playlist_song` VALUES (14, 9, 1);
INSERT INTO `playlist_song` VALUES (15, 3, 6);
INSERT INTO `playlist_song` VALUES (16, 2, 7);

-- ----------------------------
-- Table structure for song
-- ----------------------------
DROP TABLE IF EXISTS `song`;
CREATE TABLE `song`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '歌曲名称',
  `artist` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '歌手名称',
  `duration` int(0) NULL DEFAULT NULL COMMENT '歌曲时长(秒)',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '歌曲文件地址',
  `cover_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '歌曲封面图片地址',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `lyric_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '歌词文件地址',
  `play_count` int(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of song
-- ----------------------------
INSERT INTO `song` VALUES (1, '日落大道', '梁博', 289, '/public/image/日落大道.mp3', '/public/image/日落大道.jpg', '2025-04-04 10:17:23', NULL, 8);
INSERT INTO `song` VALUES (2, 'Lemon', '米津玄师', 255, '/public/image/Lemon.mp3', '/public/image/Lemon.jpg', '2025-04-04 10:17:23', NULL, 5);
INSERT INTO `song` VALUES (3, 'Shape of You', 'Ed Sheeran', 233, '/public/image/ShapeOfYou.mp3', '/public/image/Shape of You.jpg', '2025-04-04 10:17:23', NULL, 4);
INSERT INTO `song` VALUES (4, '起风了', '买辣椒也用券', 325, '/public/image/起风了.mp3', '/public/image/起风了.jpg', '2025-04-04 10:17:23', NULL, 0);
INSERT INTO `song` VALUES (5, 'Dynamite', 'BTS', 198, '/public/image/Dynamite.mp3', '/public/image/Dynamite.jpg', '2025-04-04 10:17:23', NULL, 1);
INSERT INTO `song` VALUES (6, '曾经是情侣', '梁博', 403, '/public/image/M800000bAGcY1aisjY.mp3', '/public/image/曾经是情侣.jpg', '2025-04-07 10:39:13', NULL, 0);
INSERT INTO `song` VALUES (7, '男孩', '梁博', 329, '/public/image/M800000zdYVD4TZ6YW.mp3', '/public/image/男孩.jpg', '2025-04-07 10:44:42', NULL, 1);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `uid` int(0) NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `user_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户登录名',
  `user_pwd` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户登录密码',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (6, 'wjj', 'e10adc3949ba59abbe56e057f20f883e');
INSERT INTO `user` VALUES (16, 'jing', 'e10adc3949ba59abbe56e057f20f883e');

SET FOREIGN_KEY_CHECKS = 1;
