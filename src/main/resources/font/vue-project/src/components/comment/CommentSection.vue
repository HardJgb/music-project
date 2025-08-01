<template>
  <div class="comment-section">
    <div class="comment-header">
      <h2 class="comment-title">评论 <span v-if="totalComments">({{ totalComments }})</span></h2>
      <div class="comment-filter">
        <span :class="{ 'active': sortBy === 'latest' }" @click="changeSortBy('latest')">最新</span>
        <span :class="{ 'active': sortBy === 'popular' }" @click="changeSortBy('popular')">最热</span>
      </div>
    </div>
    
    <!-- 评论输入框 -->
    <div class="comment-input-container">
      <div class="comment-avatar">
        <el-avatar :size="40" :src="userAvatar" :icon="UserFilled"></el-avatar>
      </div>
      <div class="comment-input-wrapper">
        <el-input
          type="textarea"
          v-model="commentContent"
          :rows="1"
          :placeholder="replyTo ? `回复 @${replyTo.username}：` : '分享你的感受和想法...'"
          resize="none"
          class="comment-textarea"
        ></el-input>
        <div class="comment-toolbar">
          <div class="toolbar-left">
            <div class="comment-tools">
              <el-button class="emoji-btn" @click="showEmojiPicker = !showEmojiPicker">
                <el-icon><Sunny /></el-icon>
              </el-button>
              
              <!-- 表情弹窗 -->
              <div class="emoji-popup" v-if="showEmojiPicker">
                <div class="emoji-popup-header">
                  <span>常用表情</span>
                  <el-icon class="close-icon" @click="showEmojiPicker = false"><Close /></el-icon>
                </div>
                <div class="emoji-list">
                  <span v-for="emoji in emojiList" :key="emoji" @click="addEmoji(emoji)">{{ emoji }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="comment-buttons">
            <template v-if="replyTo">
              <el-button type="text" @click="cancelReply">取消回复</el-button>
            </template>
            <el-button 
              type="primary" 
              :disabled="!commentContent.trim()" 
              @click="submitComment"
              class="publish-btn"
            >
              发布
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 评论列表 -->
    <div v-if="loading" class="comment-loading">
      <el-skeleton :rows="3" animated />
    </div>
    
    <div v-else-if="comments.length === 0" class="no-comments">
      <p>还没有评论，快来发表第一条评论吧！</p>
    </div>
    
    <div v-else class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div 
          class="comment-avatar" 
          @click="comment.userId !== userId ? openChatDialog(comment) : null" 
          :title="comment.userId !== userId ? '点击与用户聊天' : null"
          :class="{ 'clickable': comment.userId !== userId }"
        >
          <el-avatar :size="40" :src="comment.userAvatar" :icon="UserFilled"></el-avatar>
        </div>
        <div class="comment-content">
          <div class="comment-user">
            <span class="username">{{ comment.username }}</span>
            <span v-if="comment.isAuthor" class="user-badge">作者</span>
          </div>
          <div class="comment-text">
            <p>{{ comment.content }}</p>
          </div>
          <div class="comment-footer">
            <span class="comment-time">{{ formatCommentTime(comment.createTime) }}</span>
            <div class="comment-actions">
              <span class="reply-btn" @click="replyToComment(comment)">
                <el-icon><ChatDotRound /></el-icon> 回复
              </span>
              <span 
                class="like-btn" 
                :class="{ 'liked': comment.liked }"
                @click="toggleLike(comment)"
              >
                <el-icon v-if="comment.liked"><StarFilled /></el-icon>
                <el-icon v-else><Star /></el-icon>
                {{ comment.likeCount > 0 ? comment.likeCount : '点赞' }}
              </span>
              <span 
                v-if="comment.userId === userId" 
                class="delete-btn" 
                @click="deleteComment(comment)"
              >
                <el-icon><Delete /></el-icon> 删除
              </span>
            </div>
          </div>
          
          <!-- 回复列表 -->
          <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
            <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
              <div 
                class="reply-avatar" 
                @click="reply.userId !== userId ? openChatDialog(reply) : null" 
                :title="reply.userId !== userId ? '点击与用户聊天' : null"
                :class="{ 'clickable': reply.userId !== userId }"
              >
                <el-avatar :size="32" :src="reply.userAvatar" :icon="UserFilled"></el-avatar>
              </div>
              <div class="reply-content">
                <div class="reply-user">
                  <span class="username">{{ reply.username }}</span>
                  <span v-if="reply.isAuthor" class="user-badge">作者</span>
                </div>
                <div class="reply-text">
                  <template v-if="reply.replyToUsername">
                    回复 <span class="reply-to">@{{ reply.replyToUsername }}</span>：{{ reply.content }}
                  </template>
                  <template v-else>
                    {{ reply.content }}
                  </template>
                </div>
                <div class="reply-footer">
                  <span class="reply-time">{{ formatCommentTime(reply.createTime) }}</span>
                  <div class="reply-actions">
                    <span class="reply-btn" @click="replyToComment(reply, comment)">
                      <el-icon><ChatDotRound /></el-icon> 回复
                    </span>
                    <span 
                      class="like-btn" 
                      :class="{ 'liked': reply.liked }"
                      @click="toggleLike(reply)"
                    >
                      <el-icon v-if="reply.liked"><StarFilled /></el-icon>
                      <el-icon v-else><Star /></el-icon>
                      {{ reply.likeCount > 0 ? reply.likeCount : '点赞' }}
                    </span>
                    <span 
                      v-if="reply.userId === userId" 
                      class="delete-btn" 
                      @click="deleteComment(reply)"
                    >
                      <el-icon><Delete /></el-icon> 删除
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 显示更多回复按钮 -->
          <div v-if="comment.totalReplies && comment.replies && comment.totalReplies > comment.replies.length" class="show-more-replies">
            <span @click="loadMoreReplies(comment)">查看更多{{ comment.totalReplies - comment.replies.length }}条回复</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-container" v-if="totalPages > 1">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="totalComments"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </div>
    
    <!-- 聊天弹窗 -->
    <el-dialog
      v-model="chatDialogVisible"
      :title="`与 ${chatTargetUser.username} 的聊天`"
      width="400px"
      class="chat-dialog"
      :before-close="closeChatDialog"
    >
      <div class="chat-container">
        <div class="chat-messages" ref="chatMessagesRef">
          <div v-for="(msg, index) in chatMessages" :key="index" :class="['chat-message', msg.from === 'self' ? 'self' : 'other']">
            <el-avatar v-if="msg.from !== 'self'" :size="30" :src="chatTargetUser.userAvatar" :icon="UserFilled" />
            <div class="message-content">{{ msg.content }}</div>
            <el-avatar v-if="msg.from === 'self'" :size="30" :src="userAvatar" :icon="UserFilled" />
          </div>
          <div v-if="isTyping" class="typing-indicator">
            <div class="typing-avatar">
              <el-avatar :size="30" :src="chatTargetUser.userAvatar" :icon="UserFilled" />
            </div>
            <div class="typing-dots">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
        <div class="quick-replies" v-if="!isTyping">
          <el-tag 
            v-for="(reply, index) in quickReplies" 
            :key="index"
            @click="sendQuickReply(reply)"
            class="quick-reply-tag"
            type="info"
            effect="plain"
            size="small"
          >
            {{ reply }}
          </el-tag>
        </div>
        <div class="chat-input">
          <el-input 
            v-model="chatInputMessage" 
            placeholder="输入消息..." 
            @keyup.enter="sendChatMessage"
            :disabled="isTyping"
          >
            <template #append>
              <el-button type="primary" @click="sendChatMessage" :disabled="isTyping">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, ChatDotRound, Star, StarFilled, Delete, Close, Sunny } from '@element-plus/icons-vue'
