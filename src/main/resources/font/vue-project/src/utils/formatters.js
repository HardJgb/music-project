/**
 * 将毫秒转换为分钟:秒格式
 * @param {number} ms 毫秒数
 * @returns {string} 格式化后的时间 (mm:ss)
 */
export function formatDuration(ms) {
  // console.log('utils/formatters.js formatDuration 输入:', ms, '类型:', typeof ms)
  
  if (!ms || ms <= 0) {
    console.log('无效时长，返回 00:00')
    return '00:00'
  }
  
  // 如果输入值小于10000，假设是秒而不是毫秒
  let totalSeconds;
  if (ms < 10000) {
    // console.log('检测到秒格式，保持不变:', ms)
    totalSeconds = Math.floor(ms)
  } else {
    // console.log('检测到毫秒格式，转换为秒:', ms, '->', Math.floor(ms/1000))
    totalSeconds = Math.floor(ms / 1000)
  }
  
  // 计算分钟和秒
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  
  // 格式化为两位数
  const formattedMinutes = String(minutes).padStart(2, '0')
  const formattedSeconds = String(seconds).padStart(2, '0')
  
  const result = `${formattedMinutes}:${formattedSeconds}`
  // console.log('格式化后时长:', result)
  return result
}

/**
 * 格式化日期
 * @param {string|Date} date 日期对象或ISO日期字符串
 * @returns {string} 格式化后的日期 (YYYY-MM-DD)
 */
export function formatDate(date) {
  if (!date) return ''
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  
  return `${year}-${month}-${day}`
} 