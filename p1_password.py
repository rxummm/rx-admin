import sys
sys.stdout.reconfigure(encoding="utf-8")

# ============================================================
# 1. Backend: Add @Pattern to RegisterRequest
# ============================================================
path = "D:/vueprojects/RX/src/main/java/com/rx/admin/entity/RegisterRequest.java"
c = open(path, "r", encoding="utf-8").read()
c = c.replace(
    "import jakarta.validation.constraints.NotBlank;",
    "import jakarta.validation.constraints.NotBlank;\nimport jakarta.validation.constraints.Pattern;"
)
c = c.replace(
    "@NotBlank(message = \"密码不能为空\")\n    private String password;",
    "@NotBlank(message = \"密码不能为空\")\n    @Pattern(regexp = \"^(?=.*[A-Za-z])(?=.*\\\\d).{6,}$\", message = \"密码需包含字母和数字，至少6位\")\n    private String password;"
)
open(path, "w", encoding="utf-8").write(c)
print("RegisterRequest.java updated")

# ============================================================
# 2. Backend: Add password validation in SysUserService
# ============================================================
path2 = "D:/vueprojects/RX/src/main/java/com/rx/admin/service/SysUserService.java"
c2 = open(path2, "r", encoding="utf-8").read()

# Add import
c2 = c2.replace(
    "import org.springframework.util.StringUtils;",
    "import org.springframework.util.StringUtils;\nimport java.util.regex.Pattern;"
)

# Add password validation in addUser (after checking username exists)
old_add = "        // 加密密码\n        user.setPassword(passwordEncoder.encode(user.getPassword()));"
new_add = "        // 密码策略校验\n        validatePassword(user.getPassword());\n        // 加密密码\n        user.setPassword(passwordEncoder.encode(user.getPassword()));"
c2 = c2.replace(old_add, new_add)

# Add password validation in updateUser (inside if has password)
old_update = "        if (StringUtils.hasText(user.getPassword())) {\n            update.setPassword(passwordEncoder.encode(user.getPassword()));\n        }"
new_update = "        if (StringUtils.hasText(user.getPassword())) {\n            validatePassword(user.getPassword());\n            update.setPassword(passwordEncoder.encode(user.getPassword()));\n        }"
c2 = c2.replace(old_update, new_update)

# Add validatePassword method before the closing brace of the class
c2 = c2.replace(
    "    public void assignRole(Long userId, Long roleId) {\n        userRoleMapper.insert(userId, roleId);\n    }\n}",
    """    public void assignRole(Long userId, Long roleId) {
        userRoleMapper.insert(userId, roleId);
    }

    /** 密码策略：至少6位，包含字母和数字 */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\\\d).{6,}$");

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("密码需包含字母和数字，至少6位");
        }
    }
}"""
)

open(path2, "w", encoding="utf-8").write(c2)
print("SysUserService.java updated")

# ============================================================
# 3. Frontend: Password strength composable
# ============================================================
pw_composable = """/**
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
  if (/\\d/.test(password)) score += 15
  // 包含特殊字符
  if (/[!@#$%^&*()_+\\-=\\[\\]{};':"\\\\|,.<>\\/?]/.test(password)) score += 15
  // 长度超过8且包含3种以上字符
  if (password.length >= 8 && [
    /[a-z]/.test(password),
    /[A-Z]/.test(password),
    /\\d/.test(password),
    /[!@#$%^&*()_+\\-=\\[\\]{};':"\\\\|,.<>\\/?]/.test(password)
  ].filter(Boolean).length >= 3) score += 10

  if (score < 30) return { level: 1, label: 'weak', color: '#f56c6c', percent: 25 }
  if (score < 50) return { level: 2, label: 'medium', color: '#e6a23c', percent: 50 }
  if (score < 70) return { level: 3, label: 'strong', color: '#67c23a', percent: 75 }
  return { level: 4, label: 'veryStrong', color: '#67c23a', percent: 100 }
}
"""
open("D:/vueprojects/RX/ui/src/composables/usePasswordStrength.js", "w", encoding="utf-8").write(pw_composable)
print("usePasswordStrength.js created")

# ============================================================
# 4. Frontend: PasswordStrength Vue component
# ============================================================
pw_component = """<template>
  <div class="pw-strength" v-if="password">
    <div class="pw-bar">
      <div class="pw-bar-fill" :style="{ width: strength.percent + '%', background: strength.color }"></div>
    </div>
    <div class="pw-label" :style="{ color: strength.color }">
      <template v-if="strength.level === 1">{{ $t('login.passwordWeak') }}</template>
      <template v-else-if="strength.level === 2">{{ $t('login.passwordMedium') }}</template>
      <template v-else-if="strength.level === 3">{{ $t('login.passwordStrong') }}</template>
      <template v-else-if="strength.level === 4">{{ $t('login.passwordVeryStrong') }}</template>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { checkPasswordStrength } from '@/composables/usePasswordStrength'

const props = defineProps({
  password: { type: String, default: '' }
})

const strength = computed(() => checkPasswordStrength(props.password))
</script>

<style scoped>
.pw-strength {
  display: flex; align-items: center; gap: 8px; margin-top: 4px;
}
.pw-bar {
  flex: 1; height: 4px; background: var(--border-lighter); border-radius: 2px; overflow: hidden;
}
.pw-bar-fill {
  height: 100%; border-radius: 2px; transition: all 0.3s ease;
}
.pw-label {
  font-size: 11px; white-space: nowrap; flex-shrink: 0; min-width: 50px; text-align: right;
}
</style>
"""
open("D:/vueprojects/RX/ui/src/components/PasswordStrength.vue", "w", encoding="utf-8").write(pw_component)
print("PasswordStrength.vue created")

# ============================================================
# 5. Add i18n keys for password strength
# ============================================================
# en-US
en = open("D:/vueprojects/RX/ui/src/i18n/lang/en-US.js", "r", encoding="utf-8").read()
en = en.replace(
    "passwordMismatch: 'Passwords do not match',",
    "passwordMismatch: 'Passwords do not match',\n    passwordWeak: 'Weak',\n    passwordMedium: 'Medium',\n    passwordStrong: 'Strong',\n    passwordVeryStrong: 'Very Strong',"
)
open("D:/vueprojects/RX/ui/src/i18n/lang/en-US.js", "w", encoding="utf-8").write(en)
print("en-US.js updated")

# zh-CN
zh = open("D:/vueprojects/RX/ui/src/i18n/lang/zh-CN.js", "r", encoding="utf-8").read()
zh = zh.replace(
    "passwordMismatch: '密码输入不一致',",
    "passwordMismatch: '密码输入不一致',\n    passwordWeak: '弱',\n    passwordMedium: '中',\n    passwordStrong: '强',\n    passwordVeryStrong: '非常强',"
)
open("D:/vueprojects/RX/ui/src/i18n/lang/zh-CN.js", "w", encoding="utf-8").write(zh)
print("zh-CN.js updated")

print("\\nAll password policy files created!")
