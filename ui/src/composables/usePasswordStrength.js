/**
 * 密码强度检测工具
 * @param {string} password - 密码字符串
 * @returns {{ level: number, label: string, color: string, percent: number }}
 */
export function checkPasswordStrength(password) {
  if (!password) return { level: 0, label: '', color: '', percent: 0 }

  let score = 0

  // 长度加分
  if (password.length >= 6) score += 10
  if (password.length >= 8) score += 10
  if (password.length >= 12) score += 10

  // 包含小写字母
  if (/[a-z]/.test(password)) score += 15
  // 包含大写字母
  if (/[A-Z]/.test(password)) score += 15
  // 包含数字
  if (/\d/.test(password)) score += 15
  // 包含特殊字符
  if (/[!@#$%^&*()_+\-={}:;'",.<>/?]/.test(password)) score += 15
  // 长度超过8且包含3种以上字符
  if (
    password.length >= 8 &&
    [
      /[a-z]/.test(password),
      /[A-Z]/.test(password),
      /\d/.test(password),
      /[!@#$%^&*()_+\-={}:;'",.<>/?]/.test(password)
    ].filter(Boolean).length >= 3
  )
    score += 10

  if (score < 30) return { level: 1, label: 'weak', color: 'var(--color-danger)', percent: 25 }
  if (score < 50) return { level: 2, label: 'medium', color: 'var(--color-warning)', percent: 50 }
  if (score < 70) return { level: 3, label: 'strong', color: 'var(--color-success)', percent: 75 }
  return { level: 4, label: 'veryStrong', color: 'var(--color-success)', percent: 100 }
}
