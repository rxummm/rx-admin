import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import eslintPluginPrettierRecommended from 'eslint-plugin-prettier/recommended'

const isProduction = process.env.NODE_ENV === 'production'

export default [
  js.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  eslintPluginPrettierRecommended,
  {
    rules: {
      'vue/multi-word-component-names': 'off',
      'vue/no-v-html': 'off',
      'no-unused-vars': ['error', { varsIgnorePattern: '^_' }],
      'vue/require-default-prop': 'off',
      'vue/require-prop-types': 'off',
      'no-console': isProduction ? 'error' : 'warn',
      'no-debugger': isProduction ? 'error' : 'warn'
    }
  }
]
