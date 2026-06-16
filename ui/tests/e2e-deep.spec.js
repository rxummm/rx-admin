import { chromium } from 'playwright';

const BASE = 'http://localhost:3000';

(async () => {
  const browser = await chromium.launch({
    channel: 'chrome',
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });
  const page = await browser.newPage({
    viewport: { width: 1920, height: 1080 },
    locale: 'zh-CN'
  });

  const issues = [];
  function record(t, m, d) { issues.push({ type: t, msg: m, detail: d || '' }); }

  const apiErrors = [];
  page.on('response', resp => {
    if (resp.url().includes('/api/') && (resp.status() === 404 || resp.status() >= 500)) {
      apiErrors.push({ url: resp.url(), status: resp.status() });
    }
  });

  console.log('1. Login to dashboard...');
  await page.goto(BASE, { waitUntil: 'networkidle', timeout: 30000 });
  await page.waitForSelector('.login-page', { timeout: 10000 });
  await page.click('.login-btn');
  await page.waitForURL('**/dashboard', { timeout: 20000 });
  console.log('   Logged in');

  // Wait for dashboard to fully load
  await page.waitForTimeout(5000);
  await page.screenshot({ path: 'tests/dashboard.png', fullPage: true });
  console.log('   Dashboard screenshot saved');

  const bodyText = await page.textContent('body');
  const keywords = ['仪表盘', 'dashboard', '统计', '欢迎'];
  const found = keywords.filter(k => bodyText.toLowerCase().includes(k.toLowerCase()));
  if (found.length === 0) record('WARN', 'Dashboard missing expected content');
  else console.log('   Dashboard keywords:', found.join(', '));

  // Check tagsView
  const tagsExist = await page.evaluate(() => {
    return !!document.querySelector('.tags-view, .tags-view-wrapper, [class*="tag"], .el-tabs--card');
  });
  if (!tagsExist) record('WARN', 'TagsView not found in layout');

  // Use 'load' instead of 'networkidle' for pages with SSE/streaming
  async function safeGoto(url, name) {
    try {
      await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 15000 });
      await page.waitForTimeout(3000);
      console.log(`   ${name}: OK`);
    } catch (e) {
      record('ERROR', `${name} page load timeout`, e.message?.substring(0, 100));
      try { await page.goto(url, { timeout: 5000 }).catch(() => {}); } catch {}
    }
  }

  // Profile page
  console.log('2. Profile page...');
  await safeGoto(BASE + '/profile', 'Profile');
  await page.screenshot({ path: 'tests/profile.png', fullPage: true }).catch(() => {});
  const profile = await page.evaluate(() => !!document.querySelector('.profile, .user-profile, .el-form'));
  if (!profile) record('WARN', 'Profile page appears empty');

  // System user
  console.log('3. System User...');
  await safeGoto(BASE + '/system/user', 'SystemUser');
  await page.screenshot({ path: 'tests/system-user.png', fullPage: true }).catch(() => {});
  const hasTable = await page.evaluate(() => !!document.querySelector('.el-table, table'));
  if (!hasTable) record('WARN', 'User table not found');

  // System menu
  console.log('4. System Menu...');
  await safeGoto(BASE + '/system/menu', 'SystemMenu');
  await page.screenshot({ path: 'tests/system-menu.png', fullPage: true }).catch(() => {});

  // Role page
  console.log('5. System Role...');
  await safeGoto(BASE + '/system/role', 'SystemRole');
  await page.screenshot({ path: 'tests/system-role.png', fullPage: true }).catch(() => {});

  // Config page
  console.log('6. System Config...');
  await safeGoto(BASE + '/system/config', 'SystemConfig');
  await page.screenshot({ path: 'tests/system-config.png', fullPage: true }).catch(() => {});

  // Monitor online
  console.log('7. Monitor Online...');
  await safeGoto(BASE + '/monitor/online', 'MonitorOnline');
  await page.screenshot({ path: 'tests/monitor-online.png', fullPage: true }).catch(() => {});

  // Error page
  console.log('8. Error page...');
  await page.goto(BASE + '/error/404', { waitUntil: 'domcontentloaded', timeout: 10000 }).catch(() => {});
  await page.waitForTimeout(1000);
  await page.screenshot({ path: 'tests/error-404.png', fullPage: true }).catch(() => {});
  const error404 = await page.evaluate(() => {
    return document.body.textContent.includes('404');
  });
  if (!error404) record('WARN', '404 page not showing 404 content');

  // Check for API errors
  for (const ae of apiErrors) {
    record('API_ERROR', ae.url, 'HTTP ' + ae.status);
  }

  // Mobile viewport
  console.log('9. Mobile viewport...');
  await page.setViewportSize({ width: 375, height: 812 });
  await page.goto(BASE + '/login', { waitUntil: 'domcontentloaded', timeout: 15000 }).catch(() => {});
  await page.waitForTimeout(2000);
  await page.screenshot({ path: 'tests/login-mobile.png', fullPage: true }).catch(() => {});
  const loginWidth = await page.evaluate(() => {
    const w = document.querySelector('.login-wrapper');
    return w ? w.getBoundingClientRect().width : 0;
  });
  if (loginWidth > 350) record('WARN', `Mobile login wrapper too wide: ${loginWidth}px`);

  // Print results
  console.log('\n' + '='.repeat(50));
  console.log('FINAL RESULTS');
  console.log('Issues found:', issues.length);
  for (const i of issues) {
    console.log(`  [${i.type}] ${i.msg}${i.detail ? ' | ' + i.detail : ''}`);
  }
  if (apiErrors.length > 0) {
    console.log('\nAPI errors (' + apiErrors.length + '):');
    for (const ae of apiErrors) {
      console.log(`  ${ae.status} ${ae.url}`);
    }
  }

  await browser.close();
})();
