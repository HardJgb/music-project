# 音乐平台歌词存储解决方案

## 概述

本方案提供了将歌词存储到数据库的完整解决方案，包括表结构设计、数据插入和查询方法。该方案适用于需要存储和管理LRC格式歌词的音乐播放平台。

## 文件说明

- `lyrics_insertion.sql`: 包含创建歌词表、插入示例歌词数据和创建便于查询的视图的SQL脚本

## 表结构设计

`songs_lyrics`表设计包括以下字段：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 主键，自动递增 |
| song_id | INT | 外键，关联到song表的id |
| content | TEXT | 歌词内容，存储完整的LRC格式内容 |
| format | ENUM | 歌词格式，可选"LRC"或"PLAIN" |
| source | VARCHAR | 歌词来源信息 |
| created_at | TIMESTAMP | 记录创建时间 |
| updated_at | TIMESTAMP | 记录更新时间 |

表使用`utf8mb4`字符集确保支持所有中文字符和emoji。

## 使用说明

### 创建表结构

执行以下命令创建歌词表：

```sql
CREATE TABLE IF NOT EXISTS songs_lyrics (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '歌词记录ID',
    song_id INT NOT NULL COMMENT '关联的歌曲ID（对应song表的id）',
    content TEXT NOT NULL COMMENT '歌词内容（LRC格式或纯文本）',
    format ENUM('LRC', 'PLAIN') DEFAULT 'LRC' COMMENT '歌词格式：LRC（带时间戳）或PLAIN（纯文本）',
    source VARCHAR(255) COMMENT '歌词来源',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (song_id) REFERENCES song(id) ON DELETE CASCADE COMMENT '外键约束，关联song表'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='歌曲歌词存储表';
```

### 插入歌词数据

插入歌词前，需要确认对应歌曲在`song`表中的ID，然后执行插入操作：

```sql
INSERT INTO songs_lyrics (song_id, content, format, source) VALUES (
    歌曲ID,
    'LRC格式的歌词内容',
    'LRC',
    '歌词来源'
);
```

### 查询歌词

可以通过以下SQL查询特定歌曲的歌词：

```sql
SELECT content FROM songs_lyrics WHERE song_id = 歌曲ID;
```

或使用视图获取歌曲的所有信息和歌词：

```sql
SELECT * FROM v_song_with_lyrics WHERE id = 歌曲ID;
```

## 前端集成

前端可以从后端API获取歌词，并使用LRC解析器处理时间戳和歌词内容，实现歌词同步显示功能。

## 注意事项

1. 执行脚本前请确保`song`表已存在
2. 确保要插入的`song_id`在`song`表中存在
3. 备份数据库后再执行脚本
4. 确保有足够的权限执行创建表和插入数据的操作 