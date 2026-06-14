<template>
  <div class="devtools-container">
    <!-- 左侧分类导航 -->
    <div class="devtools-sidebar">
      <div class="sidebar-title">{{ $t('tool.devTools.sidebarTitle') }}</div>
      <el-menu :default-active="activeTool" @select="handleSelect" class="tool-menu">
        <el-sub-menu v-for="cat in categories" :key="cat.key" :index="cat.key">
          <template #title>
            <el-icon><component :is="cat.icon" /></el-icon>
            <span>{{ cat.name }}</span>
          </template>
          <el-menu-item v-for="tool in cat.tools" :key="tool.key" :index="tool.key">
            {{ tool.name }}
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </div>

    <!-- 右侧工具内容 -->
    <div class="devtools-content">

      <!-- ========== 编码转换 ========== -->

      <!-- JSON格式化 -->
      <div v-if="activeTool === 'json'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.json.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.json.desc') }}</span></h3>
        <el-input v-model="jsonInput" type="textarea" :rows="8" placeholder='{"key":"value"}' style="font-family:monospace" />
        <div class="tool-actions">
          <el-button type="primary" @click="formatJson">格式化</el-button>
          <el-button @click="compressJson">压缩</el-button>
        </div>
        <el-input v-model="jsonOutput" type="textarea" :rows="8" readonly style="font-family:monospace;margin-top:8px" />
      </div>

      <!-- JSON编辑器 -->
      <div v-if="activeTool === 'jsoneditor'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.jsoneditor.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.jsoneditor.desc') }}</span></h3>
        <div class="tool-actions">
          <el-button type="primary" size="small" @click="jsonEditFormat">格式化</el-button>
          <el-button size="small" @click="jsonEditCompress">压缩</el-button>
          <el-button size="small" @click="jsonEditValidate">验证</el-button>
          <el-tag v-if="jsonEditValid !== null" :type="jsonEditValid ? 'success' : 'danger'" size="small">{{ jsonEditValid ? '✓ 有效JSON' : '✗ 格式错误' }}</el-tag>
        </div>
        <div style="display:flex;gap:12px;margin-top:12px;min-height:300px">
          <el-input v-model="jsonEditInput" type="textarea" :rows="14" placeholder='{"name":"value"}' style="flex:1;font-family:monospace;font-size:13px" @input="jsonEditOnInput" />
          <div class="json-tree-panel">
            <div v-if="jsonEditTree" class="json-tree-title">结构预览</div>
            <div v-if="jsonEditTree" v-html="jsonEditTree" style="overflow:auto;font-family:monospace;font-size:12px;line-height:1.6"></div>
            <div v-if="!jsonEditTree && jsonEditInput" style="color:#c0c4cc;text-align:center;padding-top:40px">输入有效JSON后显示结构</div>
          </div>
        </div>
      </div>

      <!-- JSON转换 -->
      <div v-if="activeTool === 'jsonconvert'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.jsonconvert.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.jsonconvert.desc') }}</span></h3>
        <el-input v-model="jsonConvertInput" type="textarea" :rows="8" placeholder='输入JSON字符串' style="font-family:monospace" />
        <div class="tool-actions">
          <el-select v-model="jsonConvertTo" style="width:175px">
            <el-option label="JSON → XML" value="xml" />
            <el-option label="JSON → CSV" value="csv" />
            <el-option label="XML → JSON" value="xml2json" />
            <el-option label="CSV → JSON" value="csv2json" />
          </el-select>
          <el-button type="primary" @click="doJsonConvert">转换</el-button>
          <el-button @click="copyOne(jsonConvertOutput)" :disabled="!jsonConvertOutput">复制结果</el-button>
        </div>
        <el-input v-model="jsonConvertOutput" type="textarea" :rows="10" readonly style="font-family:monospace;margin-top:8px" />
      </div>

      <!-- Base64编解码 -->
      <div v-if="activeTool === 'base64'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.base64.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.base64.desc') }}</span></h3>
        <el-input v-model="base64Input" type="textarea" :rows="4" placeholder="输入文本" style="font-family:monospace" />
        <div class="tool-actions">
          <el-button type="primary" @click="base64Encode">编码</el-button>
          <el-button @click="base64Decode">解码</el-button>
        </div>
        <el-input v-model="base64Output" type="textarea" :rows="4" readonly style="font-family:monospace;margin-top:8px" />
      </div>

      <!-- URL编解码 -->
      <div v-if="activeTool === 'urlcodec'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.urlcodec.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.urlcodec.desc') }}</span></h3>
        <el-input v-model="urlInput" type="textarea" :rows="4" placeholder="输入URL或文本" style="font-family:monospace" />
        <div class="tool-actions">
          <el-button type="primary" @click="urlEncode">编码</el-button>
          <el-button @click="urlDecode">解码</el-button>
        </div>
        <el-input v-model="urlOutput" type="textarea" :rows="4" readonly style="font-family:monospace;margin-top:8px" />
      </div>

      <!-- Unicode中文互转 -->
      <div v-if="activeTool === 'unicode'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.unicode.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.unicode.desc') }}</span></h3>
        <el-input v-model="unicodeText" type="textarea" :rows="4" placeholder="输入中文或Unicode转义字符串（如 \u4f60\u597d）" style="font-family:monospace" />
        <div class="tool-actions">
          <el-button type="primary" @click="toUnicode">中文 → Unicode</el-button>
          <el-button @click="fromUnicode">Unicode → 中文</el-button>
        </div>
        <el-input v-model="unicodeResult" type="textarea" :rows="4" readonly style="font-family:monospace;margin-top:8px" />
      </div>

      <!-- HTML实体编解码 -->
      <div v-if="activeTool === 'htmlentity'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.htmlentity.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.htmlentity.desc') }}</span></h3>
        <el-input v-model="htmlInput" type="textarea" :rows="4" placeholder="输入HTML文本或实体字符串" style="font-family:monospace" />
        <div class="tool-actions">
          <el-button type="primary" @click="htmlEncode">转义(编码)</el-button>
          <el-button @click="htmlDecode">反转义(解码)</el-button>
        </div>
        <el-input v-model="htmlOutput" type="textarea" :rows="4" readonly style="font-family:monospace;margin-top:8px" />
      </div>

      <!-- 进制转换 -->
      <div v-if="activeTool === 'radix'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.radix.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.radix.desc') }}</span></h3>
        <div class="radix-row">
          <span class="radix-label">输入值：</span>
          <el-input v-model="radixInput" placeholder="输入数值" style="width:260px;font-family:monospace" @input="convertRadix" />
          <el-select v-model="radixFrom" style="width:100px;margin-left:8px" @change="convertRadix">
            <el-option label="十进制" :value="10" />
            <el-option label="十六进制" :value="16" />
            <el-option label="八进制" :value="8" />
            <el-option label="二进制" :value="2" />
          </el-select>
        </div>
        <div class="radix-results" v-if="radixResults">
          <div class="radix-item"><span>> 二进制：</span><code>{{ radixResults.bin }}</code></div>
          <div class="radix-item"><span>> 八进制：</span><code>{{ radixResults.oct }}</code></div>
          <div class="radix-item"><span>> 十进制：</span><code>{{ radixResults.dec }}</code></div>
          <div class="radix-item"><span>> 十六进制：</span><code>{{ radixResults.hex }}</code></div>
        </div>
      </div>

      <!-- 加密解密 -->
      <div v-if="activeTool === 'crypto'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.crypto.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.crypto.desc') }}</span></h3>
        <el-input v-model="cryptoInput" type="textarea" :rows="4" placeholder="输入要计算哈希的文本" />
        <div class="tool-actions">
          <el-select v-model="cryptoAlgo" style="width:140px">
            <el-option label="MD5" value="MD5" />
            <el-option label="SHA-1" value="SHA-1" />
            <el-option label="SHA-256" value="SHA-256" />
            <el-option label="SHA-512" value="SHA-512" />
          </el-select>
          <el-button type="primary" @click="doCrypto">计算</el-button>
          <el-button @click="copyOne(cryptoResult)" :disabled="!cryptoResult">复制</el-button>
        </div>
        <div v-if="cryptoResult" class="crypto-result-box">
          <div class="crypto-label">{{ cryptoAlgo }}：</div>
          <code class="crypto-hash">{{ cryptoResult }}</code>
        </div>
      </div>

      <!-- JWT解析 -->
      <div v-if="activeTool === 'jwt'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.jwt.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.jwt.desc') }}</span></h3>
        <el-input v-model="jwtToken" type="textarea" :rows="2" placeholder="粘贴JWT Token，如 eyJhbGciOiJIUzI1NiJ9..." style="font-family:monospace" @input="parseJwt" />
        <div v-if="jwtResult" class="jwt-result">
          <div v-if="jwtResult.error" class="jwt-error">{{ jwtResult.error }}</div>
          <template v-else>
            <div class="jwt-section">
              <div class="jwt-section-title">Header</div>
              <pre class="jwt-json">{{ jwtResult.header }}</pre>
            </div>
            <div class="jwt-section">
              <div class="jwt-section-title">Payload</div>
              <pre class="jwt-json">{{ jwtResult.payload }}</pre>
              <div class="jwt-claims">
                <span v-if="jwtResult.iat">签发时间: {{ jwtResult.iat }}</span>
                <span v-if="jwtResult.exp" :class="jwtResult.expired ? 'jwt-expired' : 'jwt-valid'">
                  过期时间: {{ jwtResult.exp }} {{ jwtResult.expired ? '(已过期)' : '(有效)' }}
                </span>
              </div>
            </div>
            <div class="jwt-section">
              <div class="jwt-section-title">Signature</div>
              <code class="jwt-sig">{{ jwtResult.signature }}</code>
            </div>
          </template>
        </div>
      </div>

      <!-- ========== 网络工具 ========== -->

      <!-- HTTP请求测试 -->
      <div v-if="activeTool === 'http'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.http.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.http.desc') }}</span></h3>
        <div class="http-row">
          <el-select v-model="httpMethod" style="width:110px">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
          <el-input v-model="httpUrl" placeholder="输入请求URL，如 https://api.example.com/data" style="flex:1;font-family:monospace" />
          <el-button type="primary" @click="sendHttp" :loading="httpLoading">发送</el-button>
        </div>
        <div style="margin-top:10px">
          <div class="http-label">Headers (JSON格式，可选)：</div>
          <el-input v-model="httpHeaders" type="textarea" :rows="2" placeholder='{"Content-Type":"application/json"}' style="font-family:monospace;font-size:13px" />
        </div>
        <div style="margin-top:10px" v-if="httpMethod !== 'GET'">
          <div class="http-label">Body：</div>
          <el-input v-model="httpBody" type="textarea" :rows="4" placeholder='{"key":"value"}' style="font-family:monospace;font-size:13px" />
        </div>
        <div v-if="httpResponse !== null" class="http-response-box">
          <div class="http-response-header">
            <span>状态: <el-tag :type="httpStatus >= 200 && httpStatus < 300 ? 'success' : 'danger'" size="small">{{ httpStatus }}</el-tag></span>
            <span style="margin-left:12px">耗时: {{ httpDuration }}ms</span>
            <el-button link type="primary" size="small" style="margin-left:auto" @click="copyOne(httpResponse)">复制</el-button>
          </div>
          <pre class="http-response-body">{{ httpResponse }}</pre>
        </div>
      </div>

      <!-- IP地址查询 -->
      <div v-if="activeTool === 'ipquery'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.ipquery.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.ipquery.desc') }}</span></h3>
        <div class="tool-actions">
          <el-input v-model="ipQueryAddr" placeholder="输入IP地址，留空查当前IP" style="width:280px;font-family:monospace" />
          <el-button type="primary" @click="queryIp" :loading="ipLoading">查询</el-button>
        </div>
        <div v-if="ipResult" class="ip-result-box">
          <div class="ip-result-item" v-for="(v,k) in ipResult" :key="k">
            <span class="ip-result-key">{{ k }}</span>
            <span class="ip-result-val">{{ v }}</span>
          </div>
        </div>
      </div>

      <!-- ========== 生成器 ========== -->

      <!-- UUID生成 -->
      <div v-if="activeTool === 'uuid'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.uuid.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.uuid.desc') }}</span></h3>
        <div class="tool-actions">
          <el-input-number v-model="uuidCount" :min="1" :max="100" style="width:160px" />
          <el-button type="primary" @click="generateUuid">生成</el-button>
          <el-button @click="copyUuids" :disabled="!uuids.length">复制全部</el-button>
        </div>
        <div class="uuid-list" v-if="uuids.length">
          <div v-for="(u,i) in uuids" :key="i" class="uuid-item">{{ u }}</div>
        </div>
      </div>

      <!-- 随机密码生成器 -->
      <div v-if="activeTool === 'passgen'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.passgen.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.passgen.desc') }}</span></h3>
        <div class="passgen-settings">
          <div class="passgen-row">
            <span>密码长度：</span>
            <el-input-number v-model="passLength" :min="4" :max="64" style="width:140px" />
            <span style="margin-left:20px">生成数量：</span>
            <el-input-number v-model="passCount" :min="1" :max="50" style="width:140px" />
          </div>
          <div class="passgen-row" style="margin-top:12px">
            <el-checkbox v-model="passUpper">大写字母 A-Z</el-checkbox>
            <el-checkbox v-model="passLower">小写字母 a-z</el-checkbox>
            <el-checkbox v-model="passDigit">数字 0-9</el-checkbox>
            <el-checkbox v-model="passSymbol">特殊符号 !@#$%</el-checkbox>
          </div>
          <div class="tool-actions">
            <el-button type="primary" @click="generatePassword">生成</el-button>
            <el-button @click="copyPasswords" :disabled="!passwords.length">复制全部</el-button>
          </div>
        </div>
        <div class="uuid-list" v-if="passwords.length">
          <div v-for="(p,i) in passwords" :key="i" class="uuid-item">
            <span>{{ p }}</span>
            <el-button link type="primary" size="small" @click="copyOne(p)">复制</el-button>
          </div>
        </div>
      </div>

      <!-- 二维码生成 -->
      <div v-if="activeTool === 'qrcode'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.qrcode.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.qrcode.desc') }}</span></h3>
        <div class="tool-actions">
          <el-input v-model="qrText" placeholder="输入文本或链接" style="width:400px" />
          <el-button type="primary" @click="generateQRCode">生成</el-button>
        </div>
        <div v-if="qrDataUrl" class="qrcode-result">
          <img :src="qrDataUrl" />
        </div>
      </div>

      <!-- ========== 文本处理 ========== -->

      <!-- 正则测试（保留完整常用模板） -->
      <div v-if="activeTool === 'regex'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.regex.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.regex.desc') }}</span></h3>

        <!-- 常用正则模板（按分组） -->
        <div class="regex-templates-wrap">
          <div v-for="group in regexTemplateGroups" :key="group.name" class="regex-group">
            <span class="regex-group-name">{{ group.name }}</span>
            <el-button v-for="tpl in group.items" :key="tpl.label" size="small" @click="applyRegexTemplate(tpl)">
              {{ tpl.label }}
              <span class="regex-tpl-pattern">{{ tpl.pattern }}</span>
            </el-button>
          </div>
        </div>

        <el-input v-model="regexPattern" placeholder="输入正则表达式，如 \d{11}" style="width:480px;margin-bottom:8px;font-family:monospace" />
        <el-input v-model="regexText" type="textarea" :rows="5" placeholder="输入测试文本" style="margin-bottom:8px" />
        <div class="tool-actions">
          <el-button type="primary" @click="testRegex">测试</el-button>
          <el-tag v-if="regexMatch !== null" :type="regexMatch.length>0?'success':'danger'">
            {{ regexMatch.length > 0 ? `匹配到 ${regexMatch.length} 条` : '无匹配' }}
          </el-tag>
        </div>
        <div v-if="regexMatch !== null && regexMatch.length > 0" class="regex-result-box">
          <div class="regex-result-header">匹配结果（共 {{ regexMatch.length }} 条）</div>
          <div v-for="(m,i) in regexMatch" :key="i" class="regex-match-item">{{ m }}</div>
        </div>
        <div v-else-if="regexMatch !== null && regexMatch.length === 0" class="regex-result-empty">未找到匹配内容</div>
      </div>

      <!-- 文本统计 -->
      <div v-if="activeTool === 'textstats'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.textstats.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.textstats.desc') }}</span></h3>
        <el-input v-model="textStatsInput" type="textarea" :rows="10" placeholder="输入或粘贴文本..." />
        <div class="tool-actions">
          <el-button type="primary" @click="calcTextStats">统计</el-button>
          <el-button @click="cleanText('trim')">去首尾空格</el-button>
          <el-button @click="cleanText('emptyLine')">去空白行</el-button>
          <el-button @click="cleanText('allSpace')">去所有空格换行</el-button>
          <el-button @click="copyTextStats">复制结果</el-button>
        </div>
        <div v-if="textStats" class="text-stats-result">
          <div class="stat-card"><span class="stat-val">{{ textStats.chars }}</span><span class="stat-lbl">字符数(含空格)</span></div>
          <div class="stat-card"><span class="stat-val">{{ textStats.charsNoSpace }}</span><span class="stat-lbl">字符数(无空格)</span></div>
          <div class="stat-card"><span class="stat-val">{{ textStats.words }}</span><span class="stat-lbl">字数(中文)</span></div>
          <div class="stat-card"><span class="stat-val">{{ textStats.lines }}</span><span class="stat-lbl">行数</span></div>
        </div>
      </div>

      <!-- HTML ↔ Markdown 互转 -->
      <div v-if="activeTool === 'mdhtml'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.mdhtml.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.mdhtml.desc') }}</span></h3>
        <div class="tool-actions">
          <el-radio-group v-model="mdhtmlDir">
            <el-radio value="md2html">Markdown → HTML</el-radio>
            <el-radio value="html2md">HTML → Markdown</el-radio>
          </el-radio-group>
        </div>
        <el-input v-model="mdhtmlInput" type="textarea" :rows="8" placeholder="输入Markdown或HTML内容" style="font-family:monospace;margin-top:8px" />
        <div class="tool-actions">
          <el-button type="primary" @click="convertMdhtml">转换</el-button>
          <el-button @click="copyOne(mdhtmlOutput)" :disabled="!mdhtmlOutput">复制结果</el-button>
        </div>
        <el-input v-model="mdhtmlOutput" type="textarea" :rows="8" readonly style="font-family:monospace;margin-top:8px" />
      </div>

      <!-- Emoji选择器 -->
      <div v-if="activeTool === 'emojipicker'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.emojipicker.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.emojipicker.desc') }}</span></h3>
        <div class="emoji-search-row">
          <el-input v-model="emojiSearch" placeholder="搜索Emoji..." size="small" style="width:260px" clearable @clear="clearEmojiSearch" />
          <div class="emoji-cats">
            <span v-for="c in emojiCategories" :key="c.key" class="emoji-cat-chip" :class="{ active: emojiCat === c.key }" @click="emojiCat = c.key">{{ c.label }}</span>
          </div>
        </div>
        <div class="emoji-grid" v-if="filteredEmojis.length">
          <span v-for="e in filteredEmojis" :key="e.c" class="emoji-item" :title="e.n" @click="copyEmoji(e.c)">{{ e.c }}</span>
        </div>
        <div v-else class="emoji-empty">未找到匹配的Emoji</div>
        <div v-if="emojiRecent.length" class="emoji-recent">
          <div class="emoji-recent-title">最近使用</div>
          <div class="emoji-grid emoji-grid-small">
            <span v-for="(c,i) in emojiRecent" :key="i" class="emoji-item" @click="copyEmoji(c)">{{ c }}</span>
          </div>
        </div>
      </div>

      <!-- ========== 时间日期 ========== -->

      <!-- 时间戳转换 -->
      <div v-if="activeTool === 'timestamp'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.timestamp.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.timestamp.desc') }}</span></h3>
        <el-radio-group v-model="tsMode" style="margin-bottom:12px">
          <el-radio value="toDate">时间戳 → 日期</el-radio>
          <el-radio value="toTs">日期 → 时间戳</el-radio>
        </el-radio-group>
        <div v-if="tsMode==='toDate'">
          <el-input v-model="tsInput" placeholder="输入时间戳(毫秒)" style="width:300px" />
          <el-button type="primary" @click="convertTimestamp" style="margin-left:8px">转换</el-button>
          <div v-if="tsResult" class="ts-result">{{ tsResult }}</div>
        </div>
        <div v-else>
          <el-date-picker v-model="tsDateInput" type="datetime" placeholder="选择日期时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:300px" />
          <el-button type="primary" @click="convertTimestamp" style="margin-left:8px">转换</el-button>
          <div v-if="tsResult" class="ts-result">{{ tsResult }}</div>
        </div>
      </div>

      <!-- 日期计算器 -->
      <div v-if="activeTool === 'datecalc'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.datecalc.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.datecalc.desc') }}</span></h3>
        <el-radio-group v-model="dateCalcMode" style="margin-bottom:12px">
          <el-radio value="diff">日期差值</el-radio>
          <el-radio value="add">日期加减</el-radio>
        </el-radio-group>
        <div v-if="dateCalcMode==='diff'" class="datecalc-row">
          <el-date-picker v-model="dateStart" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" style="width:180px" />
          <span style="margin:0 8px">至</span>
          <el-date-picker v-model="dateEnd" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" style="width:180px" />
          <el-button type="primary" @click="calcDateDiff" style="margin-left:8px">计算</el-button>
          <span v-if="dateDiffResult" class="ts-result">{{ dateDiffResult }}</span>
        </div>
        <div v-else class="datecalc-row">
          <el-date-picker v-model="dateBase" type="date" placeholder="基准日期" value-format="YYYY-MM-DD" style="width:180px" />
          <el-select v-model="dateAddOp" style="width:80px;margin:0 8px">
            <el-option label="加" value="add" />
            <el-option label="减" value="sub" />
          </el-select>
          <el-input-number v-model="dateAddDays" :min="0" :max="36500" style="width:140px" placeholder="天数" />
          <el-button type="primary" @click="calcDateAdd" style="margin-left:8px">计算</el-button>
          <span v-if="dateAddResult" class="ts-result">{{ dateAddResult }}</span>
        </div>
      </div>

      <!-- 时区转换 -->
      <div v-if="activeTool === 'timezone'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.timezone.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.timezone.desc') }}</span></h3>
        <div class="datecalc-row" style="margin-bottom:12px">
          <el-date-picker v-model="tzSourceTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:220px" />
          <el-select v-model="tzSourceZone" placeholder="源时区" style="width:200px" filterable>
            <el-option v-for="z in timezoneList" :key="z" :label="z" :value="z" />
          </el-select>
        </div>
        <div class="datecalc-row" style="margin-bottom:12px">
          <span style="font-size:14px;color:#606266;margin-right:8px">目标时区：</span>
          <el-select v-model="tzTargetZone" placeholder="目标时区" style="width:240px" filterable>
            <el-option v-for="z in timezoneList" :key="z" :label="z" :value="z" />
          </el-select>
          <el-button type="primary" @click="convertTimezone">转换</el-button>
        </div>
        <div v-if="tzResult" class="ts-result" style="background:#f5f7fa;padding:12px 16px;border-radius:6px;font-size:15px">
          {{ tzResult }}
        </div>
      </div>

      <!-- Cron表达式生成器 -->
      <div v-if="activeTool === 'cron'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.cron.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.cron.desc') }}</span></h3>
        <div class="cron-builder">
          <div class="cron-row">
            <span class="cron-label">秒</span>
            <el-input v-model="cronParts[0]" size="small" style="width:80px" placeholder="0" @input="syncCronExpr" />
          </div>
          <div class="cron-row">
            <span class="cron-label">分钟</span>
            <el-input v-model="cronParts[1]" size="small" style="width:80px" placeholder="*" @input="syncCronExpr" />
          </div>
          <div class="cron-row">
            <span class="cron-label">小时</span>
            <el-input v-model="cronParts[2]" size="small" style="width:80px" placeholder="*" @input="syncCronExpr" />
          </div>
          <div class="cron-row">
            <span class="cron-label">日</span>
            <el-input v-model="cronParts[3]" size="small" style="width:80px" placeholder="*" @input="syncCronExpr" />
          </div>
          <div class="cron-row">
            <span class="cron-label">月</span>
            <el-input v-model="cronParts[4]" size="small" style="width:80px" placeholder="*" @input="syncCronExpr" />
          </div>
          <div class="cron-row">
            <span class="cron-label">星期</span>
            <el-input v-model="cronParts[5]" size="small" style="width:80px" placeholder="*" @input="syncCronExpr" />
          </div>
        </div>
        <div class="tool-actions" style="margin-top:10px">
          <span style="font-size:13px;color:#606266">快捷：</span>
          <el-button size="small" @click="setCronQuick('* * * * * ? *')">每秒</el-button>
          <el-button size="small" @click="setCronQuick('0 * * * * ? *')">每分钟</el-button>
          <el-button size="small" @click="setCronQuick('0 0 * * * ? *')">每小时</el-button>
          <el-button size="small" @click="setCronQuick('0 0 0 * * ? *')">每天0点</el-button>
          <el-button size="small" @click="setCronQuick('0 0 0 ? * MON')">每周一0点</el-button>
        </div>
        <div v-if="cronExpr" class="cron-preview" style="margin-top:12px">
          <div class="cron-expr-box">
            <span style="color:#909399;font-size:13px">Cron表达式：</span>
            <code style="font-size:16px;color:#303133;font-weight:600;background:#e8eaed;padding:4px 12px;border-radius:4px">{{ cronExpr }}</code>
            <el-button size="small" link type="primary" @click="copyOne(cronExpr)">复制</el-button>
          </div>
          <div v-if="cronNextRuns.length" style="margin-top:10px">
            <span style="color:#909399;font-size:12px">最近5次执行时间：</span>
            <div v-for="(t,i) in cronNextRuns" :key="i" class="cron-run-item">{{ t }}</div>
          </div>
        </div>
      </div>

      <!-- ========== 代码工具 ========== -->

      <!-- 代码格式化 -->
      <div v-if="activeTool === 'codefmt'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.codefmt.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.codefmt.desc') }}</span></h3>
        <div class="tool-actions">
          <el-select v-model="codeFmtLang" style="width:130px">
            <el-option label="HTML" value="html" />
            <el-option label="CSS" value="css" />
            <el-option label="JavaScript" value="js" />
            <el-option label="SQL" value="sql" />
          </el-select>
          <el-button type="primary" @click="formatCode">格式化</el-button>
          <el-button @click="copyOne(codeFmtOutput)" :disabled="!codeFmtOutput">复制结果</el-button>
        </div>
        <div style="display:flex;gap:12px;margin-top:12px">
          <el-input v-model="codeFmtInput" type="textarea" :rows="16" placeholder="粘贴需要格式化的代码..." style="flex:1;font-family:monospace;font-size:13px" />
          <el-input v-model="codeFmtOutput" type="textarea" :rows="16" readonly placeholder="格式化结果" style="flex:1;font-family:monospace;font-size:13px" />
        </div>
      </div>

      <!-- CSS渐变生成器 -->
      <div v-if="activeTool === 'cssgradient'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.cssgradient.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.cssgradient.desc') }}</span></h3>
        <div class="cssgrad-layout">
          <div class="cssgrad-config">
            <div class="cssgrad-row">
              <span>渐变类型：</span>
              <el-radio-group v-model="gradType" size="small" @change="updateCssGradient">
                <el-radio value="linear">线性</el-radio>
                <el-radio value="radial">径向</el-radio>
              </el-radio-group>
            </div>
            <div class="cssgrad-row" v-if="gradType==='linear'">
              <span>角度：</span>
              <el-slider v-model="gradAngle" :min="0" :max="360" style="width:240px" show-input @input="updateCssGradient" />
            </div>
            <div class="cssgrad-row">
              <span>起始颜色：</span>
              <el-color-picker v-model="gradColor1" @change="updateCssGradient" />
              <el-input v-model="gradColor1" size="small" style="width:100px;font-family:monospace" @input="updateCssGradient" />
            </div>
            <div class="cssgrad-row">
              <span>结束颜色：</span>
              <el-color-picker v-model="gradColor2" @change="updateCssGradient" />
              <el-input v-model="gradColor2" size="small" style="width:100px;font-family:monospace" @input="updateCssGradient" />
            </div>
            <div class="cssgrad-preview" :style="{ background: gradCss }" />
            <div style="margin-top:12px">
              <div class="cssgrad-code-title">CSS代码：</div>
              <code class="cssgrad-code">{{ gradCss }}</code>
              <el-button size="small" style="margin-top:6px" @click="copyOne(gradCss)">复制CSS</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- YML ↔ Properties 互转 -->
      <div v-if="activeTool === 'ymlprops'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.ymlprops.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.ymlprops.desc') }}</span></h3>
        <div class="tool-actions">
          <el-radio-group v-model="ymlDir">
            <el-radio value="yml2prop">YML → Properties</el-radio>
            <el-radio value="prop2yml">Properties → YML</el-radio>
          </el-radio-group>
        </div>
        <div style="display:flex;gap:12px;margin-top:12px">
          <el-input v-model="ymlInput" type="textarea" :rows="14" :placeholder="ymlDir==='yml2prop'?'server:\n  port: 8080\n  host: localhost':'server.port=8080\nserver.host=localhost'" style="flex:1;font-family:monospace;font-size:13px" />
          <el-input v-model="ymlOutput" type="textarea" :rows="14" readonly placeholder="转换结果" style="flex:1;font-family:monospace;font-size:13px" />
        </div>
        <div class="tool-actions">
          <el-button type="primary" @click="convertYml">转换</el-button>
          <el-button @click="copyOne(ymlOutput)" :disabled="!ymlOutput">复制结果</el-button>
        </div>
      </div>

      <!-- ========== 颜色工具 ========== -->

      <!-- 颜色转换器 -->
      <div v-if="activeTool === 'colortool'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.colortool.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.colortool.desc') }}</span></h3>
        <div class="color-row">
          <div class="color-picker-wrap">
            <el-color-picker v-model="colorPickerVal" show-alpha @change="onColorPick" />
            <span class="color-preview" :style="{background:colorPickerVal}"></span>
          </div>
          <div class="color-values">
            <div class="color-field">
              <span class="color-label">HEX：</span>
              <el-input v-model="colorHex" placeholder="#f59e0b" style="width:160px;font-family:monospace" @input="onHexInput" />
              <el-button size="small" @click="copyColor('hex')" :disabled="!colorHex">复制</el-button>
            </div>
            <div class="color-field">
              <span class="color-label">RGB：</span>
              <el-input v-model="colorRgb" placeholder="rgb(64,158,255)" style="width:220px;font-family:monospace" @input="onRgbInput" />
              <el-button size="small" @click="copyColor('rgb')" :disabled="!colorRgb">复制</el-button>
            </div>
            <div class="color-field">
              <span class="color-label">HSL：</span>
              <el-input v-model="colorHsl" placeholder="hsl(211,100%,63%)" style="width:220px;font-family:monospace" @input="onHslInput" />
              <el-button size="small" @click="copyColor('hsl')" :disabled="!colorHsl">复制</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- ========== 图像工具 ========== -->

      <!-- 图片压缩 -->
      <div v-if="activeTool === 'imgcompress'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.imgcompress.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.imgcompress.desc') }}</span></h3>
        <div class="img-tool-upload" v-if="!imgCompressSrc">
          <el-upload drag :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="onImgCompressUpload">
            <el-icon style="font-size:42px;color:#c0c4cc"><Plus /></el-icon>
            <div style="color:#909399;margin-top:8px">拖拽或点击上传图片</div>
          </el-upload>
        </div>
        <template v-else>
          <div class="img-compress-config">
            <div class="img-config-row">
              <span>压缩质量：</span>
              <el-slider v-model="imgCompressQuality" :min="0.1" :max="1" :step="0.05" style="width:200px" show-input :format-tooltip="v => Math.round(v*100)+'%'" />
            </div>
            <div class="img-config-row">
              <span>最大宽度：</span>
              <el-input-number v-model="imgCompressMaxWidth" :min="100" :max="8000" :step="100" style="width:140px" /> px
              <span style="margin-left:16px">最大高度：</span>
              <el-input-number v-model="imgCompressMaxHeight" :min="100" :max="8000" :step="100" style="width:140px" /> px
            </div>
            <div class="tool-actions">
              <el-button type="primary" @click="compressImage">压缩</el-button>
              <el-button @click="resetImgCompress">重新上传</el-button>
              <el-button v-if="imgCompressedSrc" type="success" @click="downloadImg(imgCompressedSrc, 'compressed')">下载压缩图</el-button>
            </div>
          </div>
          <div class="img-compare">
            <div class="img-compare-item">
              <div class="img-compare-title">原图<span class="img-size-tag">{{ imgCompressOrigSize }}</span></div>
              <img :src="imgCompressSrc" class="img-compare-preview" />
            </div>
            <div class="img-compare-item" v-if="imgCompressedSrc">
              <div class="img-compare-title">压缩后<span class="img-size-tag img-size-green">{{ imgCompressNewSize }}</span></div>
              <img :src="imgCompressedSrc" class="img-compare-preview" />
            </div>
          </div>
        </template>
      </div>

      <!-- 图片水印 -->
      <div v-if="activeTool === 'imgwatermark'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.imgwatermark.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.imgwatermark.desc') }}</span></h3>
        <div v-if="!wmSrc" class="img-tool-upload">
          <el-upload drag :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="onWmUpload">
            <el-icon style="font-size:42px;color:#c0c4cc"><Plus /></el-icon>
            <div style="color:#909399;margin-top:8px">拖拽或点击上传图片</div>
          </el-upload>
        </div>
        <template v-else>
          <div class="wm-config">
            <div class="img-config-row">
              <span>水印文字：</span>
              <el-input v-model="wmText" placeholder="请输入水印文字" style="width:240px" />
            </div>
            <div class="img-config-row">
              <span>位置：</span>
              <el-select v-model="wmPosition" style="width:120px">
                <el-option label="左上角" value="topLeft" />
                <el-option label="右上角" value="topRight" />
                <el-option label="居中" value="center" />
                <el-option label="左下角" value="bottomLeft" />
                <el-option label="右下角" value="bottomRight" />
                <el-option label="平铺" value="tile" />
              </el-select>
              <span style="margin-left:12px">字号：</span>
              <el-input-number v-model="wmFontSize" :min="12" :max="120" style="width:110px" />
            </div>
            <div class="img-config-row">
              <span>颜色：</span>
              <el-color-picker v-model="wmColor" />
              <span style="margin-left:12px">透明度：</span>
              <el-slider v-model="wmOpacity" :min="0.05" :max="1" :step="0.05" style="width:160px" show-input :format-tooltip="v => Math.round(v*100)+'%'" />
            </div>
            <div class="tool-actions">
              <el-button type="primary" @click="addWatermark">生成水印</el-button>
              <el-button @click="resetWm">重新上传</el-button>
              <el-button v-if="wmResult" type="success" @click="downloadImg(wmResult, 'watermarked')">下载</el-button>
            </div>
          </div>
          <div class="img-compare">
            <div class="img-compare-item">
              <div class="img-compare-title">原图</div>
              <img :src="wmSrc" class="img-compare-preview" />
            </div>
            <div class="img-compare-item" v-if="wmResult">
              <div class="img-compare-title">水印效果</div>
              <img :src="wmResult" class="img-compare-preview" />
            </div>
          </div>
        </template>
      </div>

      <!-- 图片转ICO -->
      <div v-if="activeTool === 'imgtoico'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.imgtoico.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.imgtoico.desc') }}</span></h3>
        <div v-if="!icoSrc" class="img-tool-upload">
          <el-upload drag :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="onIcoUpload">
            <el-icon style="font-size:42px;color:#c0c4cc"><Plus /></el-icon>
            <div style="color:#909399;margin-top:8px">拖拽或点击上传图片（建议正方形）</div>
          </el-upload>
        </div>
        <template v-else>
          <div class="ico-config">
            <div class="img-config-row">
              <span>ICO尺寸：</span>
              <el-checkbox-group v-model="icoSizes">
                <el-checkbox v-for="s in [16,24,32,48,64,128,256]" :key="s" :value="s">{{ s }}×{{ s }}</el-checkbox>
              </el-checkbox-group>
            </div>
            <div class="tool-actions">
              <el-button type="primary" @click="convertToIco">生成ICO</el-button>
              <el-button @click="resetIco">重新上传</el-button>
              <el-button v-if="icoResultSrc" type="success" @click="downloadIco">下载ICO文件</el-button>
            </div>
          </div>
          <div class="img-compare">
            <div class="img-compare-item">
              <div class="img-compare-title">原图</div>
              <img :src="icoSrc" class="img-compare-preview" style="max-height:200px" />
            </div>
            <div class="img-compare-item" v-if="icoPreviews.length">
              <div class="img-compare-title">ICO预览</div>
              <div class="ico-preview-grid">
                <div v-for="p in icoPreviews" :key="p.size" class="ico-preview-item">
                  <img :src="p.src" :style="{width:p.size+'px',height:p.size+'px'}" />
                  <span>{{ p.size }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>

      <!-- Base64图片转换 -->
      <div v-if="activeTool === 'base64img'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.base64img.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.base64img.desc') }}</span></h3>
        <div class="b64img-sections">
          <div class="b64img-section">
            <div class="b64img-section-title">图片 → Base64</div>
            <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="onB64ImageUpload">
              <el-button type="primary" size="small">选择图片</el-button>
            </el-upload>
            <el-input v-model="b64ImgResult" type="textarea" :rows="4" readonly placeholder="上传图片后将显示Base64编码" style="font-family:monospace;font-size:12px;margin-top:8px" />
            <el-button v-if="b64ImgResult" size="small" style="margin-top:6px" @click="copyOne(b64ImgResult)">复制Base64</el-button>
          </div>
          <div class="b64img-section">
            <div class="b64img-section-title">Base64 → 图片</div>
            <el-input v-model="b64DecodeInput" type="textarea" :rows="4" placeholder="粘贴Base64编码字符串（可含 data:image/...;base64, 前缀）" style="font-family:monospace;font-size:12px" />
            <div class="tool-actions" style="margin-top:8px">
              <el-button type="primary" size="small" @click="decodeB64Image">解码预览</el-button>
              <el-button v-if="b64DecodedSrc" size="small" type="success" @click="downloadImg(b64DecodedSrc, 'base64-decoded')">下载图片</el-button>
            </div>
            <div v-if="b64DecodedSrc" style="margin-top:10px">
              <img :src="b64DecodedSrc" style="max-width:100%;max-height:300px;border:1px solid #e4e7ed;border-radius:4px" />
            </div>
          </div>
        </div>
      </div>

      <!-- 图标设计器 -->
      <div v-if="activeTool === 'icondesign'" class="tool-panel">
        <h3 class="tool-title">{{ $t('tool.devTools.tools.icondesign.name') }}<span class="tool-desc"> — {{ $t('tool.devTools.tools.icondesign.desc') }}</span></h3>
        <div class="icondesign-layout">
          <div class="icondesign-config">
            <div class="img-config-row">
              <span>形状：</span>
              <el-select v-model="iconDesignShape" style="width:130px" @change="renderIconDesign">
                <el-option label="圆形" value="circle" />
                <el-option label="正方形" value="square" />
                <el-option label="圆角矩形" value="rounded" />
              </el-select>
            </div>
            <div class="img-config-row">
              <span>背景色：</span>
              <el-color-picker v-model="iconDesignBg" @change="renderIconDesign" />
              <el-input v-model="iconDesignBg" size="small" style="width:100px;font-family:monospace" @input="renderIconDesign" />
            </div>
            <div class="img-config-row">
              <span>文字颜色：</span>
              <el-color-picker v-model="iconDesignFg" @change="renderIconDesign" />
              <el-input v-model="iconDesignFg" size="small" style="width:100px;font-family:monospace" @input="renderIconDesign" />
            </div>
            <div class="img-config-row">
              <span>图标文字：</span>
              <el-input v-model="iconDesignText" placeholder="输入文字/表情" style="width:160px" maxlength="4" @input="renderIconDesign" />
            </div>
            <div class="img-config-row">
              <span>字号：</span>
              <el-input-number v-model="iconDesignFontSize" :min="20" :max="200" style="width:120px" @change="renderIconDesign" />
              <span style="margin-left:12px">大小：</span>
              <el-input-number v-model="iconDesignSize" :min="64" :max="1024" :step="64" style="width:120px" @change="renderIconDesign" />
            </div>
            <div class="tool-actions">
              <el-button type="primary" @click="downloadIconDesign">下载PNG</el-button>
            </div>
          </div>
          <div class="icondesign-preview">
            <div class="icondesign-preview-title">预览</div>
            <canvas ref="iconDesignCanvas" class="icondesign-canvas" />
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { RefreshRight, Switch, Tickets, Timer, BrushFilled, Lock, Link, Document, Picture, Plus } from '@element-plus/icons-vue'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'
import { COLORS } from '@/config/colors'

const { t } = useI18n()

// 工具分类图标映射
const catIcons = { jsoncat: Switch, encode: Lock, network: Link, generate: RefreshRight, text: Tickets, time: Timer, codetool: Document, color: BrushFilled, imgtool: Picture }

// 分类中工具 key 列表（与 i18n key 对应）
const catToolKeys = {
  jsoncat: ['json', 'jsoneditor', 'jsonconvert'],
  encode: ['base64', 'urlcodec', 'unicode', 'htmlentity', 'radix', 'crypto', 'jwt'],
  network: ['http', 'ipquery'],
  generate: ['uuid', 'passgen', 'qrcode'],
  text: ['regex', 'textstats', 'mdhtml', 'emojipicker'],
  time: ['timestamp', 'datecalc', 'timezone', 'cron'],
  codetool: ['codefmt', 'cssgradient', 'ymlprops'],
  color: ['colortool'],
  imgtool: ['imgcompress', 'imgwatermark', 'imgtoico', 'base64img', 'icondesign'],
}

const catKeys = ['jsoncat', 'encode', 'network', 'generate', 'text', 'time', 'codetool', 'color', 'imgtool']

// ===== 分类导航（支持i18n） =====
const categories = computed(() => catKeys.map(ck => ({
  key: ck,
  name: t(`tool.devTools.categories.${ck}`),
  icon: catIcons[ck],
  tools: catToolKeys[ck].map(tk => ({ key: tk, name: t(`tool.devTools.tools.${tk}.name`) })),
})))

const activeTool = ref('json')
function handleSelect(key) {
  if (key) activeTool.value = key
}

// ===== 编码转换 =====

// JSON
const jsonInput = ref(''); const jsonOutput = ref('')
function formatJson() { try { jsonOutput.value = JSON.stringify(JSON.parse(jsonInput.value), null, 2) } catch(e) { ElMessage.error('JSON格式错误') } }
function compressJson() { try { jsonOutput.value = JSON.stringify(JSON.parse(jsonInput.value)) } catch(e) { ElMessage.error('JSON格式错误') } }

// Base64
const base64Input = ref(''); const base64Output = ref('')
function base64Encode() { try { base64Output.value = btoa(unescape(encodeURIComponent(base64Input.value))) } catch(e) { ElMessage.error('编码失败') } }
function base64Decode() { try { base64Output.value = decodeURIComponent(escape(atob(base64Input.value))) } catch(e) { ElMessage.error('解码失败') } }

// URL编解码
const urlInput = ref(''); const urlOutput = ref('')
function urlEncode() { try { urlOutput.value = encodeURIComponent(urlInput.value) } catch(e) { ElMessage.error('编码失败') } }
function urlDecode() { try { urlOutput.value = decodeURIComponent(urlInput.value) } catch(e) { ElMessage.error('解码失败') } }

// Unicode互转
const unicodeText = ref(''); const unicodeResult = ref('')
function toUnicode() {
  unicodeResult.value = unicodeText.value.split('').map(c => '\\u' + c.charCodeAt(0).toString(16).padStart(4, '0')).join('')
}
function fromUnicode() {
  unicodeResult.value = unicodeText.value.replace(/\\u([0-9A-Fa-f]{4})/g, (_, hex) => String.fromCharCode(parseInt(hex, 16)))
}

// HTML实体编解码
const htmlInput = ref(''); const htmlOutput = ref('')
const htmlEntities = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }
const htmlReverse = Object.fromEntries(Object.entries(htmlEntities).map(([k,v]) => [v,k]))
function htmlEncode() {
  htmlOutput.value = htmlInput.value.replace(/[&<>"']/g, ch => htmlEntities[ch] || ch)
}
function htmlDecode() {
  htmlOutput.value = htmlInput.value.replace(/&(?:amp|lt|gt|quot|#39);/g, ent => htmlReverse[ent] || ent)
}

// 进制转换
const radixInput = ref(''); const radixFrom = ref(10); const radixResults = ref(null)
function convertRadix() {
  const val = radixInput.value.trim()
  if (!val) { radixResults.value = null; return }
  try {
    const num = parseInt(val, radixFrom.value)
    if (isNaN(num)) { radixResults.value = null; ElMessage.error('无效的数值'); return }
    radixResults.value = {
      bin: '0b' + num.toString(2),
      oct: '0o' + num.toString(8),
      dec: String(num),
      hex: '0x' + num.toString(16).toUpperCase(),
    }
  } catch(e) { radixResults.value = null }
}

// ===== 生成器 =====

// UUID
const uuidCount = ref(5); const uuids = ref([])
function generateUuid() {
  uuids.value = Array.from({length:uuidCount.value}, () =>
    crypto.randomUUID ? crypto.randomUUID() : 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,c=>{
      const r=Math.random()*16|0;return(c==='x'?r:(r&0x3|0x8)).toString(16)
    }))
}
async function copyUuids() { await navigator.clipboard.writeText(uuids.value.join('\n')); ElMessage.success('已复制') }

// 随机密码
const passLength = ref(12); const passCount = ref(5)
const passUpper = ref(true); const passLower = ref(true); const passDigit = ref(true); const passSymbol = ref(true)
const passwords = ref([])

const CHARSETS = {
  upper: 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
  lower: 'abcdefghijklmnopqrstuvwxyz',
  digit: '0123456789',
  symbol: '!@#$%^&*()_+-=[]{}|;:,.<>?'
}

function generatePassword() {
  let pool = ''
  if (passUpper.value) pool += CHARSETS.upper
  if (passLower.value) pool += CHARSETS.lower
  if (passDigit.value) pool += CHARSETS.digit
  if (passSymbol.value) pool += CHARSETS.symbol
  if (!pool) { ElMessage.error('请至少选择一种字符类型'); return }
  passwords.value = Array.from({length:passCount.value}, () =>
    Array.from({length:passLength.value}, () => pool[Math.floor(Math.random() * pool.length)]).join('')
  )
}
async function copyPasswords() { await navigator.clipboard.writeText(passwords.value.join('\n')); ElMessage.success('已复制') }
async function copyOne(text) { await navigator.clipboard.writeText(text); ElMessage.success('已复制') }

// QR Code
const qrText = ref(''); const qrDataUrl = ref('')
function generateQRCode() {
  if (!qrText.value.trim()) { ElMessage.error('请输入内容'); return }
  qrDataUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(qrText.value)}`
}

// ===== 文本处理 =====

// 正则测试（保留全部模板）
const regexPattern = ref(''); const regexText = ref(''); const regexMatch = ref(null)

const regexTemplateGroups = [
  {
    name: '数字',
    items: [
      { label: '全部数字', pattern: '\\d+', text: '订单12345，金额99，abc999 2024年6月' },
      { label: '纯数字行', pattern: '^\\d+$', text: '12345\nabc123\n67890\nhello' },
      { label: '正整数', pattern: '(?<![.-])\\b[1-9]\\d*\\b', text: '数量0 -5 42 3.14 100 007' },
      { label: '小数', pattern: '\\d+\\.\\d+', text: '价格3.14 0.5 100 42.0 .8 2.718' },
      { label: '两位小数', pattern: '\\d+\\.\\d{2}', text: '总价199.00 优惠5.5 税费13.56 余额1000' },
    ]
  },
  {
    name: '字母',
    items: [
      { label: '全字母', pattern: '[A-Za-z]+', text: 'hello world 123 ABC 你好 xyz' },
      { label: '纯字母行', pattern: '^[A-Za-z]+$', text: 'hello\nabc123\nWORLD\ntest2' },
      { label: '小写字母', pattern: '[a-z]+', text: 'Hello World abc DEF 123 ghi' },
      { label: '大写字母', pattern: '[A-Z]+', text: 'Hello World ABC def 123 GHI' },
      { label: '字母数字', pattern: '\\w+', text: 'user_123 hello 你好 admin@2024 test' },
    ]
  },
  {
    name: '指定内容',
    items: [
      { label: '包含字符串', pattern: 'error|fail|异常', text: '操作成功 error occurred 任务完成 网络异常 fail' },
      { label: '以xx开头', pattern: '^https?://', text: 'https://a.com\nftp://b.com\nhttp://c.com' },
      { label: '以xx结尾', pattern: '\\.(jpg|png|gif)$', text: 'photo.jpg\ndoc.pdf\nicon.png\nbanner.jpeg' },
      { label: '指定长度', pattern: '^\\w{6,12}$', text: 'abc\n123456\nadmin2024\ntoolong123456' },
      { label: '排除字符', pattern: '[^0-9a-zA-Z]+', text: 'abc123你好！@#$% world 456' },
    ]
  },
  {
    name: '账号/ID',
    items: [
      { label: '手机号', pattern: '\\b1[3-9]\\d{9}\\b', text: '张三13800138001 李四13912345678 王五12345678901' },
      { label: '邮箱', pattern: '[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+', text: '联系：admin@example.com 和 test@mail.cn' },
      { label: '身份证号', pattern: '[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]', text: '张三110105199001011234 李五35010219851231002X' },
      { label: 'QQ号', pattern: '[1-9]\\d{4,10}', text: 'QQ:10001 客服:12345 群号:invalid99' },
      { label: 'IP地址', pattern: '\\b(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\b', text: '服务器:192.168.1.1 DNS:8.8.8.8 无效:999.1.1.1' },
    ]
  },
  {
    name: '内容过滤',
    items: [
      { label: '中文', pattern: '[\\u4e00-\\u9fa5]+', text: 'Hello 世界！你好 World 中文测试' },
      { label: '空白行', pattern: '^\\s*$', text: 'hello\n\nworld\n  \ntest\n' },
      { label: 'HTML标签', pattern: '<[^>]+>', text: '<div><p>Hello</p>World<br/>end</div>' },
      { label: 'Hex颜色', pattern: '#[0-9A-Fa-f]{3,8}\\b', text: '主色#f59e0b 背景#fff 边框#e4e7ed 错误#F56C6C' },
      { label: 'URL链接', pattern: 'https?://[\\w./?=&#@%+~-]+', text: '访问 https://www.example.com/path?q=1 获取信息' },
    ]
  },
  {
    name: '日期/金额',
    items: [
      { label: '完整日期', pattern: '\\d{4}[-/]\\d{2}[-/]\\d{2}', text: '出生2024-06-07 入职2025/01/15 2024-6-7' },
      { label: '日期时间', pattern: '\\d{4}[-/]\\d{2}[-/]\\d{2}\\s+\\d{2}:\\d{2}', text: '创建于2024-06-07 14:30 更新2025/01/15 09:00' },
      { label: '金额', pattern: '\\d+(\\.\\d{1,2})?', text: '总价1999.00，优惠50，合计1949.50元' },
      { label: '金额(带单位)', pattern: '[¥￥$]?\\d+(\\.\\d{1,2})?[元]?', text: '商品¥199.00 运费20元 税费$5.50' },
    ]
  },
]

function applyRegexTemplate(tpl) {
  regexPattern.value = tpl.pattern
  regexText.value = tpl.text
  testRegex()
}

function testRegex() {
  try {
    const re = new RegExp(regexPattern.value, 'gm')
    const matches = regexText.value.match(re)
    regexMatch.value = matches || []
  } catch(e) {
    ElMessage.error('正则表达式格式错误: ' + e.message)
    regexMatch.value = null
  }
}

// 文本统计与清洗
const textStatsInput = ref(''); const textStats = ref(null)

function calcTextStats() {
  const t = textStatsInput.value
  textStats.value = {
    chars: t.length,
    charsNoSpace: t.replace(/\s/g, '').length,
    words: (t.match(/[\u4e00-\u9fa5]/g) || []).length,
    lines: t ? t.split('\n').length : 0,
  }
}

function cleanText(mode) {
  let t = textStatsInput.value
  if (mode === 'trim') t = t.split('\n').map(l => l.trim()).join('\n')
  else if (mode === 'emptyLine') t = t.split('\n').filter(l => l.trim() !== '').join('\n')
  else if (mode === 'allSpace') t = t.replace(/\s+/g, '')
  textStatsInput.value = t
  calcTextStats()
}

async function copyTextStats() { await navigator.clipboard.writeText(textStatsInput.value); ElMessage.success('已复制') }

// ===== 时间日期 =====

// 时间戳转换
const tsMode = ref('toDate'); const tsInput = ref(''); const tsDateInput = ref(''); const tsResult = ref('')
function convertTimestamp() {
  if (tsMode.value === 'toDate') {
    const ts = parseInt(tsInput.value); if (isNaN(ts)) { ElMessage.error('请输入有效的时间戳'); return }
    tsResult.value = new Date(ts).toLocaleString('zh-CN', { hour12: false })
  } else {
    if (!tsDateInput.value) { ElMessage.error('请选择日期'); return }
    tsResult.value = String(new Date(tsDateInput.value).getTime())
  }
}

// 日期计算器
const dateCalcMode = ref('diff')
const dateStart = ref(''); const dateEnd = ref(''); const dateDiffResult = ref('')
const dateBase = ref(''); const dateAddOp = ref('add'); const dateAddDays = ref(0); const dateAddResult = ref('')

function calcDateDiff() {
  if (!dateStart.value || !dateEnd.value) { ElMessage.error('请选择日期'); return }
  const d1 = new Date(dateStart.value); const d2 = new Date(dateEnd.value)
  const diff = Math.abs(d2 - d1) / (1000 * 60 * 60 * 24)
  dateDiffResult.value = `间隔 ${diff} 天`
}

function calcDateAdd() {
  if (!dateBase.value) { ElMessage.error('请选择基准日期'); return }
  const d = new Date(dateBase.value)
  const days = dateAddOp.value === 'add' ? dateAddDays.value : -dateAddDays.value
  d.setDate(d.getDate() + days)
  dateAddResult.value = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

// ===== 颜色工具 =====

const colorPickerVal = ref('#409EFF')
const colorHex = ref('#409EFF')
const colorRgb = ref('rgb(64, 158, 255)')
const colorHsl = ref('hsl(211, 100%, 63%)')

function hexToRgb(hex) {
  const h = hex.replace('#','')
  const r = parseInt(h.substring(0,2), 16)
  const g = parseInt(h.substring(2,4), 16)
  const b = parseInt(h.substring(4,6), 16)
  return { r, g, b }
}

function rgbToHsl(r, g, b) {
  r /= 255; g /= 255; b /= 255
  const max = Math.max(r,g,b), min = Math.min(r,g,b)
  let h = 0, s = 0, l = (max + min) / 2
  if (max !== min) {
    const d = max - min
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
    if (max === r) h = ((g - b) / d + (g < b ? 6 : 0)) * 60
    else if (max === g) h = ((b - r) / d + 2) * 60
    else h = ((r - g) / d + 4) * 60
  }
  return { h: Math.round(h), s: Math.round(s*100), l: Math.round(l*100) }
}

function syncColorsFromHex(hex) {
  try {
    const { r, g, b } = hexToRgb(hex)
    const { h, s, l } = rgbToHsl(r, g, b)
    colorHex.value = hex
    colorRgb.value = `rgb(${r}, ${g}, ${b})`
    colorHsl.value = `hsl(${h}, ${s}%, ${l}%)`
  } catch(e) {}
}

function onColorPick(val) {
  if (!val) return
  colorPickerVal.value = val
  syncColorsFromHex(val)
}

function onHexInput(val) {
  if (/^#[0-9A-Fa-f]{6}$/.test(val)) syncColorsFromHex(val)
}

function onRgbInput(val) {
  const m = val.match(/rgb\((\d+),\s*(\d+),\s*(\d+)\)/)
  if (m) {
    const [_,r,g,b] = m
    const hex = '#' + [r,g,b].map(v => parseInt(v).toString(16).padStart(2,'0')).join('').toUpperCase()
    syncColorsFromHex(hex)
    colorPickerVal.value = hex
  }
}

function onHslInput(val) {
  const m = val.match(/hsl\((\d+),\s*(\d+)%?,\s*(\d+)%?\)/)
  if (m) {
    const h = parseInt(m[1]), s = parseInt(m[2]), l = parseInt(m[3])
    // HSL to RGB
    const _s = s / 100, _l = l / 100
    const c = (1 - Math.abs(2 * _l - 1)) * _s
    const x = c * (1 - Math.abs((h / 60) % 2 - 1))
    const m0 = _l - c / 2
    let r1=0,g1=0,b1=0
    if (h < 60) { r1=c;g1=x;b1=0 }
    else if (h < 120) { r1=x;g1=c;b1=0 }
    else if (h < 180) { r1=0;g1=c;b1=x }
    else if (h < 240) { r1=0;g1=x;b1=c }
    else if (h < 300) { r1=x;g1=0;b1=c }
    else { r1=c;g1=0;b1=x }
    const r = Math.round((r1+m0)*255), g = Math.round((g1+m0)*255), b = Math.round((b1+m0)*255)
    const hex = '#' + [r,g,b].map(v => v.toString(16).padStart(2,'0')).join('').toUpperCase()
    syncColorsFromHex(hex)
    colorPickerVal.value = hex
  }
}

async function copyColor(fmt) {
  let text = ''
  if (fmt === 'hex') text = colorHex.value
  else if (fmt === 'rgb') text = colorRgb.value
  else if (fmt === 'hsl') text = colorHsl.value
  await navigator.clipboard.writeText(text)
  ElMessage.success('已复制')
}

// ===== JSON工具（JSON编辑器 + JSON转换） =====

// JSON编辑器
const jsonEditInput = ref(''); const jsonEditValid = ref(null); const jsonEditTree = ref('')
function jsonEditFormat() {
  try { const o = JSON.parse(jsonEditInput.value); jsonEditInput.value = JSON.stringify(o, null, 2); jsonEditValid.value = true; renderJsonTree(o) } catch(e) { jsonEditValid.value = false }
}
function jsonEditCompress() {
  try { const o = JSON.parse(jsonEditInput.value); jsonEditInput.value = JSON.stringify(o); jsonEditValid.value = true } catch(e) { jsonEditValid.value = false }
}
function jsonEditValidate() {
  try { JSON.parse(jsonEditInput.value); jsonEditValid.value = true; ElMessage.success('JSON格式有效') } catch(e) { jsonEditValid.value = false; ElMessage.error('JSON格式错误: ' + e.message) }
}
function jsonEditOnInput() { try { const o = JSON.parse(jsonEditInput.value); jsonEditValid.value = true; renderJsonTree(o) } catch(e) { jsonEditValid.value = null; jsonEditTree.value = '' } }
function renderJsonTree(obj, depth = 0) {
  const indent = '&nbsp;'.repeat(depth * 3)
  if (obj === null) return `<span style="color:${COLORS.TEXT_SECONDARY}">null</span>`
  if (typeof obj !== 'object') {
    const c = typeof obj === 'string' ? COLORS.SUCCESS : typeof obj === 'number' ? COLORS.WARNING : COLORS.STATUS.WARNING
    return `<span style="color:${c}">${JSON.stringify(obj)}</span>`
  }
  if (Array.isArray(obj)) {
    let html = `<span style="color:${COLORS.TEXT_SECONDARY}">[</span><br>`
    obj.forEach((v, i) => { html += `${indent}&nbsp;&nbsp;<span style="color:${COLORS.TEXT_SECONDARY}">${i}:</span> ${renderJsonTree(v, depth+1)}<br>` })
    html += `${indent}<span style="color:${COLORS.TEXT_SECONDARY}">]</span>`
    return html
  }
  let html = `<span style="color:${COLORS.TEXT_SECONDARY}">{</span><br>`
  Object.entries(obj).forEach(([k, v]) => { html += `${indent}&nbsp;&nbsp;<span style="color:${COLORS.TEXT_PRIMARY}">"${k}":</span> ${renderJsonTree(v, depth+1)}<br>` })
  html += `${indent}<span style="color:${COLORS.TEXT_SECONDARY}">}</span>`
  return html
}

// JSON转换
const jsonConvertInput = ref(''); const jsonConvertTo = ref('xml'); const jsonConvertOutput = ref('')
function doJsonConvert() {
  try {
    const val = jsonConvertInput.value.trim()
    if (jsonConvertTo.value === 'xml') {
      const obj = JSON.parse(val)
      const toXml = (o, root = 'root') => `<${root}>${typeof o === 'object' ? Object.entries(o).map(([k,v]) => toXml(v, k)).join('') : o}</${root}>`
      jsonConvertOutput.value = '<?xml version="1.0" encoding="UTF-8"?>\n' + toXml(obj)
    } else if (jsonConvertTo.value === 'csv') {
      const arr = JSON.parse(val)
      if (!Array.isArray(arr) || !arr.length) { ElMessage.error('请提供JSON数组'); return }
      const keys = Object.keys(arr[0])
      const lines = [keys.join(',')]
      arr.forEach(row => lines.push(keys.map(k => {
        const v = row[k]
        return typeof v === 'string' && (v.includes(',') || v.includes('"')) ? `"${v.replace(/"/g,'""')}"` : v
      }).join(',')))
      jsonConvertOutput.value = lines.join('\n')
    } else if (jsonConvertTo.value === 'xml2json') {
      const parser = new DOMParser()
      const doc = parser.parseFromString(val, 'text/xml')
      function xmlToJson(el) {
        const obj = {}
        for (const child of el.children) {
          const val = child.children.length ? xmlToJson(child) : child.textContent
          obj[child.tagName] = val
        }
        return obj
      }
      jsonConvertOutput.value = JSON.stringify(xmlToJson(doc.documentElement), null, 2)
    } else if (jsonConvertTo.value === 'csv2json') {
      const lines = val.split('\n').filter(l => l.trim())
      if (lines.length < 2) { ElMessage.error('CSV至少需要标题行和一行数据'); return }
      const keys = lines[0].split(',').map(k => k.trim().replace(/^"|"$/g, ''))
      const result = lines.slice(1).map(line => {
        const vals = []; let inQuote = false, cur = ''
        for (const ch of line) {
          if (ch === '"') { inQuote = !inQuote }
          else if (ch === ',' && !inQuote) { vals.push(cur.trim().replace(/^"|"$/g, '')); cur = '' }
          else cur += ch
        }
        vals.push(cur.trim().replace(/^"|"$/g, ''))
        const obj = {}; keys.forEach((k, i) => { obj[k] = vals[i] || '' })
        return obj
      })
      jsonConvertOutput.value = JSON.stringify(result, null, 2)
    }
  } catch(e) { ElMessage.error('转换失败: ' + e.message) }
}

