# 🎵 全栈音乐播放平台

一个基于 Vue 3 + Spring Boot 的现代化音乐播放平台，包含用户端、管理后台和完整的后端API服务。

## 📋 项目概述

本项目是一个功能完整的音乐播放平台，支持在线音乐播放、歌单管理、用户收藏、评论互动等功能。采用前后端分离架构，提供流畅的用户体验和强大的管理功能。

## 🏗️ 项目架构

```
全栈音乐播放平台/
├── vue-project/          # 前端用户界面 (Vue 3)
├── Music-project/         # 后端API服务 (Spring Boot)
├── background/           # 管理后台 (Vue 3)
├── music_database.sql    # 数据库结构文件
└── README.md            # 项目说明文档
```

## ✨ 主要功能

### 🎧 用户端功能 (vue-project)
- **音乐播放**: 在线播放、暂停、上一首/下一首
- **播放列表**: 动态播放队列管理
- **歌单浏览**: 推荐歌单、分类浏览、排行榜
- **用户系统**: 注册登录、个人中心、收藏管理
- **搜索功能**: 歌曲、歌手、专辑搜索
- **收藏功能**: 收藏歌曲、收藏歌单
- **最近播放**: 播放历史记录
- **评论系统**: 歌曲评论、点赞互动
- **响应式设计**: 适配各种屏幕尺寸

### 🔧 管理后台功能 (background)
- **数据统计**: Dashboard数据概览
- **用户管理**: 用户信息管理
- **歌曲管理**: 音乐文件上传、编辑、删除
- **歌单管理**: 歌单创建、编辑、分类管理
- **专辑管理**: 专辑信息维护
- **歌手管理**: 歌手信息管理
- **评论管理**: 评论内容审核
- **图片管理**: 封面图片管理

### 🚀 后端API (Music-project)
- **用户认证**: JWT Token身份验证
- **音乐服务**: 歌曲CRUD、播放统计
- **歌单服务**: 歌单管理、歌曲关联
- **收藏服务**: 用户收藏管理
- **评论服务**: 评论系统API
- **文件服务**: 音频文件、图片上传
- **搜索服务**: 多维度搜索API

## 🛠️ 技术栈

### 前端技术
- **框架**: Vue 3.5.12 + Composition API
- **构建工具**: Vite 5.4.10
- **UI组件**: Element Plus 2.9.7
- **状态管理**: Pinia 2.2.6
- **路由**: Vue Router 4.4.5
- **HTTP客户端**: Axios 1.8.4
- **样式**: CSS3 + Element Plus

### 后端技术
- **框架**: Spring Boot 3.0.5
- **JDK版本**: Java 17
- **数据访问**: MyBatis-Plus 3.5.3.1
- **数据库**: MySQL 8.0
- **连接池**: Druid
- **身份认证**: JWT (jjwt 0.9.1)
- **分页插件**: PageHelper 2.1.0
- **工具库**: Lombok 1.18.30

### 数据库设计
- **用户表** (user): 用户基本信息
- **歌曲表** (song): 音乐文件信息
- **歌单表** (playlist): 歌单信息
- **歌手表** (artist): 歌手信息  
- **收藏表** (favorites): 用户收藏
- **播放记录表** (recent): 最近播放
- **评论表** (comment): 歌曲评论
- **关联表**: 歌单歌曲关联等

### 视频展示

https://web-jgb.oss-cn-beijing.aliyuncs.com/20250801-0638-20.8796798.mp4

## 🚀 快速开始

### 环境要求

- Node.js 16+ 
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### 1. 数据库配置
```sql
# 创建数据库
CREATE DATABASE music_database CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据表结构和初始数据
mysql -u root -p music_database < music_database.sql
```

### 2. 后端服务启动

```bash
# 进入后端项目目录
cd Music-project

# 修改数据库配置 (src/main/resources/application.yml)
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/music_database
      username: your_username
      password: your_password

# 启动后端服务
mvn spring-boot:run

# 或者使用IDE运行 MusicApplication.java
```

后端服务将在 `http://localhost:8085` 启动

### 3. 前端用户界面启动

```bash
# 进入前端项目目录
cd vue-project

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端应用将在 `http://localhost:5173` 启动

### 4. 管理后台启动

```bash
# 进入管理后台目录
cd background

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

管理后台将在 `http://localhost:5174` 启动

## 📁 项目结构详解

