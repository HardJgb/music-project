# 歌词功能实现说明

## 数据库设计建议

为了存储歌词，建议在数据库中创建一个专门的表，如下所示：

```sql
CREATE TABLE songs_lyrics (
  id INT PRIMARY KEY AUTO_INCREMENT,
  song_id INT NOT NULL,
  content TEXT NOT NULL,
  format ENUM('LRC', 'PLAIN') DEFAULT 'LRC',
  source VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE
);
```

表字段说明：
- `id`：主键
- `song_id`：关联的歌曲ID（外键）
- `content`：歌词内容，存储完整的LRC格式歌词
- `format`：歌词格式，支持LRC带时间戳或纯文本格式
- `source`：歌词来源（如网站、用户上传等）
- `created_at`/`updated_at`：创建和更新时间

## 后端API实现

建议在后端实现以下API：

1. 获取歌词：
```
GET /api/songs/:id/lyrics
```

2. 上传/更新歌词（仅管理员）：
```
POST /api/songs/:id/lyrics
```

## 前端实现

当前实现的特点：

1. **隐藏滚动条**：使用CSS隐藏滚动条，保持美观但不影响功能
   ```css
   scrollbar-width: none; /* Firefox */
   -ms-overflow-style: none; /* IE and Edge */
   ```
   
   ```css
   ::-webkit-scrollbar {
     display: none; /* Chrome, Safari, Opera */
   }
   ```

2. **LRC格式解析**：解析带时间戳的LRC格式歌词
   - 提取时间信息：`[mm:ss.xx]`
   - 提取元数据信息：`[ti:标题]`、`[ar:艺术家]`等
   - 按时间顺序排序

3. **自动同步**：根据播放进度自动高亮当前歌词并滚动到可见位置

4. **回退方案**：当歌曲没有歌词时提供默认歌词或提示

## 管理员功能建议

建议添加一个管理员面板，提供以下功能：

1. 上传/编辑LRC格式歌词
2. 预览歌词与音乐的同步效果
3. 批量导入歌词
4. 从第三方API获取歌词 