// ===== 加密解密 =====
const cryptoInput = ref(''); const cryptoAlgo = ref('MD5'); const cryptoResult = ref('')
async function doCrypto() {
  const text = cryptoInput.value
  if (!text) { ElMessage.error('请输入文本'); return }
  try {
    if (cryptoAlgo.value === 'MD5') {
      cryptoResult.value = md5(text)
    } else {
      const algo = cryptoAlgo.value.replace('-', '-')  // SHA-1 → SHA-1, SHA-256 → SHA-256, SHA-512 → SHA-512
      const encoder = new TextEncoder()
      const data = encoder.encode(text)
      const hashBuffer = await crypto.subtle.digest(algo, data)
      cryptoResult.value = Array.from(new Uint8Array(hashBuffer)).map(b => b.toString(16).padStart(2, '0')).join('')
    }
  } catch(e) { ElMessage.error('计算失败: ' + e.message) }
}
function md5(string) {
  function RotateLeft(lValue, iShiftBits) { return (lValue<<iShiftBits) | (lValue>>>(32-iShiftBits)) }
  function AddUnsigned(lX,lY) { const lX8=(lX&0x80000000), lY8=(lY&0x80000000), lX4=(lX&0x40000000), lY4=(lY&0x40000000), lResult=(lX&0x3FFFFFFF)+(lY&0x3FFFFFFF); if(lX4&lY4) return lResult^0x80000000^lX8^lY8; if(lX4|lY4){if(lResult&0x40000000) return lResult^0xC0000000^lX8^lY8; else return lResult^0x40000000^lX8^lY8} else return lResult^lX8^lY8 }
  function F(x,y,z) { return (x&y)|((~x)&z) }
  function G(x,y,z) { return (x&z)|(y&(~z)) }
  function H(x,y,z) { return (x^y^z) }
  function I(x,y,z) { return (y^(x|(~z))) }
  function FF(a,b,c,d,x,s,ac) { a=AddUnsigned(a,AddUnsigned(AddUnsigned(F(b,c,d),x),ac)); return AddUnsigned(RotateLeft(a,s),b) }
  function GG(a,b,c,d,x,s,ac) { a=AddUnsigned(a,AddUnsigned(AddUnsigned(G(b,c,d),x),ac)); return AddUnsigned(RotateLeft(a,s),b) }
  function HH(a,b,c,d,x,s,ac) { a=AddUnsigned(a,AddUnsigned(AddUnsigned(H(b,c,d),x),ac)); return AddUnsigned(RotateLeft(a,s),b) }
  function II(a,b,c,d,x,s,ac) { a=AddUnsigned(a,AddUnsigned(AddUnsigned(I(b,c,d),x),ac)); return AddUnsigned(RotateLeft(a,s),b) }
  function ConvertToWordArray(string) {
    let lWordCount, lMessageLength=string.length, lNumberOfWords_temp1=lMessageLength+8, lNumberOfWords_temp2=(lNumberOfWords_temp1-(lNumberOfWords_temp1%64))/64, lNumberOfWords=(lNumberOfWords_temp2+1)*16, lWordArray=Array(lNumberOfWords-1), lBytePosition=0, lByteCount=0
    while(lByteCount<lMessageLength){ lWordCount=(lByteCount-(lByteCount%4))/4; lBytePosition=(lByteCount%4)*8; lWordArray[lWordCount]=(lWordArray[lWordCount]|(string.charCodeAt(lByteCount)<<lBytePosition)); lByteCount++ }
    lWordCount=(lByteCount-(lByteCount%4))/4; lBytePosition=(lByteCount%4)*8; lWordArray[lWordCount]=lWordArray[lWordCount]|(0x80<<lBytePosition)
    lWordArray[lNumberOfWords-2]=lMessageLength<<3; lWordArray[lNumberOfWords-1]=lMessageLength>>>29
    return lWordArray
  }
  function WordToHex(lValue) { let WordToHexValue="", WordToHexValue_temp="", lByte, lCount; for(lCount=0;lCount<=3;lCount++){ lByte=(lValue>>>(lCount*8))&255; WordToHexValue_temp="0"+lByte.toString(16); WordToHexValue=WordToHexValue+WordToHexValue_temp.substr(WordToHexValue_temp.length-2,2) } return WordToHexValue }
  let x=ConvertToWordArray(string), a=0x67452301,b=0xEFCDAB89,c=0x98BADCFE,d=0x10325476
  for(let k=0;k<x.length;k+=16){ let AA=a,BB=b,CC=c,DD=d; a=FF(a,b,c,d,x[k+0],7,0xD76AA478); d=FF(d,a,b,c,x[k+1],12,0xE8C7B756); c=FF(c,d,a,b,x[k+2],17,0x242070DB); b=FF(b,c,d,a,x[k+3],22,0xC1BDCEEE); a=FF(a,b,c,d,x[k+4],7,0xF57C0FAF); d=FF(d,a,b,c,x[k+5],12,0x4787C62A); c=FF(c,d,a,b,x[k+6],17,0xA8304613); b=FF(b,c,d,a,x[k+7],22,0xFD469501); a=FF(a,b,c,d,x[k+8],7,0x698098D8); d=FF(d,a,b,c,x[k+9],12,0x8B44F7AF); c=FF(c,d,a,b,x[k+10],17,0xFFFF5BB1); b=FF(b,c,d,a,x[k+11],22,0x895CD7BE); a=FF(a,b,c,d,x[k+12],7,0x6B901122); d=FF(d,a,b,c,x[k+13],12,0xFD987193); c=FF(c,d,a,b,x[k+14],17,0xA679438E); b=FF(b,c,d,a,x[k+15],22,0x49B40821)
  a=GG(a,b,c,d,x[k+1],5,0xF61E2562); d=GG(d,a,b,c,x[k+6],9,0xC040B340); c=GG(c,d,a,b,x[k+11],14,0x265E5A51); b=GG(b,c,d,a,x[k+0],20,0xE9B6C7AA); a=GG(a,b,c,d,x[k+5],5,0xD62F105D); d=GG(d,a,b,c,x[k+10],9,0x2441453); c=GG(c,d,a,b,x[k+15],14,0xD8A1E681); b=GG(b,c,d,a,x[k+4],20,0xE7D3FBC8); a=GG(a,b,c,d,x[k+9],5,0x21E1CDE6); d=GG(d,a,b,c,x[k+14],9,0xC33707D6); c=GG(c,d,a,b,x[k+3],14,0xF4D50D87); b=GG(b,c,d,a,x[k+8],20,0x455A14ED); a=GG(a,b,c,d,x[k+13],5,0xA9E3E905); d=GG(d,a,b,c,x[k+7],9,0xFCEFA3F8); c=GG(c,d,a,b,x[k+2],14,0x676F02D9); b=GG(b,c,d,a,x[k+12],20,0x8D2A4C8A)
  a=HH(a,b,c,d,x[k+5],4,0xFFFA3942); d=HH(d,a,b,c,x[k+8],11,0x8771F681); c=HH(c,d,a,b,x[k+11],16,0x6D9D6122); b=HH(b,c,d,a,x[k+14],23,0xFDE5380C); a=HH(a,b,c,d,x[k+1],4,0xA4BEEA44); d=HH(d,a,b,c,x[k+4],11,0x4BDECFA9); c=HH(c,d,a,b,x[k+7],16,0xF6BB4B60); b=HH(b,c,d,a,x[k+10],23,0xBEBFBC70); a=HH(a,b,c,d,x[k+13],4,0x289B7EC6); d=HH(d,a,b,c,x[k+8],11,0xEAA127FA); c=HH(c,d,a,b,x[k+3],16,0xD4EF3085); b=HH(b,c,d,a,x[k+6],23,0x4881D05); a=HH(a,b,c,d,x[k+9],4,0xD9D4D039); d=HH(d,a,b,c,x[k+12],11,0xE6DB99E5); c=HH(c,d,a,b,x[k+15],16,0x1FA27CF8); b=HH(b,c,d,a,x[k+2],23,0xC4AC5665)
  a=II(a,b,c,d,x[k+0],6,0xF4292244); d=II(d,a,b,c,x[k+7],10,0x432AFF97); c=II(c,d,a,b,x[k+14],15,0xAB9423A7); b=II(b,c,d,a,x[k+5],21,0xFC93A039); a=II(a,b,c,d,x[k+12],6,0x655B59C3); d=II(d,a,b,c,x[k+3],10,0x8F0CCC92); c=II(c,d,a,b,x[k+10],15,0xFFEFF47D); b=II(b,c,d,a,x[k+1],21,0x85845DD1); a=II(a,b,c,d,x[k+8],6,0x6FA87E4F); d=II(d,a,b,c,x[k+14],10,0xFE2CE6E0); c=II(c,d,a,b,x[k+5],15,0xA3014314); b=II(b,c,d,a,x[k+12],21,0x4E0811A1); a=II(a,b,c,d,x[k+4],6,0xF7537E82); d=II(d,a,b,c,x[k+9],10,0xBD3AF235); c=II(c,d,a,b,x[k+15],15,0x2AD7D2BB); b=II(b,c,d,a,x[k+6],21,0xEB86D391)
  a=AddUnsigned(a,AA); b=AddUnsigned(b,BB); c=AddUnsigned(c,CC); d=AddUnsigned(d,DD) }
  return (WordToHex(a)+WordToHex(b)+WordToHex(c)+WordToHex(d)).toLowerCase()
}

