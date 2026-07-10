/**
 * 前端数据脱敏工具
 * 用于在前端展示时自动脱敏敏感字段
 */

/**
 * 手机号脱敏：138****1234
 */
export function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone
  return phone.substring(0, 3) + '****' + phone.substring(phone.length - 4)
}

/**
 * 身份证脱敏：110***********1234
 */
export function maskIdCard(idCard) {
  if (!idCard || idCard.length < 8) return idCard
  return idCard.substring(0, 3) + '***********' + idCard.substring(idCard.length - 4)
}

/**
 * 邮箱脱敏：t***@example.com
 */
export function maskEmail(email) {
  if (!email || !email.includes('@')) return email
  const atIndex = email.indexOf('@')
  if (atIndex <= 1) return email
  return email.substring(0, 1) + '***' + email.substring(atIndex)
}

/**
 * 银行卡脱敏：6222 **** **** 1234
 */
export function maskBankCard(bankCard) {
  if (!bankCard || bankCard.length < 8) return bankCard
  return bankCard.substring(0, 4) + ' **** **** ' + bankCard.substring(bankCard.length - 4)
}

/**
 * 姓名脱敏：张*三
 */
export function maskName(name) {
  if (!name || name.length < 2) return name
  if (name.length === 2) return name.charAt(0) + '*'
  return name.charAt(0) + '*' + name.substring(name.length - 1)
}

/**
 * 地址脱敏：保留前6个字符，其余用*代替
 */
export function maskAddress(address) {
  if (!address || address.length <= 6) return address
  return address.substring(0, 6) + '****'
}

/**
 * 根据类型自动脱敏
 */
export function autoMask(type, value) {
  const maskers = {
    phone: maskPhone,
    idCard: maskIdCard,
    email: maskEmail,
    bankCard: maskBankCard,
    name: maskName,
    address: maskAddress
  }
  
  const masker = maskers[type]
  return masker ? masker(value) : value
}
