import { chromium } from 'playwright';

const BASE = 'http://localhost:3000';
const BACKEND = 'http://localhost:8088';

async function waitForBackend(page, timeoutMs = 120000) {
  const start = Date.now();
  while (Date.now() - start < timeoutMs) {
    try {
      const resp = await page.request.get(`${BACKEND}/api/auth/captcha`);
      if (resp.ok()) return true;
    } catch {}
    await new Promise(r => setTimeout(r, 2000));
  }
  throw new Error('Backend did not start in time');
}

(async () => {
  const browser = await chromium.launch({
    channel: 'chrome',
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });
  const context = await browser.newContext({
    viewport: { width: 1920, height: 1080 },
    locale: 'zh-CN',
    ignoreHTTPSErrors: true
  });
  const page = await context.newPage();

  const issues = [];

  function record(type, msg, detail = '') {
    issues.push({ type, msg, detail });
    console.log(`  [${type}] ${msg} ${detail}`);
  }

  // Collect console errors
  const consoleErrors = [];
  page.on('console', msg => {
    if (msg.type() === 'error') {
      consoleErrors.push(msg.text());
    }
  });
  page.on('pageerror', err => {
    consoleErrors.push(err.message);
  });

  console.log('\n=== RX Admin E2E Test ===\n');

  // ── Step 1: Visit Login Page ──
  console.log('1. Login Page');
  await page.goto(BASE, { waitUntil: 'networkidle', timeout: 30000 });
  await page.waitForSelector('.login-page', { timeout: 10000 });
  const loginTitle = await page.textContent('.login-title');
  if (!loginTitle) record('ERROR', '登录页标题未渲染');
  else console.log('   ✓ 登录页标题:', loginTitle);

  // Check form elements exist
  const inputs = await page.$$('.login-form input');
  console.log(`   ✓ 登录表单输入框数量: ${inputs.length}`);

  // Check captcha image loads
  const captchaImg = await page.$('.captcha-image img');
  if (captchaImg) {
    const src = await captchaImg.getAttribute('src');
    if (src && src.startsWith('data:image')) console.log('   ✓ 验证码图片已加载');
    else record('WARN', '验证码图片可能未正确加载', src?.substring(0, 50));
  } else {
    record('WARN', '验证码图片元素未找到');
  }

  // ── Step 2: Check for CSS/duplicate properties in login page ──
  console.log('\n2. CSS Analysis (login page)');
  const cssIssues = await page.evaluate(() => {
    const issues = [];
    // Check duplicate properties
    for (const sheet of document.styleSheets) {
      try {
        for (const rule of sheet.cssRules) {
          if (rule.style) {
            const props = {};
            for (let i = 0; i < rule.style.length; i++) {
              const prop = rule.style[i];
              props[prop] = (props[prop] || 0) + 1;
            }
            for (const [prop, count] of Object.entries(props)) {
              if (count > 1) {
                issues.push(`Duplicate CSS property "${prop}" in rule: ${rule.selectorText || 'inline'}`);
              }
            }
          }
        }
      } catch {}
    }
    return issues;
  });
  for (const ci of cssIssues) record('CSS', ci);

  // ── Step 3: Try to Login (need backend) ──
  console.log('\n3. Backend & Login');
  let backendReady = false;
  try {
    await waitForBackend(page, 120000);
    backendReady = true;
    console.log('   ✓ Backend is running');
  } catch (e) {
    record('WARN', '后端未启动，跳过登录测试', e.message);
  }

  if (backendReady) {
    // Check pre-filled credentials
    const usernameInput = await page.$('.login-form input');
    const usernameVal = await usernameInput.inputValue();
    if (usernameVal === 'admin') console.log('   ✓ 开发环境已预填用户名');

    await page.click('.login-btn');
    await page.waitForTimeout(3000);

    // Check for success after login
    const currentUrl = page.url();
    if (currentUrl.includes('/dashboard')) {
      console.log('   ✓ 登录成功，已跳转到仪表盘');
    } else {
      // Might have validation errors
      const errorMsgs = await page.$$('.el-form-item__error');
      if (errorMsgs.length > 0) {
        const errTexts = await Promise.all(errorMsgs.map(e => e.textContent()));
        record('WARN', '登录验证错误', errTexts.join(' | '));
      }
      record('WARN', '登录未跳转到仪表盘', currentUrl);
    }

    // ── Step 4: Dashboard ──
    console.log('\n4. Dashboard');
    await page.goto(`${BASE}/dashboard`, { waitUntil: 'networkidle', timeout: 15000 }).catch(() => {});
    await page.waitForTimeout(2000);
    const dashboardContent = await page.$('.dashboard, .app-main, .main-content, #app > div > div');
    if (dashboardContent) {
      const html = await dashboardContent.innerHTML();
      if (html && html.length > 50) console.log(`   ✓ 仪表盘内容已加载 (${html.length} chars)`);
      else record('WARN', '仪表盘内容太少', html?.substring(0, 100));
    } else {
      // Check if we're still on login or error
      console.log('   - 仪表盘内容未找到（可能未登录）');
    }

    // ── Step 5: Try language switch ──
    console.log('\n5. Language Switch');
    const langBtn = await page.$('.toolbar-btn:last-child, .toolbar-btn .fa-globe');
    if (langBtn) {
      const parentBtn = await langBtn.$('xpath=..');
      await (parentBtn || langBtn).click();
      await page.waitForTimeout(500);
      console.log('   ✓ 语言切换按钮可点击');
    } else {
      // On dashboard, check for language switch in layout
      record('WARN', '页面中未找到语言切换按钮');
    }
  }

  // ── Step 6: 404 Error Page ──
  console.log('\n6. Error Page');
  await page.goto(`${BASE}/some-nonexistent-page`, { waitUntil: 'networkidle', timeout: 10000 }).catch(() => {});
  await page.waitForTimeout(1000);
  const errorPage = await page.$('.error-page, .error, .el-result');
  if (errorPage) {
    const text = await errorPage.textContent();
    console.log(`   ✓ 错误页面已显示: ${text?.substring(0, 60)}`);
  } else {
    const bodyText = await page.textContent('body');
    if (bodyText.includes('404') || bodyText.includes('错误')) {
      console.log('   ✓ 404 页面已显示');
    } else {
      record('WARN', '没有自定义 404 页面', bodyText?.substring(0, 100));
    }
  }

  // ── Step 7: Console Errors ──
  console.log('\n7. Console Errors');
  if (consoleErrors.length === 0) {
    console.log('   ✓ 未发现控制台错误');
  } else {
    for (const err of consoleErrors) {
      record('CONSOLE_ERROR', err);
    }
  }

  // ── Summary ──
  console.log('\n=== Test Summary ===');
  console.log(`Total issues found: ${issues.length}`);
  const byType = {};
  for (const i of issues) {
    byType[i.type] = (byType[i.type] || 0) + 1;
  }
  for (const [type, count] of Object.entries(byType)) {
    console.log(`  ${type}: ${count}`);
  }
  console.log('\n--- Issue Details ---');
  for (const i of issues) {
    console.log(`[${i.type}] ${i.msg} ${i.detail}`);
  }

  await browser.close();
  return issues;
})().catch(err => {
  console.error('Test failed:', err);
  process.exit(1);
});