// ===== JWT解析 =====
const jwtToken = ref(''); const jwtResult = ref(null)
function parseJwt() {
  const token = jwtToken.value.trim()
  if (!token) { jwtResult.value = null; return }
  const parts = token.split('.')
  if (parts.length !== 3) { jwtResult.value = { error: '无效的JWT格式（应为 header.payload.signature）' }; return }
  try {
    const b64Decode = (str) => {
      str = str.replace(/-/g, '+').replace(/_/g, '/')
      const padding = 4 - str.length % 4
      if (padding < 4) str += '='.repeat(padding)
      return decodeURIComponent(escape(atob(str)))
    }
    const header = JSON.parse(b64Decode(parts[0]))
    const payload = JSON.parse(b64Decode(parts[1]))
    const now = Math.floor(Date.now() / 1000)
    const result = {
      header: JSON.stringify(header, null, 2),
      payload: JSON.stringify(payload, null, 2),
      signature: parts[2].substring(0, 20) + '...',
      iat: payload.iat ? new Date(payload.iat * 1000).toLocaleString('zh-CN') : null,
      exp: payload.exp ? new Date(payload.exp * 1000).toLocaleString('zh-CN') : null,
      expired: payload.exp ? payload.exp < now : null,
    }
    jwtResult.value = result
  } catch(e) { jwtResult.value = { error: '解析失败: ' + e.message } }
}