import axios from 'axios'
import { API_BASE_URL } from '@/api/config'

const props = defineProps({
  songId: {
    type: Number,
    required: true
  },
  songArtist: {
    type: String,
    default: ''
  }
})

// 状态变量
const loading = ref(true)
const comments = ref([])
const totalComments = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const sortBy = ref('latest')
const commentContent = ref('')
const showEmojiPicker = ref(false)
const replyTo = ref(null)
const parentComment = ref(null)
const userAvatar = ref('')
const userId = ref(null)
const username = ref('')

// 计算属性
const totalPages = computed(() => Math.ceil(totalComments.value / pageSize.value))

// 表情列表
const emojiList = ['😊', '😂', '😍', '🤩', '🎁', '👍', '👌', 
                '🎵', '❤️', '😎', '👏', '✨', '🔥', '💯', '🎧']

// 聊天弹窗相关
const chatDialogVisible = ref(false)
const chatTargetUser = ref({})
const chatMessages = ref([])
const chatInputMessage = ref('')
const chatMessagesRef = ref(null)
const isTyping = ref(false)

// 预设的自动回复，随机选择
const autoReplies = [
  '你好！很高兴收到你的消息。',
  '我正在听这首歌，很好听！',
  '谢谢你的留言，有空我们可以一起讨论音乐。',
  '我也很喜欢这个歌手的作品！',
  '抱歉，我暂时不在，稍后回复你。',
  '这首歌的节奏很棒，不是吗？',
  '我觉得这张专辑的制作很精良。',
  '你喜欢什么类型的音乐呢？',
  '音乐是生活的调味剂，让我们的心情更加愉快！',
  '有机会可以分享一下你的歌单给我～',
  '你推荐的这首歌真的让我眼前一亮！',
  '最近有什么新发现的好歌吗？',
  '我非常喜欢流行和民谣类型的音乐。',
  '你觉得这位歌手的声音有什么特点？',
  '听音乐是我放松的最佳方式。',
  '我收藏了很多小众的独立音乐人作品。',
  '能遇到同样喜欢这种音乐的人真是太棒了！',
  '最近我在学习弹吉他，希望有一天能自弹自唱。',
  '你有去过现场音乐会吗？那种氛围真的很震撼。',
  '我们可以交换一下歌单，互相发现新歌。'
]

