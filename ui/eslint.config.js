import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import eslintPluginPrettierRecommended from 'eslint-plugin-prettier/recommended'

const isProduction = process.env.NODE_ENV === 'production'

export default [
  js.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  eslintPluginPrettierRecommended,
  {
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: 'module',
      globals: {
        document: 'readonly',
        window: 'readonly',
        console: 'readonly',
        setTimeout: 'readonly',
        clearTimeout: 'readonly',
        setInterval: 'readonly',
        clearInterval: 'readonly',
        URL: 'readonly',
        Blob: 'readonly',
        FormData: 'readonly',
        navigator: 'readonly',
        crypto: 'readonly',
        requestAnimationFrame: 'readonly',
        cancelAnimationFrame: 'readonly',
        EventSource: 'readonly',
        ResizeObserver: 'readonly',
        PerformanceObserver: 'readonly',
        btoa: 'readonly',
        atob: 'readonly',
        TextEncoder: 'readonly',
        TextDecoder: 'readonly',
        localStorage: 'readonly',
        sessionStorage: 'readonly',
        fetch: 'readonly',
        history: 'readonly',
        Image: 'readonly',
        FileReader: 'readonly',
        DOMParser: 'readonly',
        Audio: 'readonly',
        AbortController: 'readonly',
        CustomEvent: 'readonly',
        MouseEvent: 'readonly',
        HTMLElement: 'readonly',
        XMLHttpRequest: 'readonly',
        getComputedStyle: 'readonly',
        indexedDB: 'readonly',
        postMessage: 'readonly',
        addEventListener: 'readonly',
        removeEventListener: 'readonly',
        dispatchEvent: 'readonly',
        matchMedia: 'readonly',
        requestIdleCallback: 'readonly',
        cancelIdleCallback: 'readonly',
        structuredClone: 'readonly',
        Notification: 'readonly',
        SpeechRecognition: 'readonly',
        webkitSpeechRecognition: 'readonly',
        IntersectionObserver: 'readonly',
        MutationObserver: 'readonly',
        XMLSerializer: 'readonly',
        MediaRecorder: 'readonly',
        performance: 'readonly',
        WebSocket: 'readonly'
      }
    },
    rules: {
      'vue/multi-word-component-names': 'off',
      'vue/no-v-html': 'off',
      'no-unused-vars': ['error', { varsIgnorePattern: '^_', argsIgnorePattern: '^_' }],
      'vue/require-default-prop': 'off',
      'vue/require-prop-types': 'off',
      'no-console': isProduction ? 'error' : 'warn',
      'no-debugger': isProduction ? 'error' : 'warn',
      'no-empty': ['error', { allowEmptyCatch: true }],
      'no-useless-catch': 'error'
    }
  }
]