// ===== HTTP请求测试 =====
const httpMethod = ref('GET'); const httpUrl = ref(''); const httpHeaders = ref(''); const httpBody = ref('')
const httpResponse = ref(null); const httpStatus = ref(0); const httpDuration = ref(0); const httpLoading = ref(false)
async function sendHttp() {
  if (!httpUrl.value.trim()) { ElMessage.error('请输入URL'); return }
  httpLoading.value = true; httpResponse.value = null
  const start = Date.now()
  try {
    let headers = {}
    if (httpHeaders.value.trim()) {
      try { headers = JSON.parse(httpHeaders.value) } catch(e) { /* keep as is */ }
    }
    const opts = { method: httpMethod.value, headers }
    if (httpMethod.value !== 'GET' && httpBody.value.trim()) {
      opts.body = httpBody.value
    }
    const res = await fetch(httpUrl.value, opts)
    httpStatus.value = res.status
    httpDuration.value = Date.now() - start
    const text = await res.text()
    try { httpResponse.value = JSON.stringify(JSON.parse(text), null, 2) } catch(e) { httpResponse.value = text }
  } catch(e) {
    httpStatus.value = 0; httpDuration.value = Date.now() - start
    httpResponse.value = `请求失败: ${e.message}`
  } finally { httpLoading.value = false }
}

