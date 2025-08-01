-- 创建专辑表
CREATE TABLE IF NOT EXISTS `album` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '专辑名称',
  `artist` varchar(255) NOT NULL COMMENT '艺术家名称',
  `description` text COMMENT '专辑描述',
  `cover_img` varchar(255) DEFAULT NULL COMMENT '专辑封面图片路径',
  `release_date` datetime DEFAULT NULL COMMENT '发行日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_artist` (`artist`),
  KEY `idx_release_date` (`release_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专辑信息表';

-- 创建专辑歌曲关联表
CREATE TABLE IF NOT EXISTS `album_song` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `album_id` int(11) NOT NULL COMMENT '专辑ID',
  `song_id` int(11) NOT NULL COMMENT '歌曲ID',
  `track_number` int(11) DEFAULT '0' COMMENT '曲目编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_album_song` (`album_id`,`song_id`),
  KEY `idx_album_id` (`album_id`),
  KEY `idx_song_id` (`song_id`),
  CONSTRAINT `fk_album_song_album` FOREIGN KEY (`album_id`) REFERENCES `album` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_album_song_song` FOREIGN KEY (`song_id`) REFERENCES `song` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专辑歌曲关联表';

-- 创建初始专辑数据的存储过程
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS `import_artist_albums`()
BEGIN
    -- 从歌曲表导入艺术家专辑
    INSERT INTO album (name, artist, release_date, create_time)
    SELECT DISTINCT 
        CONCAT(artist, ' - 精选集') as name, 
        artist, 
        NOW() as release_date,
        NOW() as create_time
    FROM song 
    WHERE artist NOT IN (
        SELECT DISTINCT artist FROM album
    );
    
    -- 获取新创建的专辑
    SET @row_number = 0;
    DROP TEMPORARY TABLE IF EXISTS temp_new_albums;
    CREATE TEMPORARY TABLE temp_new_albums AS
    SELECT id, artist FROM album WHERE id NOT IN (
        SELECT DISTINCT album_id FROM album_song
    );
    
    -- 为每个新专辑添加对应艺术家的歌曲
    INSERT INTO album_song (album_id, song_id, track_number)
    SELECT a.id as album_id, s.id as song_id, 
           ROW_NUMBER() OVER (PARTITION BY a.id ORDER BY s.id) as track_number
    FROM temp_new_albums a
    JOIN song s ON a.artist = s.artist;
    
    -- 清理临时表
    DROP TEMPORARY TABLE IF EXISTS temp_new_albums;
END //
DELIMITER ;

-- 执行存储过程导入初始数据
-- CALL import_artist_albums();

-- 修复AlbumMapper.xml中的方法名称不匹配问题
-- 1. 将findLatestAlbums改为findLatest
-- 2. 将findByName改为searchByName 