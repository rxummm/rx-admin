import { chromium } from 'playwright';

const BASE = 'http://localhost:3000';
const BACKEND = 'http://localhost:8088';

async function waitFor(page, ms) {
  await new Promise(r => setTimeout(r, ms));
}

(async () => {
  const browser = await chromium.launch({
    channel: 'chrome',
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });
  const context = await browser.newContext({
    viewport: { width: 1920, height: 1080 },
    locale: 'zh-CN'
  });
  const page = await context.newPage();

  const issues = [];
  function record(type, msg, detail = '') {
    issues.push({ type, msg, detail });
    console.log(`  [${type}] ${msg} ${detail ? '→ ' + detail : ''}`);
  }

  const consoleErrors = [];
  page.on('console', msg => {
    if (msg.type() === 'error') consoleErrors.push(msg.text());
    if (msg.type() === 'warning') consoleErrors.push('[WARN] ' + msg.text());
  });
  page.on('pageerror', err => consoleErrors.push('[PAGE_ERROR] ' + err.message));

  // ─────────────────────────────
  // 1. Visit login page
  // ─────────────────────────────
  console.log('=== 1. 登录页 ===');
  await page.goto(BASE, { waitUntil: 'networkidle', timeout: 30000 }).catch(() => page.goto(BASE, { waitUntil: 'load', timeout: 30000 }));
  await page.waitForSelector('.login-page', { timeout: 15000 });

  // Wait for fonts to load
  await page.evaluate(() => document.fonts.ready);
  console.log('   字体已加载');

  // Check NProgress
  const nprogress = await page.$('#nprogress');
  if (nprogress) {
    const display = await nprogress.evaluate(el => getComputedStyle(el).display);
    if (display !== 'none') record('WARN', 'NProgress 条在页面加载后未隐藏');
  }

  // Check captcha loads
  const captchaImg = await page.$('.captcha-image img');
  if (!captchaImg) record('ERROR', '验证码图片未渲染');

  // Check i18n works - look for login button text
  const loginBtn = await page.$('.login-btn');
  if (loginBtn) {
    const btnText = await loginBtn.textContent();
    console.log(`   登录按钮文字: "${btnText}"`);
  }

  // ─────────────────────────────
  // 2. Login
  // ─────────────────────────────
  console.log('\n=== 2. 登录流程 ===');
  await page.click('.login-btn');
  await page.waitForURL('**/dashboard', { timeout: 15000 }).catch(() => {});
  const loggedIn = page.url().includes('/dashboard');
  if (!loggedIn) {
    // Check for validation errors
    const errs = await page.$$('.el-form-item__error');
    if (errs.length) {
      for (const e of errs) record('ERROR', '表单验证错误', await e.textContent());
    }
    // Maybe still on login, try again
    await page.waitForTimeout(2000);
    const after = page.url();
    if (!after.includes('/dashboard')) record('ERROR', '登录失败，当前URL', after);
  } else {
    console.log('   登录成功');

    // ─────────────────────────────
    // 3. Dashboard
    // ─────────────────────────────
    console.log('\n=== 3. 仪表盘 ===');
    await page.waitForSelector('.app-main, .el-main, .dashboard-container, .dashboard', { timeout: 10000 }).catch(() => {});
    await waitFor(page, 3000); // wait for SSE / charts to initialize

    // Check for console errors (SSE, charts)
    if (consoleErrors.length) {
      for (const err of consoleErrors) {
        if (err.includes('ECONNREFUSED') || err.includes('ERR_CONNECTION')) {
          record('ERROR', 'SSE/WebSocket 连接失败', err);
        } else if (err.includes('ResizeObserver')) {
          record('WARN', 'ResizeObserver 循环错误', err);
        } else {
          record('WARN', '仪表盘控制台错误', err);
        }
      }
    }

    // Check SSE EventSource connection
    const sseIssues = consoleErrors.filter(e => e.includes('notification/stream') || e.includes('EventSource'));
    if (sseIssues.length) {
      for (const e of sseIssues) record('ERROR', 'SSE 通知流连接失败', e);
    }

    // ─────────────────────────────
    // 4. Layout & Navigation
    // ─────────────────────────────
    console.log('\n=== 4. 布局与导航 ===');
    
    // Check sidebar menu exists
    const sidebar = await page.$('.sidebar-container, .el-menu, .sidebar, aside');
    if (sidebar) {
      const menuItems = await sidebar.$$('.el-menu-item, .el-sub-menu, a');
      console.log(`   侧边栏菜单项数量: ${menuItems.length}`);
    } else {
      record('WARN', '侧边栏菜单未找到');
    }

    // Check header / navbar
    const navbar = await page.$('.navbar, .topbar, header, .el-header');
    if (!navbar) record('WARN', '顶部导航栏未找到');

    // Check language switch in layout
    const langSwitcher = await page.$('[class*="lang"], [class*="locale"], .toolbar-btn, .fa-globe');
    if (langSwitcher) console.log('   语言切换按钮存在');
    else record('WARN', '布局中未找到语言切换按钮');

    // Check theme switch
    const themeBtn = await page.$('.fa-moon, .fa-sun, .fa-palette, [class*="theme"]');
    if (!themeBtn) record('WARN', '主题切换按钮未找到');

    // ─────────────────────────────
    // 5. Navigate to system pages
    // ─────────────────────────────
    console.log('\n=== 5. 系统页面导航 ===');
    
    const pages = [
      { name: '用户管理', path: '/system/user' },
      { name: '角色管理', path: '/system/role' },
      { name: '菜单管理', path: '/system/menu' },
    ];

    for (const p of pages) {
      await page.goto(`${BASE}${p.path}`, { waitUntil: 'networkidle', timeout: 15000 }).catch(() => {});
      await waitFor(page, 2000);
      const err = consoleErrors.filter(e => !e.includes('[WARN]') && !issues.some(i => i.detail === e));
      if (err.length) {
        for (const e of err) record('ERROR', `${p.name} 页面错误`, e);
      }
      
      // Check if the page renders any table or content
      const content = await page.$('.el-table, .app-main, .main-content');
      if (content) {
        const html = await content.innerHTML();
        if (html.length < 50) record('WARN', `${p.name} 页面内容太少`);
      }
      console.log(`   ${p.name}: ${page.url()}`);
    }

    // ─────────────────────────────
    // 6. Profile page
    // ─────────────────────────────
    console.log('\n=== 6. 个人中心 ===');
    await page.goto(`${BASE}/profile`, { waitUntil: 'networkidle', timeout: 15000 }).catch(() => {});
    await waitFor(page, 2000);
    const profileContent = await page.$('.profile, .user-profile, .el-form');
    if (!profileContent) record('WARN', '个人中心页面内容未渲染');
    else console.log('   个人中心页面已渲染');

    // ─────────────────────────────
    // 7. Logout test
    // ─────────────────────────────
    console.log('\n=== 7. 登出 ===');
    const logoutBtn = await page.$('text=退出登录, text=退出, [class*="logout"]');
    if (logoutBtn) {
      await logoutBtn.click();
      await waitFor(page, 2000);
      const atLogin = page.url().includes('/login');
      if (atLogin) console.log('   退出登录成功');
      else record('WARN', '退出登录后未跳转到登录页', page.url());
    }
  }

  // ─────────────────────────────
  // 8. Summary of console errors
  // ─────────────────────────────
  console.log('\n=== 8. 所有控制台错误 ===');
  const uniqueErrors = [...new Set(consoleErrors)];
  if (uniqueErrors.length === 0) {
    console.log('   无控制台错误');
  } else {
    for (const err of uniqueErrors) {
      if (!issues.some(i => i.detail === err)) {
        record('CONSOLE', err);
      }
    }
  }

  // ─────────────────────────────
  // Summary
  // ─────────────────────────────
  console.log('\n' + '='.repeat(50));
  console.log('测试完成');
  console.log(`发现问题数: ${issues.length}`);
  
  const byType = {};
  for (const i of issues) {
    byType[i.type] = (byType[i.type] || 0) + 1;
  }
  for (const [type, count] of Object.entries(byType)) {
    console.log(`  ${type}: ${count}`);
  }

  console.log('\n--- 问题详情 ---');
  for (const i of issues) {
    console.log(`[${i.type}] ${i.msg}`);
    if (i.detail) console.log(`       ${i.detail}`);
  }

  await browser.close();
  return issues;
})().catch(err => {
  console.error('测试异常:', err);
  process.exit(1);
});