// ===== IP地址查询 =====
const ipQueryAddr = ref(''); const ipResult = ref(null); const ipLoading = ref(false)
async function queryIp() {
  ipLoading.value = true; ipResult.value = null
  try {
    const url = ipQueryAddr.value.trim() ? `https://ipapi.co/${ipQueryAddr.value.trim()}/json/` : 'https://ipapi.co/json/'
    const res = await fetch(url)
    const data = await res.json()
    if (data.error) { ElMessage.error(data.reason || '查询失败'); ipLoading.value = false; return }
    ipResult.value = {
      'IP地址': data.ip,
      '版本': data.version || '-',
      '城市': data.city || '-',
      '地区': data.region || '-',
      '国家': data.country_name || '-',
      '运营商': data.org || '-',
      '邮编': data.postal || '-',
      '时区': data.timezone || '-',
    }
  } catch(e) { ElMessage.error('查询失败: ' + e.message) }
  finally { ipLoading.value = false }
}

// ===== HTML ↔ Markdown 互转 =====
const mdhtmlDir = ref('md2html'); const mdhtmlInput = ref(''); const mdhtmlOutput = ref('')
function convertMdhtml() {
  const input = mdhtmlInput.value
  if (!input) return
  if (mdhtmlDir.value === 'md2html') {
    let html = input
      .replace(/^### (.+)$/gm, '<h3>$1</h3>')
      .replace(/^## (.+)$/gm, '<h2>$1</h2>')
      .replace(/^# (.+)$/gm, '<h1>$1</h1>')
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.+?)\*/g, '<em>$1</em>')
      .replace(/`(.+?)`/g, '<code>$1</code>')
      .replace(/\[(.+?)\]\((.+?)\)/g, '<a href="$2">$1</a>')
      .replace(/!\[(.+?)\]\((.+?)\)/g, '<img src="$2" alt="$1">')
      .replace(/^\- (.+)$/gm, '<li>$1</li>')
      .replace(/\n{2,}/g, '</p><p>')
    html = '<p>' + html + '</p>'
    html = html.replace(/<li>.*?<\/li>(\s*<li>.*?<\/li>)*/gs, m => '<ul>' + m + '</ul>')
    mdhtmlOutput.value = html
  } else {
    let md = input
      .replace(/<h([1-3])>/gi, (_, n) => '#'.repeat(parseInt(n)) + ' ')
      .replace(/<\/h[1-3]>/gi, '\n')
      .replace(/<strong>(.+?)<\/strong>/gi, '**$1**')
      .replace(/<em>(.+?)<\/em>/gi, '*$1*')
      .replace(/<code>(.+?)<\/code>/gi, '`$1`')
      .replace(/<a\s+href=["'](.+?)["']>(.+?)<\/a>/gi, '[$2]($1)')
      .replace(/<li>(.+?)<\/li>/gi, '- $1\n')
      .replace(/<\/?[uo]l>/gi, '')
      .replace(/<br\s*\/?>/gi, '\n')
      .replace(/<\/?p>/gi, '\n')
      .replace(/<[^>]+>/g, '')
      .replace(/\n{3,}/g, '\n\n')
      .replace(/&amp;/g, '&')
      .replace(/&lt;/g, '<')
      .replace(/&gt;/g, '>')
      .replace(/&quot;/g, '"')
    mdhtmlOutput.value = md.trim()
  }
}

// ===== 时区转换 =====
const tzSourceTime = ref(''); const tzSourceZone = ref('Asia/Shanghai'); const tzTargetZone = ref('America/New_York'); const tzResult = ref('')
const timezoneList = [
  'Asia/Shanghai','Asia/Tokyo','Asia/Seoul','Asia/Singapore','Asia/Hong_Kong','Asia/Dubai','Asia/Kolkata',
  'Europe/London','Europe/Paris','Europe/Berlin','Europe/Moscow',
  'America/New_York','America/Chicago','America/Denver','America/Los_Angeles','America/Toronto','America/Sao_Paulo',
  'Pacific/Auckland','Pacific/Honolulu','Pacific/Fiji',
  'Australia/Sydney','Australia/Melbourne',
  'Africa/Cairo','Africa/Lagos','Africa/Johannesburg',
  'UTC'
]
function convertTimezone() {
  if (!tzSourceTime.value) { ElMessage.error('请选择时间'); return }
  try {
    const d = new Date(tzSourceTime.value)
    const sourceStr = d.toLocaleString('zh-CN', { timeZone: tzSourceZone.value, hour12: false })
    const targetStr = d.toLocaleString('zh-CN', { timeZone: tzTargetZone.value, hour12: false })
    tzResult.value = `${tzSourceZone.value}: ${sourceStr}\n→ ${tzTargetZone.value}: ${targetStr}`
  } catch(e) { ElMessage.error('转换失败: ' + e.message) }
}

// ===== Cron表达式生成器 =====
const cronParts = ref(['0', '*', '*', '*', '*', '?', '*'])
const cronExpr = ref('')
function syncCronExpr() {
  cronExpr.value = cronParts.value.join(' ')
  calcCronRuns()
}
function setCronQuick(expr) {
  cronParts.value = expr.split(' ')
  cronExpr.value = expr
  calcCronRuns()
}
const cronNextRuns = ref([])
function calcCronRuns() {
  // 简化版Cron解析，生成最近5次近似时间
  const now = new Date()
  const runs = []
  const parts = cronParts.value
  const sec = parts[0] === '*' ? 0 : parseInt(parts[0]) || 0
  const min = parts[1] === '*' ? now.getMinutes() : parseInt(parts[1]) || 0
  const hour = parts[2] === '*' ? now.getHours() : parseInt(parts[2]) || 0
  const day = parts[3] === '*' ? now.getDate() : parseInt(parts[3]) || 1
  const month = parts[4] === '*' ? now.getMonth() : (parseInt(parts[4]) || 1) - 1
  let base = new Date(now.getFullYear(), month, day, hour, min, sec)
  if (base <= now) {
    if (parts[2] === '*') base.setHours(base.getHours() + 1, min, sec)
    else if (parts[1] === '*') base.setMinutes(base.getMinutes() + 1, sec)
    else base.setSeconds(base.getSeconds() + 60)
  }
  const fmt = (d) => d.toLocaleString('zh-CN', { hour12: false })
  for (let i = 0; i < 5; i++) {
    runs.push(fmt(new Date(base)))
    if (parts[1] === '*') base.setMinutes(base.getMinutes() + 1)
    else if (parts[2] === '*') base.setHours(base.getHours() + 1)
    else base.setDate(base.getDate() + 1)
  }
  cronNextRuns.value = runs
}

// ===== 代码格式化 =====
const codeFmtLang = ref('js'); const codeFmtInput = ref(''); const codeFmtOutput = ref('')
function formatCode() {
  const code = codeFmtInput.value.trim()
  if (!code) { codeFmtOutput.value = ''; return }
  const lang = codeFmtLang.value
  try {
    if (lang === 'js') {
      codeFmtOutput.value = formatJS(code)
    } else if (lang === 'html') {
      codeFmtOutput.value = formatHTML(code)
    } else if (lang === 'css') {
      codeFmtOutput.value = formatCSS(code)
    } else if (lang === 'sql') {
      codeFmtOutput.value = formatSQL(code)
    }
  } catch(e) { codeFmtOutput.value = '格式化失败: ' + e.message }
}
function formatJS(code) {
  let indent = 0, result = ''
  const tokens = code.replace(/([{}();,])/g, ' $1 ').replace(/\s+/g, ' ').trim().split(' ')
  for (let token of tokens) {
    if (['{', '('].includes(token)) { result += token + '\n' + '  '.repeat(++indent) }
    else if (['}', ')'].includes(token)) { result += '\n' + '  '.repeat(--indent) + token }
    else if (token === ';') { result += ';\n' + '  '.repeat(indent) }
    else { result += (result.endsWith('\n') ? '  '.repeat(indent) : (result && !result.endsWith(' ') ? ' ' : '')) + token }
  }
  return result.replace(/\n\s*\n/g, '\n').trim()
}
function formatHTML(code) {
  let indent = 0, result = '', i = 0
  while (i < code.length) {
    if (code[i] === '<' && code[i+1] !== '/' && code[i+1] !== '!') {
      result += '\n' + '  '.repeat(indent) + '<'
      i++; let tag = ''
      while (i < code.length && code[i] !== '>' && !/\s/.test(code[i])) { tag += code[i]; i++ }
      result += tag
      const voidTags = ['br','hr','img','input','meta','link','area','base','col','embed','source','track','wbr']
      if (!voidTags.includes(tag.toLowerCase())) indent++
    } else if (code[i] === '<' && code[i+1] === '/') {
      indent = Math.max(0, indent - 1)
      result += '\n' + '  '.repeat(indent) + '</'
      i += 2; while (i < code.length && code[i] !== '>') { result += code[i]; i++ }
      result += '>'
    } else if (code[i] === '>') {
      result += '>'; i++
    } else {
      let chunk = ''
      while (i < code.length && code[i] !== '<') { chunk += code[i]; i++ }
      chunk = chunk.trim()
      if (chunk) result += (result.endsWith('>') ? '' : ' ') + chunk
    }
  }
  return result.replace(/^\n/, '').trim()
}
function formatCSS(code) {
  let result = code
    .replace(/\s*{\s*/g, ' {\n  ')
    .replace(/\s*;\s*/g, ';\n  ')
    .replace(/\s*}\s*/g, '\n}\n')
    .replace(/;\n\s*}/g, ';\n}')
    .replace(/\n{3,}/g, '\n\n')
  return result.trim()
}
function formatSQL(code) {
  const keywords = ['SELECT','FROM','WHERE','AND','OR','INSERT','INTO','VALUES','UPDATE','SET','DELETE',
    'CREATE','TABLE','ALTER','DROP','INDEX','JOIN','LEFT','RIGHT','INNER','OUTER','ON','GROUP','BY',
    'ORDER','ASC','DESC','LIMIT','OFFSET','HAVING','UNION','ALL','DISTINCT','AS','IN','NOT','NULL','IS',
    'LIKE','BETWEEN','EXISTS','CASE','WHEN','THEN','ELSE','END','COUNT','SUM','AVG','MAX','MIN']
  let result = code.replace(/\s+/g, ' ').trim()
  keywords.forEach(kw => {
    const re = new RegExp('\\b' + kw + '\\b', 'gi')
    result = result.replace(re, kw)
  })
  result = result.replace(/\b(SELECT|FROM|WHERE|AND|OR|ORDER BY|GROUP BY|HAVING|LIMIT|INSERT INTO|VALUES|UPDATE|SET|DELETE FROM|CREATE TABLE|ALTER TABLE|LEFT JOIN|RIGHT JOIN|INNER JOIN|ON)\b/gi, '\n$1')
  return result.replace(/^\n/, '').trim()
}

// ===== CSS渐变生成器 =====
const gradType = ref('linear'); const gradAngle = ref(90)
const gradColor1 = ref(COLORS.PRIMARY.toUpperCase()); const gradColor2 = ref(COLORS.SUCCESS.toUpperCase())
const gradCss = ref('')
function updateCssGradient() {
  if (gradType.value === 'linear') {
    gradCss.value = `background: linear-gradient(${gradAngle.value}deg, ${gradColor1.value}, ${gradColor2.value});`
  } else {
    gradCss.value = `background: radial-gradient(circle, ${gradColor1.value}, ${gradColor2.value});`
  }
}
updateCssGradient()

// ===== YML ↔ Properties 互转 =====
const ymlDir = ref('yml2prop'); const ymlInput = ref(''); const ymlOutput = ref('')
function convertYml() {
  const input = ymlInput.value
  if (!input.trim()) return
  try {
    if (ymlDir.value === 'yml2prop') {
      const lines = []
      function parseYml(text, prefix = '') {
        const arr = text.split('\n')
        let i = 0
        while (i < arr.length) {
          const line = arr[i]
          if (!line.trim()) { i++; continue }
          const m = line.match(/^(\s*)([\w.-]+):(\s*)(.*)/)
          if (!m) { i++; continue }
          const key = m[2], val = m[4].trim()
          const fullKey = prefix ? prefix + '.' + key : key
          if (!val) {
            // nested: collect indented lines
            let j = i + 1, nested = ''
            while (j < arr.length && (arr[j].trim() === '' || arr[j].match(/^\s{2,}/) || arr[j].startsWith('  ') || arr[j].startsWith('\t'))) {
              if (arr[j].trim()) nested += (nested ? '\n' : '') + arr[j]; j++
            }
            if (nested) parseYml(nested, fullKey)
            i = j; continue
          }
          lines.push(fullKey + '=' + val.replace(/^["']|["']$/g, ''))
          i++
        }
      }
      parseYml(input)
      ymlOutput.value = lines.join('\n')
    } else {
      const map = {}
      input.split('\n').forEach(line => {
        const m = line.trim().match(/^([\w.]+)=(.*)/)
        if (!m) return
        const keys = m[1].split('.'), val = m[2]
        let obj = map
        keys.forEach((k,i) => {
          if (i === keys.length - 1) obj[k] = val
          else { if (!obj[k]) obj[k] = {}; obj = obj[k] }
        })
      })
      function toYml(obj, indent = 0) {
        let result = ''
        const prefix = '  '.repeat(indent)
        for (const [k, v] of Object.entries(obj)) {
          if (typeof v === 'object' && v !== null) {
            result += `${prefix}${k}:\n${toYml(v, indent+1)}`
          } else {
            result += `${prefix}${k}: ${v}\n`
          }
        }
        return result
      }
      ymlOutput.value = toYml(map).trimEnd()
    }
  } catch(e) { ElMessage.error('转换失败: ' + e.message) }
}

// ===== 图像工具 =====

// 辅助函数：格式化文件大小
function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

// 辅助函数：文件转DataURL
function fileToDataURL(file) {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target.result)
    reader.readAsDataURL(file)
  })
}

// 辅助函数：下载图片
function downloadImg(dataUrl, name) {
  const a = document.createElement('a')
  a.href = dataUrl
  a.download = name + '.png'
  a.click()
}

// ----- 图片压缩 -----
const imgCompressFile = ref(null)
const imgCompressSrc = ref('')
const imgCompressedSrc = ref('')
const imgCompressQuality = ref(0.8)
const imgCompressMaxWidth = ref(1920)
const imgCompressMaxHeight = ref(1080)
const imgCompressOrigSize = ref('')
const imgCompressNewSize = ref('')

function onImgCompressUpload(file) {
  imgCompressFile.value = file.raw
  imgCompressedSrc.value = ''
  imgCompressNewSize.value = ''
  const reader = new FileReader()
  reader.onload = (e) => {
    imgCompressSrc.value = e.target.result
    imgCompressOrigSize.value = formatFileSize(file.raw.size)
  }
  reader.readAsDataURL(file.raw)
}

function compressImage() {
  if (!imgCompressSrc.value) return
  const img = new Image()
  img.src = imgCompressSrc.value
  img.onload = () => {
    let w = img.width, h = img.height
    if (w > imgCompressMaxWidth.value) {
      h = Math.round(h * imgCompressMaxWidth.value / w)
      w = imgCompressMaxWidth.value
    }
    if (h > imgCompressMaxHeight.value) {
      w = Math.round(w * imgCompressMaxHeight.value / h)
      h = imgCompressMaxHeight.value
    }
    const canvas = document.createElement('canvas')
    canvas.width = w; canvas.height = h
    const ctx = canvas.getContext('2d')
    ctx.drawImage(img, 0, 0, w, h)
    canvas.toBlob((blob) => {
      imgCompressedSrc.value = URL.createObjectURL(blob)
      imgCompressNewSize.value = formatFileSize(blob.size)
      ElMessage.success(`压缩完成: ${imgCompressOrigSize.value} → ${imgCompressNewSize.value}`)
    }, 'image/jpeg', imgCompressQuality.value)
  }
}

function resetImgCompress() {
  imgCompressSrc.value = ''
  imgCompressedSrc.value = ''
  imgCompressOrigSize.value = ''
  imgCompressNewSize.value = ''
}

// ----- 图片水印 -----
const wmSrc = ref('')
const wmResult = ref('')
const wmText = ref('水印文字')
const wmPosition = ref('bottomRight')
const wmFontSize = ref(36)
const wmColor = ref(COLORS.BG_WHITE)
const wmOpacity = ref(0.4)

function onWmUpload(file) {
  wmResult.value = ''
  const reader = new FileReader()
  reader.onload = (e) => { wmSrc.value = e.target.result }
  reader.readAsDataURL(file.raw)
}

function addWatermark() {
  if (!wmSrc.value || !wmText.value.trim()) { ElMessage.error('请上传图片并输入水印文字'); return }
  const img = new Image()
  img.src = wmSrc.value
  img.onload = () => {
    const canvas = document.createElement('canvas')
    canvas.width = img.width; canvas.height = img.height
    const ctx = canvas.getContext('2d')
    ctx.drawImage(img, 0, 0)
    ctx.font = `bold ${wmFontSize.value}px "Microsoft YaHei", sans-serif`
    ctx.fillStyle = wmColor.value
    ctx.globalAlpha = wmOpacity.value
    const text = wmText.value
    const tw = ctx.measureText(text).width
    const padding = 30
    const positions = {
      topLeft: [padding, wmFontSize.value + padding],
      topRight: [canvas.width - tw - padding, wmFontSize.value + padding],
      center: [(canvas.width - tw) / 2, canvas.height / 2],
      bottomLeft: [padding, canvas.height - padding],
      bottomRight: [canvas.width - tw - padding, canvas.height - padding],
    }
    if (wmPosition.value === 'tile') {
      ctx.save()
      ctx.translate(canvas.width / 2, canvas.height / 2)
      ctx.rotate(-30 * Math.PI / 180)
      const gap = wmFontSize.value * 4
      for (let y = -canvas.height; y < canvas.height * 2; y += gap) {
        for (let x = -canvas.width; x < canvas.width * 2; x += tw * 2 + 40) {
          ctx.fillText(text, x, y)
        }
      }
      ctx.restore()
    } else {
      const [x, y] = positions[wmPosition.value]
      ctx.fillText(text, x, y)
    }
    wmResult.value = canvas.toDataURL('image/png')
    ElMessage.success('水印生成成功')
  }
}

function resetWm() { wmSrc.value = ''; wmResult.value = '' }

// ----- 图片转ICO -----
const icoSrc = ref('')
const icoSizes = ref([16, 32, 48, 256])
const icoPreviews = ref([])
const icoResultSrc = ref('')
const icoBlob = ref(null)

function onIcoUpload(file) {
  icoPreviews.value = []
  icoResultSrc.value = ''
  icoBlob.value = null
  const reader = new FileReader()
  reader.onload = (e) => { icoSrc.value = e.target.result }
  reader.readAsDataURL(file.raw)
}

function convertToIco() {
  if (!icoSrc.value || !icoSizes.value.length) { ElMessage.error('请上传图片并选择尺寸'); return }
  const img = new Image()
  img.src = icoSrc.value
  img.onload = () => {
    const previews = []
    icoPreviews.value = []
    const size = Math.max(...icoSizes.value)
    // Generate ICO: resize to each size and build ICO file
    const entries = []
    const bmpDatas = []
    icoSizes.value.sort((a, b) => b - a).forEach(s => {
      const canvas = document.createElement('canvas')
      canvas.width = s; canvas.height = s
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, s, s)
      previews.push({ size: s, src: canvas.toDataURL() })
      const bmpData = canvasToBMPData(canvas, s, s)
      bmpDatas.push(bmpData)
      // ICO dir entry
      const entry = new ArrayBuffer(16)
      const dv = new DataView(entry)
      dv.setUint8(0, s >= 256 ? 0 : s)
      dv.setUint8(1, s >= 256 ? 0 : s)
      dv.setUint8(2, 0); dv.setUint8(3, 0)
      dv.setUint16(4, 1, true)
      dv.setUint16(6, 32, true)
      dv.setUint32(8, bmpData.byteLength, true)
      entries.push({ entry, bmpData })
    })
    icoPreviews.value = previews
    // Build ICO header
    const header = new ArrayBuffer(6)
    const hv = new DataView(header)
    hv.setUint16(0, 0, true)
    hv.setUint16(2, 1, true)
    hv.setUint16(4, entries.length, true)
    // Calculate offsets
    let offset = 6 + entries.length * 16
    const parts = [new Uint8Array(header)]
    for (let i = 0; i < entries.length; i++) {
      const { entry, bmpData } = entries[i]
      const dv = new DataView(entry)
      dv.setUint32(12, offset, true)
      offset += bmpData.byteLength
      parts.push(new Uint8Array(entry))
    }
    for (const { bmpData } of entries) {
      parts.push(new Uint8Array(bmpData))
    }
    const totalLen = parts.reduce((a, b) => a + b.length, 0)
    const icoFile = new Uint8Array(totalLen)
    let pos = 0
    for (const p of parts) { icoFile.set(p, pos); pos += p.length }
    icoBlob.value = new Blob([icoFile], { type: 'image/x-icon' })
    icoResultSrc.value = URL.createObjectURL(icoBlob.value)
    ElMessage.success(`生成ICO成功，包含 ${entries.length} 种尺寸`)
  }
}

function canvasToBMPData(canvas, w, h) {
  const ctx = canvas.getContext('2d')
  const imageData = ctx.getImageData(0, 0, w, h)
  const pixels = imageData.data
  const bmpHeaderSize = 40
  const pixelDataSize = w * h * 4
  const andMaskRowSize = Math.ceil(w / 32) * 4
  const andMaskSize = andMaskRowSize * h
  const totalSize = bmpHeaderSize + pixelDataSize + andMaskSize
  const buf = new ArrayBuffer(totalSize)
  const dv = new DataView(buf)
  dv.setUint32(0, 40, true)
  dv.setInt32(4, w, true)
  dv.setInt32(8, h * 2, true)
  dv.setUint16(12, 1, true)
  dv.setUint16(14, 32, true)
  // Pixel data: BGRA, bottom-up
  const pxView = new Uint8Array(buf, bmpHeaderSize, pixelDataSize)
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const srcIdx = ((h - 1 - y) * w + x) * 4
      const dstIdx = (y * w + x) * 4
      pxView[dstIdx] = pixels[srcIdx + 2]
      pxView[dstIdx + 1] = pixels[srcIdx + 1]
      pxView[dstIdx + 2] = pixels[srcIdx]
      pxView[dstIdx + 3] = pixels[srcIdx + 3]
    }
  }
  return buf
}