### 前端项目结构 (vue-project)
```
vue-project/
├── src/
│   ├── components/          # 组件目录
│   │   ├── layout/         # 布局组件
│   │   │   ├── AppHeader.vue      # 顶部导航
│   │   │   ├── AppSidebar.vue     # 侧边栏
│   │   │   └── MusicPlayer.vue    # 音乐播放器
│   │   ├── auth/           # 认证组件
│   │   ├── song/           # 歌曲相关组件
│   │   └── playlist/       # 歌单相关组件
│   ├── views/              # 页面组件
│   │   ├── HomeView.vue    # 首页
│   │   ├── PlaylistDetail.vue     # 歌单详情
│   │   ├── UserProfile.vue        # 用户中心
│   │   └── ...
│   ├── stores/             # Pinia状态管理
│   │   ├── playerStore.js  # 播放器状态
│   │   └── userStore.js    # 用户状态
│   ├── api/                # API接口
│   ├── router/             # 路由配置
│   └── assets/             # 静态资源
├── package.json
└── vite.config.js
```

### 后端项目结构 (Music-project)
```
Music-project/
├── src/main/java/org/example/
│   ├── MusicApplication.java      # 启动类
│   ├── controller/               # 控制器层
│   │   ├── SongController.java   # 歌曲API
│   │   ├── UserController.java   # 用户API
│   │   ├── PlaylistController.java # 歌单API
│   │   └── ...
│   ├── service/                  # 服务层
│   ├── mapper/                   # 数据访问层
│   ├── pojo/                     # 实体类
│   │   ├── Song.java            # 歌曲实体
│   │   ├── User.java            # 用户实体
│   │   └── ...
│   ├── utils/                    # 工具类
│   ├── config/                   # 配置类
│   └── interceptor/              # 拦截器
├── src/main/resources/
│   ├── application.yml           # 配置文件
│   ├── mapper/                   # MyBatis映射文件
│   └── static/                   # 静态资源
└── pom.xml
```

## 🔧 配置说明

### 后端配置 (application.yml)
```yaml
server:
  port: 8085

spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/music_database
      username: root
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver

jwt:
  token:
    tokenExpiration: 240    # Token过期时间(分钟)
    tokenSignKey: 123456    # JWT签名密钥

imageUpload:
  basePath: /path/to/upload/  # 文件上传路径
  accessPath: http://localhost:8085/public/image/
```

### 前端API配置 (config.js)
```javascript
export const API_BASE_URL = 'http://localhost:8085';
```

## 🎯 API接口文档

### 用户相关
- `POST /user/login` - 用户登录
- `POST /user/register` - 用户注册
- `GET /user/profile` - 获取用户信息

### 歌曲相关  
- `GET /api/songs` - 获取歌曲列表
- `GET /api/songs/{id}` - 获取歌曲详情
- `POST /api/songs/{id}/play` - 增加播放次数
- `GET /api/songs/ranking` - 获取排行榜

### 歌单相关
- `GET /api/playlists` - 获取歌单列表
- `GET /api/playlists/{id}` - 获取歌单详情
- `GET /api/playlists/recommended` - 获取推荐歌单

### 收藏相关
- `POST /api/favorites` - 添加收藏
- `DELETE /api/favorites/{id}` - 取消收藏
- `GET /api/favorites` - 获取收藏列表

## 🎨 功能特色

### 🎵 音乐播放器
- **专业级播放控制**: 播放/暂停、上一首/下一首、进度控制
- **音量调节**: 实时音量控制
- **播放模式**: 支持列表循环、单曲循环
- **播放队列**: 动态播放列表管理
- **音频格式支持**: MP3、WAV等主流格式

### 🎪 用户体验
- **响应式设计**: 完美适配桌面和移动端
- **流畅动画**: CSS3动画提升用户体验
- **实时搜索**: 支持歌曲、歌手、专辑搜索
- **智能推荐**: 基于用户行为的个性化推荐
- **夜间模式**: 护眼的深色主题

### 🔐 安全特性
- **JWT认证**: 安全的用户身份验证
- **权限控制**: 基于角色的访问控制
- **数据验证**: 前后端双重数据验证
- **防XSS**: 评论内容安全过滤

## 📞 联系方式
- 微信: jing_569
- 邮箱: 3214343497@qq.com

## 🙏 致谢

感谢所有开源项目的贡献者，特别是：
- Vue.js 团队
- Element Plus 团队  
- Spring Boot 团队
- MyBatis-Plus 团队

---

⭐ 如果这个项目对你有帮助，请给它一个星标！
