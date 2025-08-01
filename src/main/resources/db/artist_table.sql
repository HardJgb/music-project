-- 创建歌手表
CREATE TABLE IF NOT EXISTS artist (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    avatar_url VARCHAR(255),
    description TEXT
);

-- 导入已有歌手数据
INSERT INTO artist (name)
SELECT DISTINCT artist FROM song
WHERE artist NOT IN (SELECT name FROM artist); 