function downloadIco() {
  if (!icoBlob.value) return
  const url = URL.createObjectURL(icoBlob.value)
  const a = document.createElement('a')
  a.href = url; a.download = 'favicon.ico'; a.click()
  URL.revokeObjectURL(url)
}

function resetIco() { icoSrc.value = ''; icoPreviews.value = []; icoResultSrc.value = ''; icoBlob.value = null }

// ----- Base64图片转换 -----
const b64ImgResult = ref('')
const b64DecodeInput = ref('')
const b64DecodedSrc = ref('')

function onB64ImageUpload(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    b64ImgResult.value = e.target.result
    ElMessage.success('转换完成')
  }
  reader.readAsDataURL(file.raw)
}

function decodeB64Image() {
  const input = b64DecodeInput.value.trim()
  if (!input) { ElMessage.error('请粘贴Base64编码'); return }
  let base64 = input
  // Strip data:image/... prefix if present
  const match = base64.match(/^data:image\/\w+;base64,(.+)$/i)
  if (match) base64 = match[1]
  try {
    b64DecodedSrc.value = 'data:image/png;base64,' + base64
    // Validate
    const img = new Image()
    img.onload = () => { ElMessage.success('解码成功') }
    img.onerror = () => { ElMessage.error('Base64编码无效'); b64DecodedSrc.value = '' }
    img.src = b64DecodedSrc.value
  } catch(e) {
    ElMessage.error('解码失败: ' + e.message)
    b64DecodedSrc.value = ''
  }
}

// ----- 图标设计器 -----
const iconDesignCanvas = ref(null)
const iconDesignShape = ref('circle')
const iconDesignBg = ref(COLORS.PRIMARY)
const iconDesignFg = ref('#FFFFFF')
const iconDesignText = ref('A')
const iconDesignFontSize = ref(80)
const iconDesignSize = ref(256)

function renderIconDesign() {
  nextTick(() => {
    const canvas = iconDesignCanvas.value
    if (!canvas) return
    const size = iconDesignSize.value
    canvas.width = size; canvas.height = size
    const ctx = canvas.getContext('2d')
    ctx.clearRect(0, 0, size, size)
    // Background shape
    ctx.fillStyle = iconDesignBg.value
    ctx.beginPath()
    const margin = 4
    if (iconDesignShape.value === 'circle') {
      ctx.arc(size / 2, size / 2, size / 2 - margin, 0, Math.PI * 2)
    } else if (iconDesignShape.value === 'rounded') {
      const r = size * 0.18
      ctx.moveTo(r + margin, margin)
      ctx.lineTo(size - r - margin, margin)
      ctx.quadraticCurveTo(size - margin, margin, size - margin, r + margin)
      ctx.lineTo(size - margin, size - r - margin)
      ctx.quadraticCurveTo(size - margin, size - margin, size - r - margin, size - margin)
      ctx.lineTo(r + margin, size - margin)
      ctx.quadraticCurveTo(margin, size - margin, margin, size - r - margin)
      ctx.lineTo(margin, r + margin)
      ctx.quadraticCurveTo(margin, margin, r + margin, margin)
      ctx.closePath()
    } else {
      ctx.rect(margin, margin, size - margin * 2, size - margin * 2)
    }
    ctx.fill()
    // Text
    ctx.fillStyle = iconDesignFg.value
    ctx.font = `bold ${iconDesignFontSize.value}px "Microsoft YaHei", sans-serif`
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(iconDesignText.value, size / 2, size / 2)
  })
}

// Watch for initial render
watch(activeTool, (val) => {
  if (val === 'icondesign') renderIconDesign()
})

function downloadIconDesign() {
  const canvas = iconDesignCanvas.value
  if (!canvas) return
  const dataUrl = canvas.toDataURL('image/png')
  downloadImg(dataUrl, 'icon')
  ElMessage.success('图标已下载')
}

// ===== Emoji选择器 =====
const emojiStore = useStorage(STORAGE_KEYS.EMOJI_RECENT)
const emojiSearch = ref('')
const emojiCat = ref('all')
const emojiRecent = ref((() => { const d = emojiStore.get(); return Array.isArray(d) ? d.slice(0, 18) : [] })())

const emojiCategories = [
  { key: 'all', label: '全部' },
  { key: 'face', label: '😀 表情' },
  { key: 'gesture', label: '🖐 手势' },
  { key: 'animal', label: '🐱 动物' },
  { key: 'food', label: '🍎 食物' },
  { key: 'nature', label: '🌿 自然' },
  { key: 'activity', label: '🏀 运动' },
  { key: 'object', label: '📦 物品' },
  { key: 'symbol', label: '❤️ 符号' },
  { key: 'flag', label: '🚩 旗帜' },
]