// 快速回复选项
const quickReplies = [
  '你好！',
  '这首歌很好听',
  '推荐更多类似的歌曲',
  '谢谢分享',
  '你的头像很酷'
]

// 初始化
onMounted(async () => {
  await getUserInfo()
  loadComments()
})

// 监听歌曲ID变化，重新加载评论
watch(() => props.songId, () => {
  currentPage.value = 1
  loadComments()
})

// 获取用户信息
const getUserInfo = async () => {
  const token = localStorage.getItem('token')
  if (!token) return
  
  try {
    const response = await axios.get(`${API_BASE_URL}/user/info`, {
      headers: { token }
    })
    
    if (response.data.code === 200 && response.data.data) {
      const user = response.data.data
      userAvatar.value = user.avatarUrl ? `${API_BASE_URL}${user.avatarUrl}` : ''
      userId.value = user.id
      username.value = user.username
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 加载评论
const loadComments = async () => {
  loading.value = true
  
  try {
    const response = await axios.get(`${API_BASE_URL}/api/comments/song/${props.songId}`, {
      params: {
        page: currentPage.value,
        pageSize: pageSize.value,
        sortBy: sortBy.value
      },
      headers: { token: localStorage.getItem('token') || '' }
    })
    
    console.log('加载评论响应:', response.data)
    
    if (response.data.code === 200) {
      comments.value = response.data.data.comments || []
      totalComments.value = response.data.data.total || 0
      
      // 确保每个评论都有replies数组
      comments.value.forEach(comment => {
        if (!comment.replies) {
          comment.replies = [];
        }
        // 输出评论信息，便于调试
        console.log(`评论ID: ${comment.id}, 用户ID: ${comment.userId}, 用户名: ${comment.username}`)
      });
    } else {
      ElMessage.error(response.data.message || '获取评论失败')
      comments.value = []
      totalComments.value = 0
    }
  } catch (error) {
    console.error('获取评论失败:', error)
    ElMessage.error('网络错误，请稍后再试')
    comments.value = []
    totalComments.value = 0
  } finally {
    loading.value = false
  }
}

// 加载更多回复
const loadMoreReplies = async (comment) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/comments/${comment.id}/replies`, {
      params: {
        page: 1,
        pageSize: 999 // 加载所有回复
      },
      headers: { token: localStorage.getItem('token') || '' }
    })
    
    if (response.data.code === 200) {
      comment.replies = response.data.data || []
    } else {
      ElMessage.error(response.data.message || '获取回复失败')
    }
  } catch (error) {
    console.error('获取回复失败:', error)
    ElMessage.error('网络错误，请稍后再试')
  }
}

// 回复评论
const replyToComment = (comment, parent = null) => {
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录后再评论')
    return
  }
  
  console.log('回复评论对象:', comment)
  console.log('父评论对象:', parent || comment)
  
  replyTo.value = comment
  parentComment.value = parent || comment
  commentContent.value = ''
  
  // 滚动到评论输入框
  setTimeout(() => {
    document.querySelector('.comment-input-container').scrollIntoView({ behavior: 'smooth' })
  }, 100)
}

// 取消回复
const cancelReply = () => {
  replyTo.value = null
  parentComment.value = null
}

// 发布评论
const submitComment = async () => {
  if (!commentContent.value.trim()) return
  
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录后再评论')
    return
  }
  
  try {
    let url = `${API_BASE_URL}/api/comments/add`
    let data = {
      songId: props.songId,
      content: commentContent.value
    }
    
    // 如果是回复评论
    if (replyTo.value) {
      url = `${API_BASE_URL}/api/comments/reply`
      data = {
        commentId: parentComment.value.id,
        replyToId: replyTo.value.userId, // 使用userId而不是id作为回复对象的ID
        content: commentContent.value
      }
      
      console.log('回复评论数据:', {
        commentId: parentComment.value.id,
        replyToId: replyTo.value.userId,
        replyToUsername: replyTo.value.username,
        content: commentContent.value
      })
    }
    
    const response = await axios.post(url, data, {
      headers: { token: localStorage.getItem('token') }
    })
    
    console.log('评论响应:', response.data)
    
    if (response.data.code === 200) {
      ElMessage.success(replyTo.value ? '回复发表成功' : '评论发表成功')
      commentContent.value = ''
      cancelReply()
      loadComments() // 重新加载评论
    } else {
      ElMessage.error(response.data.message || '发表失败')
    }
  } catch (error) {
    console.error('发表评论失败:', error)
    if (error.response) {
      console.error('错误响应:', error.response.data)
    }
    ElMessage.error('网络错误，请稍后再试')
  }
}

// 点赞/取消点赞
const toggleLike = async (comment) => {
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  
  try {
    const url = comment.liked 
      ? `${API_BASE_URL}/api/comments/unlike/${comment.id}`
      : `${API_BASE_URL}/api/comments/like/${comment.id}`
    
    const response = await axios.post(url, null, {
      headers: { token: localStorage.getItem('token') }
    })
    
    if (response.data.code === 200) {
      // 更新点赞状态和数量
      comment.liked = !comment.liked
      comment.likeCount = comment.liked 
        ? (comment.likeCount || 0) + 1 
        : Math.max(0, (comment.likeCount || 0) - 1)
      
      ElMessage.success(comment.liked ? '点赞成功' : '已取消点赞')
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('网络错误，请稍后再试')
  }
}

// 更改排序方式
const changeSortBy = (type) => {
  if (sortBy.value === type) return
  sortBy.value = type
  currentPage.value = 1
  loadComments()
}

// 处理分页
const handlePageChange = (page) => {
  currentPage.value = page
  loadComments()
  
  // 滚动到评论区顶部
  setTimeout(() => {
    document.querySelector('.comment-section').scrollIntoView({ behavior: 'smooth' })
  }, 100)
}

// 添加表情
const addEmoji = (emoji) => {
  commentContent.value += emoji
  showEmojiPicker.value = false
}

// 格式化评论时间
const formatCommentTime = (timestamp) => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  // 小于1分钟
  if (diff < 60 * 1000) {
    return '刚刚'
  }
  
  // 小于1小时
  if (diff < 60 * 60 * 1000) {
    return Math.floor(diff / (60 * 1000)) + '分钟前'
  }
  
  // 小于24小时
  if (diff < 24 * 60 * 60 * 1000) {
    return Math.floor(diff / (60 * 60 * 1000)) + '小时前'
  }
  
  // 小于30天
  if (diff < 30 * 24 * 60 * 60 * 1000) {
    return Math.floor(diff / (24 * 60 * 60 * 1000)) + '天前'
  }
  
  // 大于30天，显示具体日期
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 检查是否已登录
const isLoggedIn = () => {
  return !!localStorage.getItem('token')
}

// 删除评论
const deleteComment = async (comment) => {
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录后再操作')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？此操作不可恢复', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const url = `${API_BASE_URL}/api/comments/${comment.id}`
    
    const response = await axios.delete(url, {
      headers: { token: localStorage.getItem('token') }
    })
    
    if (response.data.code === 200) {
      ElMessage.success('评论删除成功')
      
      // 如果是回复，从父评论的回复列表中移除
      if (comment.parentId) {
        const parentComment = comments.value.find(c => c.id === comment.parentId)
        if (parentComment && parentComment.replies) {
          parentComment.replies = parentComment.replies.filter(r => r.id !== comment.id)
          if (parentComment.totalReplies && parentComment.totalReplies > 0) {
            parentComment.totalReplies--
          }
        }
      } else {
        // 如果是主评论，从评论列表中移除
        comments.value = comments.value.filter(c => c.id !== comment.id)
        if (totalComments.value > 0) {
          totalComments.value--
        }
      }
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    
    console.error('删除评论失败:', error)
    if (error.response) {
      console.error('错误响应:', error.response.data)
    }
    ElMessage.error('网络错误，请稍后再试')
  }
}

// 发送快速回复
const sendQuickReply = (reply) => {
  chatInputMessage.value = reply
  sendChatMessage()
}

// 打开聊天弹窗
const openChatDialog = (user) => {
  // 检查是否已登录
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录后再使用聊天功能')
    return
  }
  
  // 检查是否是用户自己 - 更严格的检查
  console.log('当前用户ID:', userId.value, typeof userId.value)
  console.log('目标用户ID:', user.userId, typeof user.userId)
  
  // 尝试多种比较方式
  const currentUserId = String(userId.value).trim()
  const targetUserId = String(user.userId).trim()
  
  console.log('转换后 - 当前用户ID:', currentUserId)
  console.log('转换后 - 目标用户ID:', targetUserId)
  
  if (currentUserId === targetUserId || parseInt(currentUserId) === parseInt(targetUserId)) {
    console.log('用户尝试和自己聊天，已阻止')
    ElMessage.info('不能和自己聊天')
    return
  }
  
  chatTargetUser.value = {
    id: user.userId,
    username: user.username,
    userAvatar: user.userAvatar
  }
  
  // 初始化对话消息
  chatMessages.value = [
    {
      from: 'other',
      content: `你好，我是${user.username}，很高兴认识你！`,
      time: new Date()
    }
  ]
  
  chatDialogVisible.value = true
  chatInputMessage.value = ''
  
  // 自动滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
}

// 关闭聊天弹窗
const closeChatDialog = () => {
  chatDialogVisible.value = false
  isTyping.value = false
}

// 发送聊天消息
const sendChatMessage = () => {
  if (!chatInputMessage.value.trim()) return
  
  // 添加自己的消息
  chatMessages.value.push({
    from: 'self',
    content: chatInputMessage.value,
    time: new Date()
  })
  
  chatInputMessage.value = ''
  
  // 自动滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
  
  // 显示对方正在输入状态
  isTyping.value = true
  
  // 模拟对方回复
  const replyDelay = 1000 + Math.random() * 2000 // 1-3秒随机延迟
  setTimeout(() => {
    const randomReply = autoReplies[Math.floor(Math.random() * autoReplies.length)]
    
    // 隐藏正在输入状态
    isTyping.value = false
    
    // 添加对方回复
    chatMessages.value.push({
      from: 'other',
      content: randomReply,
      time: new Date()
    })
    
    // 自动滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
  }, replyDelay)
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}
</script>

<style scoped>
.comment-section {
  width: 100%;
  padding: 20px 0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.comment-title {
  font-size: 20px;
  font-weight: bold;
  margin: 0;
}

.comment-filter {
  display: flex;
  gap: 16px;
}

.comment-filter span {
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 15px;
  transition: all 0.2s;
  color: #666;
}

.comment-filter span.active {
  color: #ec4141;
  background-color: rgba(236, 65, 65, 0.1);
  font-weight: 500;
}

.comment-filter span:hover {
  color: #ec4141;
}

.comment-input-container {
  display: flex;
  margin-bottom: 30px;
  background-color: #f9f9f9;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.comment-avatar {
  margin-right: 15px;
  flex-shrink: 0;
}

.comment-input-wrapper {
  flex: 1;
  position: relative;
}

.comment-textarea :deep(.el-textarea__inner) {
  border: none;
  background-color: transparent;
  box-shadow: none;
  padding: 8px 0;
  min-height: 24px !important;
  font-size: 15px;
}

.comment-toolbar {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  border-top: 1px solid #eee;
  padding-top: 10px;
}

.toolbar-left {
  position: relative;
  display: flex;
  align-items: center;
}

.comment-tools {
  position: relative;
}

.emoji-btn {
  border: none;
  background: transparent;
  color: #606266;
  padding: 6px;
  font-size: 16px;
}

.emoji-btn:hover {
  color: #ec4141;
}

.emoji-panel-wrapper {
  display: none;
}

.emoji-panel {
  display: none;
}

.emoji-popup {
  position: absolute;
  top: 40px;
  left: 0;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  width: 340px;
  z-index: 2000;
}

.emoji-popup::before {
  content: '';
  position: absolute;
  top: -8px;
  left: 20px;
  border-width: 0 8px 8px 8px;
  border-style: solid;
  border-color: transparent transparent white transparent;
}

.emoji-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid #eee;
}

.emoji-popup-header span {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.emoji-list {
  display: flex;
  flex-wrap: wrap;
  padding: 12px;
  max-height: 240px;
}

.emoji-list span {
  font-size: 24px;
  cursor: pointer;
  text-align: center;
  padding: 8px;
  margin: 3px;
  transition: background-color 0.2s;
}

.emoji-list span:hover {
  background-color: #f0f0f0;
}

.comment-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
}

.publish-btn {
  padding: 6px 16px;
  border-radius: 18px;
  font-size: 14px;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  display: flex;
  margin-bottom: 25px;
  position: relative;
}

.comment-item::after {
  content: '';
  position: absolute;
  bottom: -12px;
  left: 55px;
  right: 0;
  height: 1px;
  background-color: #f0f0f0;
}

.comment-item:last-child::after {
  display: none;
}

.comment-content {
  flex: 1;
}

.comment-user {
  margin-bottom: 5px;
}

.username {
  font-weight: bold;
  margin-right: 5px;
  color: #333;
}

.user-badge {
  font-size: 12px;
  background-color: #ec4141;
  color: white;
  padding: 1px 6px;
  border-radius: 3px;
  margin-left: 5px;
}

.comment-text {
  margin-bottom: 8px;
  line-height: 1.5;
}

.comment-text p {
  margin: 0;
  color: #333;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color: #999;
}

.comment-time {
  font-size: 12px;
}

.comment-actions {
  display: flex;
  gap: 15px;
}

.reply-btn, .like-btn, .delete-btn {
  cursor: pointer;
  margin-left: 16px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
}

.reply-btn:hover, .like-btn:hover {
  color: #ec4141;
}

.delete-btn:hover {
  color: #f56c6c;
}

.like-btn.liked {
  color: #ec4141;
}

.reply-list {
  background-color: #f8f8f8;
  border-radius: 8px;
  padding: 10px 15px;
  margin-top: 10px;
}

.reply-item {
  display: flex;
  margin-bottom: 15px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-avatar {
  margin-right: 10px;
  flex-shrink: 0;
}

.reply-content {
  flex: 1;
}

.reply-user {
  margin-bottom: 3px;
}

.reply-text {
  margin-bottom: 5px;
  font-size: 14px;
  line-height: 1.5;
  color: #333;
}

.reply-to {
  color: #ec4141;
  font-weight: 500;
}

.reply-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #999;
}

.reply-time {
  font-size: 12px;
}

.reply-actions {
  display: flex;
  gap: 15px;
}

.show-more-replies {
  margin-top: 10px;
  text-align: center;
}

.show-more-replies span {
  color: #ec4141;
  cursor: pointer;
  font-size: 14px;
}

.show-more-replies span:hover {
  text-decoration: underline;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.comment-loading, .no-comments {
  margin: 30px 0;
  text-align: center;
  color: #999;
}

/* 聊天弹窗样式 */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 400px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  background-color: #f7f7f7;
  border-radius: 8px;
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-message {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 80%;
}

.chat-message.self {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.chat-message.other {
  align-self: flex-start;
}

.message-content {
  background-color: #fff;
  padding: 10px 12px;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  word-break: break-all;
}

.chat-message.self .message-content {
  background-color: #ec4141;
  color: white;
}

.chat-input {
  margin-top: auto;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  align-self: flex-start;
  margin-top: 10px;
}

.typing-dots {
  background-color: #e0e0e0;
  padding: 8px 12px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.typing-dots span {
  display: inline-block;
  width: 8px;
  height: 8px;
  background-color: #999;
  border-radius: 50%;
  animation: typingDot 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(1) {
  animation-delay: 0s;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typingDot {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-5px);
  }
}

.quick-replies {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.quick-reply-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.quick-reply-tag:hover {
  background-color: #f0f0f0;
  color: #ec4141;
}

.clickable {
  cursor: pointer;
  transition: transform 0.2s;
}

.clickable:hover {
  transform: scale(1.05);
}

.comment-avatar, .reply-avatar {
  cursor: default;
}

.close-icon {
  cursor: pointer;
  color: #999;
  font-size: 16px;
}

.close-icon:hover {
  color: #ec4141;
}
</style> 