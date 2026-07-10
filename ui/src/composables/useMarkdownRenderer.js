import { ref, nextTick } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import 'github-markdown-css/github-markdown.css'
import { sanitizeHtml } from '@/utils/sanitize'

export function useMarkdownRenderer() {
  const contentRef = ref(null)
  const tocItems = ref([])
  const activeTocId = ref('')
  const renderedHtml = ref('')

  marked.setOptions({ breaks: true, gfm: true })

  const renderer = new marked.Renderer()

  renderer.code = function ({ text, lang }) {
    const validLang = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
    const highlighted = hljs.highlight(text, { language: validLang }).value
    return `<pre><code class="hljs language-${validLang}">${highlighted}</code></pre>`
  }

  let headingIndex = 0
  renderer.heading = function ({ text, depth }) {
    headingIndex++
    const id = `heading-${headingIndex}`
    return `<h${depth} id="${id}">${text}</h${depth}>`
  }

  marked.use({ renderer })

  async function loadDocument(url) {
    try {
      const response = await fetch(url)
      const text = await response.text()
      renderedHtml.value = marked.parse(text)
      await nextTick()
      extractToc()
      setupHeadingLinks()
    } catch (err) {
      console.error('加载文档失败:', err)
      renderedHtml.value = '<p style="color:red;text-align:center;padding:40px;">文档加载失败，请检查文件是否存在</p>'
    }
  }

  function extractToc() {
    if (!contentRef.value) return
    const headings = contentRef.value.querySelectorAll('h1, h2, h3')
    tocItems.value = Array.from(headings).map((h) => ({
      id: h.id,
      text: h.textContent,
      level: parseInt(h.tagName[1])
    }))
  }

  function setupHeadingLinks() {
    if (!contentRef.value) return
    const headings = contentRef.value.querySelectorAll('h1, h2, h3, h4, h5, h6')
    headings.forEach((h) => {
      h.addEventListener('click', () => {
        history.pushState(null, '', `#${h.id}`)
      })
    })
  }

  function scrollToHeading(id) {
    const el = document.getElementById(id)
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' })
      activeTocId.value = id
    }
  }

  return {
    contentRef,
    tocItems,
    activeTocId,
    renderedHtml,
    loadDocument,
    extractToc,
    scrollToHeading,
    sanitizeHtml
  }
}