const emojiData = ref([
  // 表情
  { c: '😀', n: '哈哈', g: 'face' }, { c: '😃', n: '大笑', g: 'face' }, { c: '😄', n: '开心', g: 'face' }, { c: '😁', n: '露齿笑', g: 'face' },
  { c: '😅', n: '尴尬', g: 'face' }, { c: '🤣', n: '笑哭', g: 'face' }, { c: '😂', n: '笑哭了', g: 'face' }, { c: '🙂', n: '微笑', g: 'face' },
  { c: '😊', n: '羞涩', g: 'face' }, { c: '😇', n: '天使', g: 'face' }, { c: '😍', n: '花痴', g: 'face' }, { c: '🤩', n: '星星眼', g: 'face' },
  { c: '😘', n: '飞吻', g: 'face' }, { c: '😗', n: '亲吻', g: 'face' }, { c: '😋', n: '好吃', g: 'face' }, { c: '😛', n: '吐舌', g: 'face' },
  { c: '😜', n: '眨眼吐舌', g: 'face' }, { c: '🤪', n: '搞怪', g: 'face' }, { c: '🤨', n: '挑眉', g: 'face' }, { c: '🧐', n: '学霸', g: 'face' },
  { c: '🤓', n: '书呆子', g: 'face' }, { c: '😎', n: '酷', g: 'face' }, { c: '🤗', n: '拥抱', g: 'face' }, { c: '🤭', n: '偷笑', g: 'face' },
  { c: '🫣', n: '偷看', g: 'face' }, { c: '🤫', n: '嘘', g: 'face' }, { c: '🤔', n: '思考', g: 'face' }, { c: '🫡', n: '敬礼', g: 'face' },
  { c: '🤐', n: '拉链嘴', g: 'face' }, { c: '😐', n: '面无表情', g: 'face' }, { c: '😑', n: '无语', g: 'face' }, { c: '😶', n: '沉默', g: 'face' },
  { c: '😏', n: '得意', g: 'face' }, { c: '😒', n: '不爽', g: 'face' }, { c: '🙄', n: '翻白眼', g: 'face' }, { c: '😬', n: '咬唇', g: 'face' },
  { c: '😮‍💨', n: '叹气', g: 'face' }, { c: '🤥', n: '说谎', g: 'face' }, { c: '😪', n: '困', g: 'face' }, { c: '😮', n: '惊讶', g: 'face' },
  { c: '😲', n: '震惊', g: 'face' }, { c: '😯', n: '沉默惊讶', g: 'face' }, { c: '😦', n: '皱眉', g: 'face' }, { c: '😧', n: '苦恼', g: 'face' },
  { c: '😨', n: '害怕', g: 'face' }, { c: '😰', n: '紧张', g: 'face' }, { c: '😥', n: '松口气', g: 'face' }, { c: '😢', n: '哭泣', g: 'face' },
  { c: '😭', n: '大哭', g: 'face' }, { c: '😱', n: '恐惧', g: 'face' }, { c: '😖', n: '困惑', g: 'face' }, { c: '😣', n: '忍耐', g: 'face' },
  { c: '😞', n: '失望', g: 'face' }, { c: '😓', n: '冷汗', g: 'face' }, { c: '😩', n: '累', g: 'face' }, { c: '😫', n: '崩溃', g: 'face' },
  { c: '🥱', n: '打哈欠', g: 'face' }, { c: '😤', n: '生气', g: 'face' }, { c: '😡', n: '愤怒', g: 'face' }, { c: '🤬', n: '骂人', g: 'face' },
  { c: '🤯', n: '爆炸头', g: 'face' }, { c: '😳', n: '脸红', g: 'face' }, { c: '🥵', n: '热', g: 'face' }, { c: '🥶', n: '冷', g: 'face' },
  { c: '😱', n: '尖叫', g: 'face' }, { c: '🥴', n: '晕', g: 'face' }, { c: '🤢', n: '想吐', g: 'face' }, { c: '🤮', n: '呕吐', g: 'face' },
  { c: '🤧', n: '打喷嚏', g: 'face' }, { c: '🥳', n: '派对', g: 'face' }, { c: '🥺', n: '委屈', g: 'face' }, { c: '🤠', n: '牛仔', g: 'face' },
  { c: '🫠', n: '融化', g: 'face' }, { c: '😴', n: '睡觉', g: 'face' }, { c: '🤤', n: '流口水', g: 'face' }, { c: '👿', n: '恶魔', g: 'face' },
  { c: '💀', n: '骷髅', g: 'face' }, { c: '👻', n: '幽灵', g: 'face' }, { c: '👽', n: '外星人', g: 'face' }, { c: '🤖', n: '机器人', g: 'face' },
  { c: '🐵', n: '猴脸', g: 'face' }, { c: '👹', n: '鬼怪', g: 'face' }, { c: '👺', n: '天狗', g: 'face' }, { c: '💩', n: '便便', g: 'face' },
  // 手势
  { c: '👍', n: '赞', g: 'gesture' }, { c: '👎', n: '踩', g: 'gesture' }, { c: '👏', n: '鼓掌', g: 'gesture' }, { c: '🙌', n: '举手庆祝', g: 'gesture' },
  { c: '👐', n: '张开手', g: 'gesture' }, { c: '🤲', n: '祈祷', g: 'gesture' }, { c: '🤝', n: '握手', g: 'gesture' }, { c: '🙏', n: '合十', g: 'gesture' },
  { c: '✊', n: '拳', g: 'gesture' }, { c: '👊', n: '拳击', g: 'gesture' }, { c: '🤛', n: '左拳', g: 'gesture' }, { c: '🤜', n: '右拳', g: 'gesture' },
  { c: '👋', n: '挥手', g: 'gesture' }, { c: '🖐', n: '手掌', g: 'gesture' }, { c: '✋', n: '举手', g: 'gesture' }, { c: '🖖', n: '瓦肯举手', g: 'gesture' },
  { c: '🤟', n: '我爱你', g: 'gesture' }, { c: '🤘', n: '摇滚', g: 'gesture' }, { c: '🤙', n: '打电话', g: 'gesture' }, { c: '🫰', n: '钱', g: 'gesture' },
  { c: '👈', n: '左指', g: 'gesture' }, { c: '👉', n: '右指', g: 'gesture' }, { c: '👆', n: '上指', g: 'gesture' }, { c: '👇', n: '下指', g: 'gesture' },
  { c: '☝️', n: '食指', g: 'gesture' }, { c: '🖕', n: '中指', g: 'gesture' }, { c: '✌️', n: 'V手势', g: 'gesture' }, { c: '🤞', n: '祈好运', g: 'gesture' },
  { c: '🫵', n: '指向你', g: 'gesture' }, { c: '🤏', n: '捏', g: 'gesture' }, { c: '🤌', n: '意大利手', g: 'gesture' }, { c: '💪', n: '肌肉', g: 'gesture' },
  // 动物
  { c: '🐶', n: '狗', g: 'animal' }, { c: '🐱', n: '猫', g: 'animal' }, { c: '🐭', n: '鼠', g: 'animal' }, { c: '🐹', n: '仓鼠', g: 'animal' },
  { c: '🐰', n: '兔', g: 'animal' }, { c: '🦊', n: '狐狸', g: 'animal' }, { c: '🐻', n: '熊', g: 'animal' }, { c: '🐼', n: '熊猫', g: 'animal' },
  { c: '🐨', n: '考拉', g: 'animal' }, { c: '🐯', n: '虎', g: 'animal' }, { c: '🦁', n: '狮子', g: 'animal' }, { c: '🐮', n: '牛', g: 'animal' },
  { c: '🐷', n: '猪', g: 'animal' }, { c: '🐸', n: '青蛙', g: 'animal' }, { c: '🐵', n: '猴', g: 'animal' }, { c: '🐔', n: '鸡', g: 'animal' },
  { c: '🐧', n: '企鹅', g: 'animal' }, { c: '🐦', n: '鸟', g: 'animal' }, { c: '🐤', n: '小鸡', g: 'animal' }, { c: '🦆', n: '鸭子', g: 'animal' },
  { c: '🦉', n: '猫头鹰', g: 'animal' }, { c: '🦅', n: '鹰', g: 'animal' }, { c: '🦇', n: '蝙蝠', g: 'animal' }, { c: '🐺', n: '狼', g: 'animal' },
  { c: '🐗', n: '野猪', g: 'animal' }, { c: '🐴', n: '马', g: 'animal' }, { c: '🦄', n: '独角兽', g: 'animal' }, { c: '🐝', n: '蜜蜂', g: 'animal' },
  { c: '🐛', n: '毛虫', g: 'animal' }, { c: '🦋', n: '蝴蝶', g: 'animal' }, { c: '🐌', n: '蜗牛', g: 'animal' }, { c: '🐞', n: '瓢虫', g: 'animal' },
  { c: '🐜', n: '蚂蚁', g: 'animal' }, { c: '🐢', n: '乌龟', g: 'animal' }, { c: '🐍', n: '蛇', g: 'animal' }, { c: '🦎', n: '蜥蜴', g: 'animal' },
  { c: '🐙', n: '章鱼', g: 'animal' }, { c: '🐠', n: '热带鱼', g: 'animal' }, { c: '🐳', n: '鲸鱼', g: 'animal' }, { c: '🐬', n: '海豚', g: 'animal' },
  { c: '🦈', n: '鲨鱼', g: 'animal' }, { c: '🐊', n: '鳄鱼', g: 'animal' }, { c: '🦭', n: '海豹', g: 'animal' }, { c: '🐾', n: '爪印', g: 'animal' },
  // 食物
  { c: '🍎', n: '苹果', g: 'food' }, { c: '🍐', n: '梨', g: 'food' }, { c: '🍊', n: '橘子', g: 'food' }, { c: '🍋', n: '柠檬', g: 'food' },
  { c: '🍌', n: '香蕉', g: 'food' }, { c: '🍉', n: '西瓜', g: 'food' }, { c: '🍇', n: '葡萄', g: 'food' }, { c: '🍓', n: '草莓', g: 'food' },
  { c: '🍒', n: '樱桃', g: 'food' }, { c: '🍑', n: '桃子', g: 'food' }, { c: '🥭', n: '芒果', g: 'food' }, { c: '🍍', n: '菠萝', g: 'food' },
  { c: '🥝', n: '猕猴桃', g: 'food' }, { c: '🥑', n: '牛油果', g: 'food' }, { c: '🌽', n: '玉米', g: 'food' }, { c: '🥕', n: '胡萝卜', g: 'food' },
  { c: '🧅', n: '洋葱', g: 'food' }, { c: '🍄', n: '蘑菇', g: 'food' }, { c: '🍅', n: '番茄', g: 'food' }, { c: '🥒', n: '黄瓜', g: 'food' },
  { c: '🍞', n: '面包', g: 'food' }, { c: '🥐', n: '牛角包', g: 'food' }, { c: '🍔', n: '汉堡', g: 'food' }, { c: '🍟', n: '薯条', g: 'food' },
  { c: '🍕', n: '披萨', g: 'food' }, { c: '🌭', n: '热狗', g: 'food' }, { c: '🍿', n: '爆米花', g: 'food' }, { c: '🧇', n: '华夫饼', g: 'food' },
  { c: '🥞', n: '薄饼', g: 'food' }, { c: '🍳', n: '煎蛋', g: 'food' }, { c: '🥓', n: '培根', g: 'food' }, { c: '🍗', n: '鸡腿', g: 'food' },
  { c: '🍖', n: '肉', g: 'food' }, { c: '🍣', n: '寿司', g: 'food' }, { c: '🍤', n: '炸虾', g: 'food' }, { c: '🍜', n: '拉面', g: 'food' },
  { c: '🍰', n: '蛋糕', g: 'food' }, { c: '🍩', n: '甜甜圈', g: 'food' }, { c: '🍪', n: '饼干', g: 'food' }, { c: '🍫', n: '巧克力', g: 'food' },
  { c: '🍦', n: '冰淇淋', g: 'food' }, { c: '🍺', n: '啤酒', g: 'food' }, { c: '🍷', n: '红酒', g: 'food' }, { c: '🍵', n: '茶', g: 'food' },
  { c: '☕', n: '咖啡', g: 'food' }, { c: '🧋', n: '奶茶', g: 'food' }, { c: '🥤', n: '饮料', g: 'food' }, { c: '🧊', n: '冰块', g: 'food' },
  // 自然
  { c: '🌞', n: '太阳', g: 'nature' }, { c: '🌝', n: '满月', g: 'nature' }, { c: '🌚', n: '新月', g: 'nature' }, { c: '⭐', n: '星星', g: 'nature' },
  { c: '🌟', n: '闪耀星', g: 'nature' }, { c: '✨', n: '闪光', g: 'nature' }, { c: '🔥', n: '火焰', g: 'nature' }, { c: '💧', n: '水滴', g: 'nature' },
  { c: '🌈', n: '彩虹', g: 'nature' }, { c: '☁️', n: '云', g: 'nature' }, { c: '⛅', n: '多云', g: 'nature' }, { c: '🌧', n: '下雨', g: 'nature' },
  { c: '⛈️', n: '暴风雨', g: 'nature' }, { c: '🌩', n: '闪电', g: 'nature' }, { c: '❄️', n: '雪花', g: 'nature' }, { c: '☃️', n: '雪人', g: 'nature' },
  { c: '🌸', n: '樱花', g: 'nature' }, { c: '🌺', n: '扶桑花', g: 'nature' }, { c: '🌻', n: '向日葵', g: 'nature' }, { c: '🌹', n: '玫瑰', g: 'nature' },
  { c: '🌷', n: '郁金香', g: 'nature' }, { c: '💐', n: '花束', g: 'nature' }, { c: '🌿', n: '药草', g: 'nature' }, { c: '🍀', n: '四叶草', g: 'nature' },
  { c: '🍂', n: '落叶', g: 'nature' }, { c: '🍃', n: '风吹叶', g: 'nature' }, { c: '🌍', n: '地球', g: 'nature' }, { c: '🌑', n: '新月', g: 'nature' },
  // 运动/活动
  { c: '⚽', n: '足球', g: 'activity' }, { c: '🏀', n: '篮球', g: 'activity' }, { c: '🏈', n: '橄榄球', g: 'activity' }, { c: '⚾', n: '棒球', g: 'activity' },
  { c: '🎾', n: '网球', g: 'activity' }, { c: '🏐', n: '排球', g: 'activity' }, { c: '🏓', n: '乒乓球', g: 'activity' }, { c: '🏸', n: '羽毛球', g: 'activity' },
  { c: '🎱', n: '台球', g: 'activity' }, { c: '🥊', n: '拳击手套', g: 'activity' }, { c: '🎯', n: '靶心', g: 'activity' }, { c: '🎮', n: '手柄', g: 'activity' },
  { c: '🎲', n: '骰子', g: 'activity' }, { c: '♟️', n: '国际象棋', g: 'activity' }, { c: '🎵', n: '音符', g: 'activity' }, { c: '🎶', n: '旋律', g: 'activity' },
  { c: '🎤', n: '麦克风', g: 'activity' }, { c: '🎧', n: '耳机', g: 'activity' }, { c: '🎬', n: '场记板', g: 'activity' }, { c: '🎨', n: '调色板', g: 'activity' },
  { c: '🎭', n: '面具', g: 'activity' }, { c: '🎪', n: '马戏团', g: 'activity' }, { c: '🏆', n: '奖杯', g: 'activity' }, { c: '🥇', n: '金牌', g: 'activity' },
  { c: '🎖', n: '勋章', g: 'activity' }, { c: '🏅', n: '奖牌', g: 'activity' }, { c: '🚴', n: '骑车', g: 'activity' }, { c: '🏊', n: '游泳', g: 'activity' },
  { c: '🧘', n: '瑜伽', g: 'activity' }, { c: '🎳', n: '保龄球', g: 'activity' },
  // 物品
  { c: '📱', n: '手机', g: 'object' }, { c: '💻', n: '笔记本', g: 'object' }, { c: '🖥', n: '台式电脑', g: 'object' }, { c: '⌨️', n: '键盘', g: 'object' },
  { c: '🖱', n: '鼠标', g: 'object' }, { c: '🖨', n: '打印机', g: 'object' }, { c: '📷', n: '相机', g: 'object' }, { c: '🎥', n: '摄影机', g: 'object' },
  { c: '📺', n: '电视', g: 'object' }, { c: '🔦', n: '手电筒', g: 'object' }, { c: '💡', n: '灯泡', g: 'object' }, { c: '🔌', n: '插头', g: 'object' },
  { c: '📖', n: '书', g: 'object' }, { c: '📚', n: '书堆', g: 'object' }, { c: '📝', n: '备忘录', g: 'object' }, { c: '✏️', n: '铅笔', g: 'object' },
  { c: '📌', n: '图钉', g: 'object' }, { c: '📍', n: '圆图钉', g: 'object' }, { c: '🔗', n: '链接', g: 'object' }, { c: '📎', n: '回形针', g: 'object' },
  { c: '✂️', n: '剪刀', g: 'object' }, { c: '📏', n: '直尺', g: 'object' }, { c: '🔒', n: '锁', g: 'object' }, { c: '🔓', n: '开锁', g: 'object' },
  { c: '🔑', n: '钥匙', g: 'object' }, { c: '🧲', n: '磁铁', g: 'object' }, { c: '🧰', n: '工具箱', g: 'object' }, { c: '📦', n: '箱子', g: 'object' },
  { c: '💰', n: '钱袋', g: 'object' }, { c: '💳', n: '信用卡', g: 'object' }, { c: '💎', n: '钻石', g: 'object' }, { c: '🔔', n: '铃铛', g: 'object' },
  { c: '🧨', n: '鞭炮', g: 'object' }, { c: '🎈', n: '气球', g: 'object' }, { c: '🎉', n: '礼花', g: 'object' }, { c: '🎀', n: '丝带', g: 'object' },
  // 符号
  { c: '❤️', n: '红心', g: 'symbol' }, { c: '🧡', n: '橙心', g: 'symbol' }, { c: '💛', n: '黄心', g: 'symbol' }, { c: '💚', n: '绿心', g: 'symbol' },
  { c: '💙', n: '蓝心', g: 'symbol' }, { c: '💜', n: '紫心', g: 'symbol' }, { c: '🖤', n: '黑心', g: 'symbol' }, { c: '🤍', n: '白心', g: 'symbol' },
  { c: '💔', n: '心碎', g: 'symbol' }, { c: '💕', n: '两颗心', g: 'symbol' }, { c: '💞', n: '旋转心', g: 'symbol' }, { c: '💓', n: '心跳', g: 'symbol' },
  { c: '❣️', n: '感叹心', g: 'symbol' }, { c: '💯', n: '满分', g: 'symbol' }, { c: '💢', n: '怒火', g: 'symbol' }, { c: '💥', n: '爆炸', g: 'symbol' },
  { c: '💫', n: '晕眩', g: 'symbol' }, { c: '💦', n: '汗', g: 'symbol' }, { c: '💨', n: '快速', g: 'symbol' }, { c: '🕳', n: '洞', g: 'symbol' },
  { c: '💬', n: '对话', g: 'symbol' }, { c: '🗨', n: '左对话', g: 'symbol' }, { c: '🗯', n: '怒对话', g: 'symbol' }, { c: '💭', n: '思想', g: 'symbol' },
  { c: '❗', n: '感叹号', g: 'symbol' }, { c: '❓', n: '问号', g: 'symbol' }, { c: '⁉️', n: '叹+问', g: 'symbol' }, { c: '🛑', n: '停止', g: 'symbol' },
  { c: '⚠️', n: '警告', g: 'symbol' }, { c: '🚸', n: '儿童过路', g: 'symbol' }, { c: '♻️', n: '回收', g: 'symbol' }, { c: '✅', n: '对勾', g: 'symbol' },
  { c: '❌', n: '叉号', g: 'symbol' }, { c: '©️', n: '版权', g: 'symbol' }, { c: '™️', n: '商标', g: 'symbol' }, { c: '®️', n: '注册', g: 'symbol' },
  { c: '🫶', n: '心形手', g: 'symbol' }, { c: '🤎', n: '棕心', g: 'symbol' },
  // 旗帜
  { c: '🚩', n: '红旗', g: 'flag' }, { c: '🏁', n: '方格旗', g: 'flag' }, { c: '🎌', n: '交叉旗', g: 'flag' }, { c: '🏴', n: '黑旗', g: 'flag' },
  { c: '🏳️', n: '白旗', g: 'flag' }, { c: '🏳️‍🌈', n: '彩虹旗', g: 'flag' }, { c: '🇨🇳', n: '中国', g: 'flag' }, { c: '🇺🇸', n: '美国', g: 'flag' },
  { c: '🇯🇵', n: '日本', g: 'flag' }, { c: '🇰🇷', n: '韩国', g: 'flag' }, { c: '🇬🇧', n: '英国', g: 'flag' }, { c: '🇫🇷', n: '法国', g: 'flag' },
  { c: '🇩🇪', n: '德国', g: 'flag' }, { c: '🇷🇺', n: '俄罗斯', g: 'flag' }, { c: '🇦🇺', n: '澳大利亚', g: 'flag' }, { c: '🇨🇦', n: '加拿大', g: 'flag' },
  { c: '🇧🇷', n: '巴西', g: 'flag' }, { c: '🇮🇳', n: '印度', g: 'flag' }, { c: '🇸🇬', n: '新加坡', g: 'flag' }, { c: '🇹🇭', n: '泰国', g: 'flag' },
])

const filteredEmojis = ref(emojiData.value)

function clearEmojiSearch() { emojiSearch.value = '' }

watch([emojiSearch, emojiCat], () => {
  let list = emojiData.value
  if (emojiCat.value !== 'all') list = list.filter(e => e.g === emojiCat.value)
  if (emojiSearch.value.trim()) {
    const q = emojiSearch.value.trim().toLowerCase()
    list = list.filter(e => e.n.toLowerCase().includes(q) || e.c.includes(q))
  }
  filteredEmojis.value = list
})

function copyEmoji(emoji) {
  navigator.clipboard.writeText(emoji).then(() => {
    ElMessage.success(`已复制: ${emoji}`)
    // Update recent
    let recent = [...new Set([emoji, ...emojiRecent.value])]
    if (recent.length > 18) recent = recent.slice(0, 18)
    emojiRecent.value = recent
    emojiStore.set(recent)
  }).catch(() => {
    // Fallback for older browsers
    const textarea = document.createElement('textarea')
    textarea.value = emoji; textarea.style.position = 'fixed'; textarea.style.opacity = '0'
    document.body.appendChild(textarea); textarea.select()
    document.execCommand('copy'); document.body.removeChild(textarea)
    ElMessage.success(`已复制: ${emoji}`)
  })
}
</script>

<style scoped>
.devtools-container {
  display: flex;
  height: 100%;
  gap: 0;
}

/* 左侧导航 */
.devtools-sidebar {
  width: 220px;
  flex-shrink: 0;
  border-right: 1px solid var(--el-border-color-lighter, #e4e7ed);
  background: var(--bg-container, #fafafa);
  overflow-y: auto;
}
.sidebar-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary, #303133);
  padding: 16px 16px 8px;
  letter-spacing: 1px;
}
.tool-menu {
  border-right: none !important;
  background: transparent;
}
.tool-menu .el-sub-menu .el-menu-item {
  min-width: auto;
  padding-left: 48px !important;
  font-size: 13px;
  height: 36px;
  line-height: 36px;
}

/* 右侧内容 */
.devtools-content {
  flex: 1;
  padding: 20px 28px;
  overflow-y: auto;
  min-width: 0;
}
.tool-panel {
  max-width: 960px;
}
.tool-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #303133);
  margin: 0 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-lighter, #ebeef5);
}
.tool-actions {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

/* 工具标题内描述 */
.tool-desc { font-size: 13px; font-weight: 400; color: var(--text-secondary, #909399); margin-left: 0; }

/* 进制转换 */
.radix-row { display: flex; align-items: center; margin-bottom: 16px; }
.radix-label { font-size: 14px; color: var(--text-regular, #606266); margin-right: 8px; white-space: nowrap; }
.radix-results { background: var(--bg-hover, #f5f7fa); border-radius: 8px; padding: 14px 18px; }
.radix-item { padding: 4px 0; font-size: 14px; color: var(--text-regular, #606266); }
.radix-item code { color: var(--text-primary, #303133); font-weight: 500; background: var(--el-border-color-extra-light, #e8eaed); padding: 2px 6px; border-radius: 3px; font-size: 13px; }

/* UUID / 密码列表 */
.uuid-list { margin-top: 12px; max-height: 400px; overflow: auto; border: 1px solid var(--border-lighter, #ebeef5); border-radius: 6px; }
.uuid-item { padding: 6px 14px; font-family: monospace; font-size: 13px; color: var(--text-primary, #303133); border-bottom: 1px solid var(--border-lighter, #f0f0f0); display: flex; align-items: center; justify-content: space-between; }
.uuid-item:last-child { border-bottom: none; }

/* 密码生成 */
.passgen-settings { margin-bottom: 4px; }
.passgen-row { display: flex; align-items: center; gap: 12px; font-size: 13px; color: var(--text-regular, #606266); }

/* 二维码 */
.qrcode-result { margin-top: 16px; text-align: center; }
.qrcode-result img { border: 1px solid var(--el-border-color-lighter, #e4e7ed); padding: 8px; border-radius: 4px; }

/* 正则测试 */
.regex-templates-wrap { margin-bottom: 12px; }
.regex-group { margin-bottom: 10px; }
.regex-group-name { font-size: 12px; color: var(--text-secondary, #909399); margin-right: 8px; font-weight: 500; vertical-align: middle; }
.regex-tpl-pattern { color: #b0b3bb; margin-left: 4px; font-size: 11px; }
.regex-result-box { margin-top: 16px; border: 1px solid var(--el-border-color-lighter, #e4e7ed); border-radius: 6px; overflow: hidden; max-height: 320px; display: flex; flex-direction: column; }
.regex-result-header { background: var(--bg-hover, #f5f7fa); padding: 8px 14px; font-size: 13px; color: var(--text-regular, #606266); font-weight: 500; border-bottom: 1px solid var(--border-lighter, #ebeef5); flex-shrink: 0; }
.regex-match-item { padding: 6px 14px; font-family: monospace; font-size: 13px; color: var(--text-primary, #303133); border-bottom: 1px solid var(--border-lighter, #f0f0f0); word-break: break-all; }
.regex-match-item:last-child { border-bottom: none; }
.regex-result-empty { margin-top: 12px; text-align: center; color: var(--text-secondary, #909399); font-size: 13px; padding: 20px; background: var(--bg-hover, #fafafa); border-radius: 4px; }

/* 文本统计 */
.text-stats-result { display: grid; grid-template-columns: repeat(4,1fr); gap: 12px; margin-top: 16px; }
.stat-card { text-align: center; padding: 14px 8px; background: var(--bg-hover, #f5f7fa); border-radius: 8px; border: 1px solid var(--border-lighter, #ebeef5); }
.stat-val { display: block; font-size: 24px; font-weight: 700; color: var(--color-primary); }
.stat-lbl { display: block; font-size: 12px; color: var(--text-secondary, #909399); margin-top: 4px; }

/* 时间戳 */
.ts-result { margin-top: 8px; font-size: 16px; font-weight: bold; color: var(--text-primary, #303133); }

/* 日期计算 */
.datecalc-row { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }

/* 颜色工具 */
.color-row { display: flex; gap: 24px; align-items: flex-start; flex-wrap: wrap; }
.color-picker-wrap { display: flex; align-items: center; gap: 12px; }
.color-preview { display: inline-block; width: 48px; height: 48px; border-radius: 8px; border: 1px solid var(--el-border-color-lighter, #dcdfe6); }
.color-values { display: flex; flex-direction: column; gap: 10px; }
.color-field { display: flex; align-items: center; gap: 8px; }
.color-label { font-size: 13px; color: var(--text-regular, #606266); width: 48px; font-weight: 500; }

/* JSON编辑器 - 树形预览 */
.json-tree-panel {
  flex: 1;
  border: 1px solid var(--border-lighter, #ebeef5);
  border-radius: 6px;
  background: var(--bg-container, #fafbfc);
  padding: 12px 16px;
  overflow: auto;
  max-height: 400px;
}
.json-tree-title { font-size: 12px; color: var(--text-secondary, #909399); font-weight: 500; margin-bottom: 8px; }

/* 加密解密 */
.crypto-result-box { display: flex; align-items: flex-start; gap: 8px; margin-top: 12px; padding: 12px 16px; background: var(--bg-hover, #f5f7fa); border-radius: 6px; }
.crypto-label { font-size: 13px; color: var(--text-regular, #606266); white-space: nowrap; }
.crypto-hash { font-size: 14px; font-family: monospace; color: var(--text-primary, #303133); word-break: break-all; }

/* JWT解析 */
.jwt-result { margin-top: 12px; }
.jwt-error { color: #f56c6c; font-size: 13px; padding: 8px 12px; background: #fef0f0; border-radius: 4px; }
.jwt-section { margin-bottom: 12px; border: 1px solid var(--border-lighter, #ebeef5); border-radius: 6px; overflow: hidden; }
.jwt-section-title { background: var(--bg-hover, #f5f7fa); padding: 6px 12px; font-size: 12px; color: var(--text-regular, #606266); font-weight: 500; border-bottom: 1px solid var(--border-lighter, #ebeef5); }
.jwt-json { font-family: monospace; font-size: 12px; padding: 8px 12px; margin: 0; white-space: pre-wrap; word-break: break-all; color: var(--text-primary, #303133); }
.jwt-claims { padding: 6px 12px; display: flex; gap: 20px; font-size: 12px; color: var(--text-secondary, #909399); flex-wrap: wrap; }
.jwt-valid { color: #67c23a; }
.jwt-expired { color: #f56c6c; font-weight: bold; }
.jwt-sig { font-family: monospace; font-size: 11px; color: var(--text-secondary, #909399); padding: 8px 12px; display: block; }

/* HTTP请求测试 */
.http-row { display: flex; align-items: center; gap: 8px; }
.http-label { font-size: 12px; color: var(--text-regular, #606266); margin-bottom: 4px; }
.http-response-box { margin-top: 14px; border: 1px solid var(--border-lighter, #ebeef5); border-radius: 6px; overflow: hidden; }
.http-response-header { display: flex; align-items: center; padding: 8px 14px; background: var(--bg-hover, #f5f7fa); border-bottom: 1px solid var(--border-lighter, #ebeef5); font-size: 13px; color: var(--text-regular, #606266); }
.http-response-body { padding: 14px; margin: 0; font-family: monospace; font-size: 12px; white-space: pre-wrap; word-break: break-all; max-height: 400px; overflow: auto; color: var(--text-primary, #303133); }

/* IP地址查询 */
.ip-result-box { margin-top: 14px; border: 1px solid var(--border-lighter, #ebeef5); border-radius: 6px; overflow: hidden; }
.ip-result-item { display: flex; padding: 8px 14px; border-bottom: 1px solid var(--border-lighter, #f0f0f0); }
.ip-result-item:last-child { border-bottom: none; }
.ip-result-key { width: 80px; font-size: 13px; color: var(--text-secondary, #909399); flex-shrink: 0; }
.ip-result-val { font-size: 13px; color: var(--text-primary, #303133); font-weight: 500; }

/* Cron表达式 */
.cron-builder { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 4px; }
.cron-row { display: flex; align-items: center; gap: 6px; }
.cron-label { font-size: 13px; color: var(--text-regular, #606266); width: 36px; text-align: right; }
.cron-preview { padding: 12px 16px; background: var(--bg-hover, #f5f7fa); border-radius: 8px; }
.cron-expr-box { display: flex; align-items: center; gap: 8px; }
.cron-run-item { font-family: monospace; font-size: 12px; color: var(--text-primary, #303133); padding: 2px 0; padding-left: 12px; border-left: 2px solid var(--color-primary); margin: 4px 0; }

/* CSS渐变生成器 */
.cssgrad-layout { display: flex; gap: 24px; }
.cssgrad-config { display: flex; flex-direction: column; gap: 14px; }
.cssgrad-row { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--text-regular, #606266); flex-wrap: wrap; }
.cssgrad-preview { width: 100%; height: 120px; border-radius: 8px; border: 1px solid var(--el-border-color-lighter, #e4e7ed); margin-top: 4px; }
.cssgrad-code-title { font-size: 12px; color: var(--text-secondary, #909399); margin-bottom: 4px; }
.cssgrad-code { display: block; font-size: 13px; font-family: monospace; color: var(--text-primary, #303133); background: var(--bg-hover, #f0f2f5); padding: 8px 12px; border-radius: 4px; word-break: break-all; }

/* 图像工具 - 通用 */
.img-tool-upload { padding: 32px 0; text-align: center; }
.img-config-row { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; font-size: 13px; color: var(--text-regular, #606266); }
.img-compare { display: flex; gap: 20px; margin-top: 16px; }
.img-compare-item { flex: 1; min-width: 0; }
.img-compare-title { font-size: 13px; color: var(--text-regular, #606266); font-weight: 500; margin-bottom: 8px; }
.img-compare-preview { max-width: 100%; max-height: 360px; border: 1px solid var(--el-border-color-lighter, #e4e7ed); border-radius: 6px; display: block; }
.img-size-tag { margin-left: 8px; font-size: 12px; color: var(--text-secondary, #909399); font-weight: normal; background: var(--bg-hover, #f0f2f5); padding: 2px 8px; border-radius: 4px; }
.img-size-green { color: #67c23a; background: #f0f9eb; }

/* 图片压缩 */
.img-compress-config { margin-bottom: 12px; }

/* 图片水印 */
.wm-config { margin-bottom: 12px; }

/* 图片转ICO */
.ico-config { margin-bottom: 12px; }
.ico-preview-grid { display: flex; flex-wrap: wrap; gap: 8px; align-items: flex-end; }
.ico-preview-item { text-align: center; }
.ico-preview-item img { border: 1px solid var(--el-border-color-lighter, #e4e7ed); border-radius: 2px; display: block; margin-bottom: 2px; }
.ico-preview-item span { font-size: 10px; color: var(--text-secondary, #909399); }

/* Base64图片转换 */
.b64img-sections { display: flex; gap: 24px; }
.b64img-section { flex: 1; }
.b64img-section-title { font-size: 14px; font-weight: 600; color: var(--text-primary, #303133); margin-bottom: 8px; padding-bottom: 6px; border-bottom: 1px solid var(--border-lighter, #ebeef5); }

/* 图标设计器 */
.icondesign-layout { display: flex; gap: 24px; align-items: flex-start; }
.icondesign-config { display: flex; flex-direction: column; gap: 4px; min-width: 280px; }
.icondesign-preview { text-align: center; }
.icondesign-preview-title { font-size: 13px; color: var(--text-secondary, #909399); margin-bottom: 8px; }
.icondesign-canvas { border: 1px solid var(--el-border-color-lighter, #e4e7ed); border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,.06); }

/* Emoji选择器 */
.emoji-search-row { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; flex-wrap: wrap; }
.emoji-cats { display: flex; gap: 4px; flex-wrap: wrap; }
.emoji-cat-chip { font-size: 12px; padding: 3px 10px; border-radius: 12px; background: var(--bg-hover, #f0f2f5); color: var(--text-regular, #606266); cursor: pointer; user-select: none; transition: all .2s; }
.emoji-cat-chip:hover { background: var(--bg-active, #e8eaed); }
.emoji-cat-chip.active { background: var(--color-primary); color: #fff; }
.emoji-grid { display: grid; grid-template-columns: repeat(12, 1fr); gap: 2px; margin-bottom: 16px; }
.emoji-grid-small { grid-template-columns: repeat(18, 1fr); }
.emoji-item { font-size: 24px; text-align: center; padding: 4px 2px; cursor: pointer; user-select: none; border-radius: 4px; transition: background .15s; }
.emoji-item:hover { background: var(--bg-active); transform: scale(1.2); }
.emoji-empty { padding: 32px 0; text-align: center; color: #c0c4cc; font-size: 13px; }
.emoji-recent { padding-top: 12px; border-top: 1px solid var(--border-lighter, #ebeef5); }
.emoji-recent-title { font-size: 13px; color: var(--text-secondary, #909399); margin-bottom: 8px; }
.emoji-grid-small .emoji-item { font-size: 18px; padding: 2px 1px; }
</style>

<!-- 暗黑模式：覆盖内联 style 中无法用 CSS 变量的颜色 -->
<style>
html.dark .devtools-container {
  --dark-text-primary: #e5eaf3;
  --dark-text-regular: #a3a6ad;
  --dark-text-secondary: #73767a;
  --dark-bg-hover: #262727;
}
html.dark .devtools-container [style*="color:#303133"] { color: var(--dark-text-primary, #e5eaf3) !important; }
html.dark .devtools-container [style*="color:#606266"] { color: var(--dark-text-regular, #a3a6ad) !important; }
html.dark .devtools-container [style*="color:#909399"] { color: var(--dark-text-secondary, #73767a) !important; }
html.dark .devtools-container [style*="color:#c0c4cc"] { color: var(--dark-text-secondary, #73767a) !important; }
html.dark .devtools-container [style*="background:#f5f7fa"],
html.dark .devtools-container [style*="background:#fafafa"],
html.dark .devtools-container [style*="background:#f0f2f5"],
html.dark .devtools-container [style*="background:#e8eaed"] { background-color: var(--dark-bg-hover, #262727) !important; }
html.dark .devtools-container [style*="background:#fef0f0"] { background-color: #3a1a1a !important; }
html.dark .devtools-container [style*="background:#f0f9eb"] { background-color: #1a2e1a !important; }
html.dark .devtools-container [style*="border:1px solid #e4e7ed"] { border-color: #363637 !important; }
</style>