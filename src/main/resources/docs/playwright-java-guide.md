# Playwright Java 网页自动化操作完全指南

> 本文档详细介绍如何使用 Java + Playwright 对网页内容进行自动化操作，涵盖元素定位、点击、输入、页面交互等所有常见操作。

---

## 目录

1. [环境准备](#1-环境准备)
2. [快速入门](#2-快速入门)
3. [元素定位（核心）](#3-元素定位核心)
4. [常用页面操作](#4-常用页面操作)
5. [键盘控制](#5-键盘控制)
6. [鼠标操作](#6-鼠标操作)
7. [页面导航与等待](#7-页面导航与等待)
8. [弹窗与对话框处理](#8-弹窗与对话框处理)
9. [iframe 操作](#9-iframe-操作)
10. [截图与录制](#10-截图与录制)
11. [网络拦截与请求](#11-网络拦截与请求)
12. [Cookie 与存储](#12-cookie-与存储)
13. [完整示例](#13-完整示例)
14. [常见问题与技巧](#14-常见问题与技巧)

---

## 1. 环境准备

### 1.1 Maven 依赖

在 `pom.xml` 中添加：

```xml
<dependencies>
    <!-- Playwright 核心依赖 -->
    <dependency>
        <groupId>com.microsoft.playwright</groupId>
        <artifactId>playwright</artifactId>
        <version>1.49.0</version>
    </dependency>
</dependencies>
```

### 1.2 Gradle 依赖

```groovy
dependencies {
    implementation 'com.microsoft.playwright:playwright:1.49.0'
}
```

### 1.3 安装浏览器驱动

首次使用时，需要下载浏览器（Chromium、Firefox、WebKit）：

```java
import com.microsoft.playwright.*;

public class InstallBrowsers {
    public static void main(String[] args) {
        // 方式一：代码中自动安装（推荐）
        try (Playwright playwright = Playwright.create()) {
            // 首次运行会自动下载浏览器
        }
        
        // 方式二：命令行安装
        // mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
    }
}
```

### 1.4 完整项目结构示例

```
my-playwright-project/
├── pom.xml
└── src/
    └── test/
        └── java/
            └── com/
                └── example/
                    ├── BaseTest.java      # 基础配置
                    ├── LoginTest.java     # 登录测试
                    └── PageActionsTest.java # 页面操作测试
```

---

## 2. 快速入门

### 2.1 第一个 Playwright 程序

```java
import com.microsoft.playwright.*;

public class QuickStart {
    public static void main(String[] args) {
        // 1. 创建 Playwright 实例
        try (Playwright playwright = Playwright.create()) {
            
            // 2. 启动浏览器（Chromium）
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(false)  // 有头模式，可以看到浏览器操作
                    .setSlowMo(100)     // 每个操作延迟100ms，方便观察
            );
            
            // 3. 创建浏览器上下文和页面
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            
            // 4. 导航到目标网页
            page.navigate("https://example.com");
            
            // 5. 获取页面标题
            String title = page.title();
            System.out.println("页面标题: " + title);
            
            // 6. 获取页面内容
            String content = page.content();
            System.out.println("页面HTML长度: " + content.length());
            
            // 7. 关闭浏览器
            browser.close();
        }
    }
}
```

### 2.2 常用启动选项

```java
Browser browser = playwright.chromium().launch(
    new BrowserType.LaunchOptions()
        .setHeadless(true)                    // 无头模式（默认）
        .setHeadless(false)                  // 有头模式
        .setSlowMo(500)                      // 操作延迟（毫秒）
        .setTimeout(30000)                   // 超时时间
        .setArgs(Arrays.asList(
            "--start-maximized",             // 窗口最大化
            "--disable-web-security",        // 禁用web安全
            "--disable-blink-features=AutomationControlled" // 防检测
        ))
);
```

---

## 3. 元素定位（核心）

Playwright 使用 `Locator` 对象来定位元素，支持多种选择器策略。

### 3.1 基本定位方式

```java
// ===== CSS 选择器（最常用）=====
// 通过 ID
Locator idElem = page.locator("#username");
// 通过 class
Locator classElem = page.locator(".submit-btn");
// 通过 标签名
Locator inputElem = page.locator("input");
// 通过 属性
Locator attrElem = page.locator("input[name='password']");
// 通过 组合选择器
Locator comboElem = page.locator("form > input[type='text']");

// ===== XPath 选择器 =====
Locator xpathElem = page.locator("//input[@id='username']");
Locator xpathText = page.locator("//button[text()='登录']");
Locator xpathContains = page.locator("//button[contains(text(),'登录')]");

// ===== 文本内容定位（推荐）=====
// 精确匹配
Locator exactText = page.getByText("登录");
// 模糊匹配
Locator fuzzyText = page.getByText("登", new Locator.GetByTextOptions().setExact(false));
// 包含匹配
Locator containsText = page.locator("text=登录");

// ===== 角色定位（Playwright 推荐，最稳定）=====
// 通过可访问性角色定位
Locator buttonByRole = page.getByRole(AriaRole.BUTTON, 
    new Page.GetByRoleOptions().setName("登录"));
Locator linkByRole = page.getByRole(AriaRole.LINK, 
    new Page.GetByRoleOptions().setName("忘记密码"));
Locator textboxByRole = page.getByRole(AriaRole.TEXTBOX, 
    new Page.GetByRoleOptions().setName("用户名"));

// ===== 标签名定位 =====
Locator inputByLabel = page.getByLabel("用户名");
Locator passwordByLabel = page.getByLabel("密码");

// ===== Placeholder 定位 =====
Locator placeholder = page.getByPlaceholder("请输入用户名");

// ===== Alt 文本定位（图片）=====
Locator imageByAlt = page.getByAltText("公司Logo");

// ===== Title 定位 =====
Locator titleElem = page.getByTitle("点击刷新");
```

### 3.2 定位技巧与最佳实践

```java
// ===== 链式过滤（精确定位）=====
// 组合多个条件
Locator elem = page.locator("input")
    .filter(new Locator.FilterOptions().setHasText("用户名"));

// 通过父元素定位子元素
Locator childInParent = page.locator("form")
    .locator("input[name='username']");

// 通过兄弟元素定位
Locator sibling = page.locator("label:has-text('用户名')")
    .locator("..")           // 上级元素
    .locator("input");       // 目标元素

// ===== 定位第N个元素 =====
Locator firstButton = page.locator("button").first();   // 第一个
Locator lastButton = page.locator("button").last();     // 最后一个
Locator secondButton = page.locator("button").nth(1);  // 第二个（索引从0开始）

// ===== 等待元素出现 =====
Locator element = page.locator("#dynamic-content");
element.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

// ===== 判断元素是否存在 =====
boolean exists = page.locator("#some-id").count() > 0;
boolean isVisible = page.locator("#some-id").isVisible();
boolean isEnabled = page.locator("#some-id").isEnabled();
boolean isChecked = page.locator("#checkbox").isChecked();
```

### 3.3 获取元素信息

```java
Locator elem = page.locator("#username");

// 获取文本内容
String text = elem.textContent();

// 获取输入框的值
String value = elem.inputValue();

// 获取属性值
String attrValue = elem.getAttribute("type");
String href = elem.getAttribute("href");

// 获取CSS属性
String color = (String) elem.evaluate("el => getComputedStyle(el).color");

// 获取元素尺寸和位置
BoundingBox box = elem.boundingBox();
if (box != null) {
    System.out.println("x: " + box.x);
    System.out.println("y: " + box.y);
    System.out.println("width: " + box.width);
    System.out.println("height: " + box.height);
}

// 获取所有匹配元素
int count = elem.count();
for (int i = 0; i < count; i++) {
    String itemText = elem.nth(i).textContent();
    System.out.println("第" + i + "个元素: " + itemText);
}
```

---

## 4. 常用页面操作

### 4.1 点击操作

```java
// ===== 基本点击 =====
page.locator("#button").click();

// ===== 带选项的点击 =====
page.locator("#button").click(new Locator.ClickOptions()
    .setButton(MouseButton.RIGHT)   // 右键点击
    .setClickCount(2)               // 双击
    .setDelay(100)                  // 按下和释放之间的延迟
    .setPosition(10, 10)            // 点击元素内的指定位置
    .setForce(true)                 // 强制点击（忽略可见性检查）
    .setNoWaitAfter(true)           // 点击后不等待
    .setTimeout(5000)               // 超时时间
);

// ===== 双击 =====
page.locator("#button").dblclick();

// ===== 右键点击 =====
page.locator("#button").click(new Locator.ClickOptions()
    .setButton(MouseButton.RIGHT));

// ===== 点击坐标 =====
page.mouse().click(100, 200);  // 点击页面坐标(100, 200)

// ===== 条件点击（元素可见才点击）=====
Locator btn = page.locator("#submit");
if (btn.isVisible()) {
    btn.click();
}
```

### 4.2 输入操作

```java
// ===== 文本框输入 =====
// 方式一：fill（推荐，会清空再输入）
page.locator("#username").fill("myusername");

// 方式二：type（逐字输入，可设置延迟）
page.locator("#username").type("myusername", 
    new Locator.TypeOptions().setDelay(100));  // 每个字符间隔100ms

// ===== 清空输入框 =====
page.locator("#username").clear();

// ===== 追加输入 =====
page.locator("#username").fill(page.locator("#username").inputValue() + "追加内容");

// ===== 按字符输入（模拟真实打字）=====
page.locator("#username").pressSequentially("myusername", 
    new Locator.PressSequentiallyOptions().setDelay(50));

// ===== 输入后按回车 =====
page.locator("#username").fill("myusername");
page.locator("#username").press("Enter");

// 或者一步完成
page.locator("#username").fill("myusername");
page.keyboard().press("Enter");
```

### 4.3 下拉框操作

```java
// ===== 通过 value 选择 =====
page.locator("#city").selectOption("beijing");

// ===== 通过 label 选择 =====
page.locator("#city").selectOption(new SelectOption().setLabel("北京"));

// ===== 多选 =====
page.locator("#hobbies").selectOption(new String[]{"reading", "sports"});

// ===== 获取已选值 =====
String selectedValue = (String) page.locator("#city").evaluate("select => select.value");
String selectedText = (String) page.locator("#city")
    .locator("option:checked").textContent();
```

### 4.4 单选框和复选框

```java
// ===== 单选框 =====
page.locator("input[name='gender'][value='male']").check();
boolean isMaleSelected = page.locator("input[name='gender'][value='male']").isChecked();

// ===== 复选框 =====
page.locator("#agree").check();       // 勾选
page.locator("#agree").uncheck();     // 取消勾选
page.locator("#agree").setChecked(true);  // 设置为勾选状态

// ===== 判断复选框状态 =====
boolean isChecked = page.locator("#agree").isChecked();
```

### 4.5 文件上传

```java
// ===== 单文件上传 =====
page.locator("input[type='file']").setInputFiles("D:/test/avatar.png");

// ===== 多文件上传 =====
page.locator("input[type='file']").setInputFiles(new Path[]{
    Paths.get("D:/test/file1.pdf"),
    Paths.get("D:/test/file2.pdf")
});

// ===== 监听文件上传事件 =====
page.onFileChooser((FileChooser fileChooser) -> {
    fileChooser.setFiles(Paths.get("D:/test/upload.pdf"));
});
```

### 4.6 下拉菜单（非select元素）

```java
// 对于用div/ul模拟的下拉菜单
page.locator("#dropdown-trigger").click();  // 点击触发下拉
page.waitForSelector("#dropdown-menu");      // 等待菜单出现
page.locator("#dropdown-menu li:has-text('选项A')").click();  // 点击选项
```

### 4.7 页面滚动

```java
// ===== 滚动到元素可见 =====
page.locator("#footer").scrollIntoViewIfNeeded();

// ===== 滚动到指定坐标 =====
page.evaluate("window.scrollTo(0, 500)");   // 向下滚动500px
page.evaluate("window.scrollTo(0, document.body.scrollHeight)");  // 滚动到底部

// ===== 使用鼠标滚轮滚动 =====
page.mouse().wheel(0, 500);   // 向下滚动500像素
page.mouse().wheel(0, -500);  // 向上滚动500像素
page.mouse().wheel(300, 0);   // 向右滚动300像素

// ===== 滚动到顶部 =====
page.evaluate("window.scrollTo(0, 0)");

// ===== 获取页面滚动位置 =====
int scrollY = (int) page.evaluate("() => window.pageYOffset");
int scrollX = (int) page.evaluate("() => window.pageXOffset");

// ===== 获取页面总高度 =====
int pageHeight = (int) page.evaluate("() => document.body.scrollHeight");
```

---

## 5. 键盘控制

### 5.1 基本键盘操作

```java
// ===== 按下单个键 =====
page.keyboard().press("Enter");
page.keyboard().press("Tab");
page.keyboard().press("Escape");
page.keyboard().press("Space");
page.keyboard().press("Backspace");
page.keyboard().press("Delete");

// ===== 按下修饰键 =====
page.keyboard().press("Control+A");   // Ctrl+A 全选
page.keyboard().press("Control+C");   // Ctrl+C 复制
page.keyboard().press("Control+V");   // Ctrl+V 粘贴
page.keyboard().press("Control+Z");   // Ctrl+Z 撤销
page.keyboard().press("Shift+Tab");    // Shift+Tab

// ===== 方向键 =====
page.keyboard().press("ArrowUp");
page.keyboard().press("ArrowDown");
page.keyboard().press("ArrowLeft");
page.keyboard().press("ArrowRight");

// ===== 功能键 =====
page.keyboard().press("F5");         // 刷新
page.keyboard().press("F12");        // 开发者工具
```

### 5.2 组合键操作

```java
// ===== 方式一：使用 press 组合 =====
page.keyboard().press("Control+Shift+N");  // Ctrl+Shift+N

// ===== 方式二：分步操作 =====
// Ctrl+C 复制
page.keyboard().down("Control");
page.keyboard().press("C");
page.keyboard().up("Control");

// ===== 在指定元素上执行键盘操作 =====
Locator input = page.locator("#search");
input.focus();  // 先聚焦
page.keyboard().type("搜索内容");
page.keyboard().press("Enter");
```

### 5.3 输入文本

```java
// ===== 直接输入文本 =====
page.keyboard().type("Hello, Playwright!");

// ===== 带延迟输入（模拟真人）=====
page.keyboard().type("Hello", new Keyboard.TypeOptions().setDelay(100));

// ===== 插入文本（不触发键盘事件）=====
page.locator("#editor").evaluate("el => el.innerHTML = '<p>新内容</p>'");
```

---

## 6. 鼠标操作

### 6.1 基本鼠标操作

```java
// ===== 点击指定坐标 =====
page.mouse().click(100, 200);

// ===== 双击 =====
page.mouse().dblclick(100, 200);

// ===== 右键 =====
page.mouse().click(100, 200, new Mouse.ClickOptions()
    .setButton(MouseButton.RIGHT));

// ===== 鼠标按下/移动/释放（拖拽）=====
page.mouse().move(100, 200);    // 移动到起始位置
page.mouse().down();             // 按下
page.mouse().move(300, 400);    // 拖拽到目标位置
page.mouse().up();               // 释放

// ===== 拖拽元素（推荐方式）=====
page.locator("#draggable").dragTo(page.locator("#drop-target"));
```

### 6.2 悬停操作

```java
// ===== 悬停在元素上 =====
page.locator("#menu-item").hover();

// ===== 带选项的悬停 =====
page.locator("#menu-item").hover(new Locator.HoverOptions()
    .setPosition(10, 10)         // 悬停在元素内的指定位置
    .setTimeout(5000));          // 超时时间
```

---

## 7. 页面导航与等待

### 7.1 页面导航

```java
// ===== 打开网页 =====
page.navigate("https://example.com");

// ===== 带选项的导航 =====
page.navigate("https://example.com", new Page.NavigateOptions()
    .setWaitUntil(WaitUntilState.NETWORKIDLE)  // 等待网络空闲
    .setTimeout(30000));                       // 超时30秒

// ===== 刷新页面 =====
page.reload();
page.reload(new Page.ReloadOptions()
    .setWaitUntil(WaitUntilState.NETWORKIDLE));

// ===== 前进/后退 =====
page.goBack();   // 后退
page.goForward(); // 前进

// ===== 获取当前URL =====
String currentUrl = page.url();

// ===== 获取页面标题 =====
String title = page.title();
```

### 7.2 等待操作（重要）

```java
// ===== 等待元素出现 =====
page.waitForSelector("#content", new Page.WaitForSelectorOptions()
    .setState(WaitForSelectorState.VISIBLE)
    .setTimeout(10000));

// ===== 等待元素消失 =====
page.waitForSelector("#loading", new Page.WaitForSelectorOptions()
    .setState(WaitForSelectorState.HIDDEN));

// ===== 等待导航完成 =====
page.waitForNavigation(() -> {
    page.locator("#link").click();
});

// ===== 等待网络空闲 =====
page.waitForLoadState(LoadState.NETWORKIDLE);

// ===== 等待DOM加载完成 =====
page.waitForLoadState(LoadState.DOMCONTENTLOADED);

// ===== 等待页面完全加载 =====
page.waitForLoadState(LoadState.LOAD);

// ===== 自定义等待（等待条件满足）=====
page.waitForCondition(() -> 
    page.locator("#progress").getAttribute("value").equals("100"));

// ===== 强制等待（不推荐，仅用于调试）=====
page.waitForTimeout(2000);  // 等待2秒
```

### 7.3 等待的最佳实践

```java
// ✅ 推荐：等待特定元素
page.locator("#submit").waitFor(new Locator.WaitForOptions()
    .setState(WaitForSelectorState.VISIBLE));

// ✅ 推荐：等待网络请求完成
page.waitForResponse(response -> 
    response.url().contains("/api/data") && response.status() == 200);

// ❌ 不推荐：使用 sleep
Thread.sleep(2000);  // 应该避免
```

---

## 8. 弹窗与对话框处理

### 8.1 警告框（Alert）

```java
// 监听并自动接受警告框
page.onDialog(dialog -> {
    System.out.println("警告内容: " + dialog.message());
    dialog.accept();  // 点击"确定"
});

// 触发警告框
page.locator("#alert-btn").click();
```

### 8.2 确认框（Confirm）

```java
// 自动接受确认框
page.onDialog(dialog -> {
    System.out.println("确认内容: " + dialog.message());
    dialog.accept();  // 点击"确定"
    // dialog.dismiss();  // 点击"取消"
});

page.locator("#confirm-btn").click();
```

### 8.3 提示框（Prompt）

```java
// 在提示框中输入内容并接受
page.onDialog(dialog -> {
    System.out.println("提示内容: " + dialog.message());
    dialog.accept("输入的内容");  // 输入内容并确定
});

page.locator("#prompt-btn").click();
```

### 8.4 新窗口/弹窗处理

```java
// ===== 监听新页面打开 =====
Page popupPage = page.waitForPopup(() -> {
    page.locator("#open-new-window").click();
});
// 在新页面上操作
popupPage.locator("#close").click();
popupPage.close();

// ===== 监听新页面（事件方式）=====
page.onPopup(popup -> {
    System.out.println("新窗口URL: " + popup.url());
    popup.waitForLoadState();
    // 在新窗口中操作
    popup.close();
});
```

---

## 9. iframe 操作

### 9.1 基本 iframe 操作

```java
// ===== 通过选择器获取 iframe =====
FrameLocator frame = page.frameLocator("#iframe-id");

// ===== 通过 name 属性获取 =====
FrameLocator frame = page.frame("frame-name");

// ===== 通过 URL 获取 =====
FrameLocator frame = page.frameByUrl(".*forms.*");

// ===== 在 iframe 中定位元素 =====
frame.locator("#username").fill("testuser");
frame.locator("#password").fill("password123");
frame.locator("button:has-text('登录')").click();

// ===== 获取 iframe 内容 =====
String frameContent = (String) page.frame("frame-name")
    .evaluate("frame => frame.document.body.innerHTML");
```

### 9.2 嵌套 iframe

```java
// 外层 iframe
FrameLocator outerFrame = page.frameLocator("#outer-frame");
// 内层 iframe
FrameLocator innerFrame = outerFrame.frameLocator("#inner-frame");
// 操作内层 iframe 中的元素
innerFrame.locator("#deep-element").click();
```

---

## 10. 截图与录制

### 10.1 页面截图

```java
// ===== 全页面截图 =====
page.screenshot(new Page.ScreenshotOptions()
    .setPath(Paths.get("screenshots/full-page.png"))
    .setFullPage(true));  // 截取整个页面（包括滚动部分）

// ===== 可视区域截图 =====
page.screenshot(new Page.ScreenshotOptions()
    .setPath(Paths.get("screenshots/viewport.png")));

// ===== 元素截图 =====
page.locator("#header").screenshot(new Locator.ScreenshotOptions()
    .setPath(Paths.get("screenshots/header.png")));

// ===== 带选项的截图 =====
page.screenshot(new Page.ScreenshotOptions()
    .setPath(Paths.get("screenshots/annotated.png"))
    .setType(ScreenshotType.PNG)
    .setQuality(80)           // JPEG质量（0-100）
    .setTimeout(5000)
    .setAnimations(ScreenshotAnimations.DISABLED)  // 禁用动画
    .setCaret(ScreenshotCaret.HIDE)                 // 隐藏光标
    .setMask(Arrays.asList(                         // 遮罩敏感信息
        page.locator("#sensitive-data")
    ))
);
```

### 10.2 录制视频

```java
// ===== 为整个上下文录制视频 =====
BrowserContext context = browser.newContext(new Browser.NewContextOptions()
    .setRecordVideoDir(Paths.get("videos/"))
    .setRecordVideoSize(1280, 720));

Page page = context.newPage();
// ... 执行操作 ...
page.close();
context.close();  // 必须关闭context，视频才会保存
```

---

## 11. 网络拦截与请求

### 11.1 监听网络请求

```java
// ===== 监听所有请求 =====
page.onRequest(request -> {
    System.out.println("请求: " + request.url());
    System.out.println("方法: " + request.method());
    System.out.println("请求头: " + request.headers());
});

// ===== 监听响应 =====
page.onResponse(response -> {
    System.out.println("响应: " + response.url());
    System.out.println("状态码: " + response.status());
    if (response.url().contains("/api/")) {
        System.out.println("API响应体: " + response.text());
    }
});

// ===== 等待特定请求完成 =====
Response response = page.waitForResponse(resp -> 
    resp.url().contains("/api/login") && resp.status() == 200);
String responseBody = response.text();
```

### 11.2 拦截和修改请求

```java
// ===== 拦截请求（Mock）=====
page.route("**/api/data", route -> {
    // 返回自定义响应
    route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("application/json")
        .setBody("{\"success\": true, \"data\": \"mocked\"}"));
});

// ===== 修改请求头 =====
page.route("**/api/**", route -> {
    Map<String, String> headers = new HashMap<>(route.request().headers());
    headers.put("Authorization", "Bearer fake-token");
    route.resume(new Route.ResumeOptions().setHeaders(headers));
});

// ===== 阻止图片加载（加速测试）=====
page.route("**/*.{png,jpg,jpeg,gif}", Route::abort);

// ===== 阻止第三方请求 =====
page.route("**/analytics/**", Route::abort);
page.route("**/ads/**", Route::abort);
```

---

## 12. Cookie 与存储

### 12.1 Cookie 操作

```java
// ===== 获取所有 Cookie =====
List<Cookie> cookies = context.cookies();
for (Cookie cookie : cookies) {
    System.out.println("名称: " + cookie.name);
    System.out.println("值: " + cookie.value);
    System.out.println("域名: " + cookie.domain);
}

// ===== 获取指定URL的 Cookie =====
List<Cookie> cookies = context.cookies(Arrays.asList("https://example.com"));

// ===== 添加 Cookie =====
context.addCookies(Arrays.asList(
    new Cookie("session_id", "abc123")
        .setDomain("example.com")
        .setPath("/")
        .setExpires(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
        .setHttpOnly(true)
        .setSecure(true)
));

// ===== 清除 Cookie =====
context.clearCookies();
```

### 12.2 LocalStorage 和 SessionStorage

```java
// ===== 获取 LocalStorage =====
String localStorage = (String) page.evaluate("() => JSON.stringify(localStorage)");

// ===== 设置 LocalStorage =====
page.evaluate("localStorage.setItem('token', 'my-token-123')");

// ===== 获取 SessionStorage =====
String sessionStorage = (String) page.evaluate("() => JSON.stringify(sessionStorage)");

// ===== 设置 SessionStorage =====
page.evaluate("sessionStorage.setItem('user', 'test')");

// ===== 清除 Storage =====
page.evaluate("localStorage.clear()");
page.evaluate("sessionStorage.clear()");
```

### 12.3 保存和恢复浏览器状态

```java
// ===== 保存浏览器状态（Cookie + LocalStorage）=====
BrowserContext context = browser.newContext();
Page page = context.newPage();
page.navigate("https://example.com/login");
// ... 登录操作 ...

// 保存状态到文件
context.storageState(new BrowserContext.StorageStateOptions()
    .setPath(Paths.get("auth.json")));

// ===== 恢复浏览器状态（跳过登录）=====
BrowserContext savedContext = browser.newContext(
    new Browser.NewContextOptions()
        .setStorageStatePath(Paths.get("auth.json"))
);
Page savedPage = savedContext.newPage();
savedPage.navigate("https://example.com/dashboard");  // 已经是登录状态
```

---

## 13. 完整示例

### 13.1 登录自动化示例

```java
import com.microsoft.playwright.*;
import java.nio.file.Paths;

public class LoginExample {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(500)
            );
            
            Page page = browser.newPage();
            
            // 1. 打开登录页
            page.navigate("https://example.com/login");
            System.out.println("页面标题: " + page.title());
            
            // 2. 等待页面加载
            page.waitForLoadState(LoadState.NETWORKIDLE);
            
            // 3. 输入用户名
            Locator usernameInput = page.locator("#username");
            usernameInput.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));
            usernameInput.fill("testuser");
            
            // 4. 输入密码
            page.locator("#password").fill("password123");
            
            // 5. 勾选记住密码
            Locator rememberMe = page.locator("#remember-me");
            if (!rememberMe.isChecked()) {
                rememberMe.check();
            }
            
            // 6. 点击登录按钮
            page.locator("button[type='submit']").click();
            
            // 7. 等待登录成功（等待跳转后的元素出现）
            page.waitForSelector(".welcome-message", new Page.WaitForSelectorOptions()
                .setTimeout(10000));
            
            System.out.println("登录成功!");
            System.out.println("当前URL: " + page.url());
            
            // 8. 截图保存
            page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/login-success.png"))
                .setFullPage(true));
            
            // 9. 保存登录状态
            page.context().storageState(new BrowserContext.StorageStateOptions()
                .setPath(Paths.get("auth.json")));
            
            browser.close();
        }
    }
}
```

### 13.2 表单自动填写示例

```java
public class FormAutoFillExample {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false)
            );
            Page page = browser.newPage();
            
            page.navigate("https://example.com/register");
            
            // 文本框
            page.locator("#name").fill("张三");
            page.locator("#email").fill("zhangsan@example.com");
            page.locator("#phone").fill("13800138000");
            
            // 下拉框
            page.locator("#city").selectOption("beijing");
            
            // 单选框
            page.locator("input[name='gender'][value='male']").check();
            
            // 复选框（多选）
            page.locator("input[name='hobby'][value='reading']").check();
            page.locator("input[name='hobby'][value='sports']").check();
            
            // 日期选择器
            page.locator("#birthday").fill("1990-01-01");
            
            // 文本域
            page.locator("#address").fill("北京市朝阳区xxx街道xxx号");
            
            // 上传头像
            page.locator("#avatar").setInputFiles(Paths.get("D:/photos/avatar.jpg"));
            
            // 同意协议
            page.locator("#agree-terms").check();
            
            // 提交
            page.locator("#submit-btn").click();
            
            // 等待提交成功
            page.waitForSelector(".success-message");
            System.out.println("表单提交成功!");
            
            browser.close();
        }
    }
}
```

### 13.3 数据爬取示例

```java
import com.microsoft.playwright.*;
import java.util.ArrayList;
import java.util.List;

public class WebScrapingExample {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)  // 无头模式，更快
            );
            Page page = browser.newPage();
            
            // 打开目标网页
            page.navigate("https://news.example.com");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            
            // 滚动加载更多内容（处理懒加载）
            for (int i = 0; i < 3; i++) {
                page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
                page.waitForTimeout(2000);  // 等待内容加载
            }
            
            // 提取数据
            List<String> titles = new ArrayList<>();
            List<String> links = new ArrayList<>();
            
            int count = page.locator(".news-item").count();
            for (int i = 0; i < count; i++) {
                Locator item = page.locator(".news-item").nth(i);
                
                String title = item.locator(".title").textContent();
                String link = item.locator("a").getAttribute("href");
                
                titles.add(title);
                links.add(link);
                
                System.out.println("标题: " + title);
                System.out.println("链接: " + link);
                System.out.println("---");
            }
            
            System.out.println("共爬取 " + titles.size() + " 条数据");
            
            browser.close();
        }
    }
}
```

### 13.4 复杂交互示例（下拉菜单、弹窗等）

```java
public class ComplexInteractionExample {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(300)
            );
            Page page = browser.newPage();
            
            page.navigate("https://example.com");
            
            // 1. 处理下拉菜单（悬停触发）
            Locator menu = page.locator("#nav-menu");
            menu.hover();  // 悬停显示子菜单
            page.waitForSelector("#sub-menu");  // 等待子菜单出现
            page.locator("#sub-menu li:has-text('设置')").click();
            
            // 2. 处理弹窗
            page.onDialog(dialog -> {
                System.out.println("弹窗内容: " + dialog.message());
                dialog.accept();
            });
            page.locator("#delete-btn").click();
            
            // 3. 处理新窗口
            Page newPage = page.waitForPopup(() -> {
                page.locator("#open-new-tab").click();
            });
            newPage.waitForLoadState();
            System.out.println("新窗口标题: " + newPage.title());
            newPage.close();
            
            // 4. 处理 iframe
            FrameLocator iframe = page.frameLocator("#content-frame");
            iframe.locator("#iframe-button").click();
            
            // 5. 键盘快捷键
            page.keyboard().press("Control+K");  // 打开搜索
            page.locator("#search-input").fill("Playwright");
            page.keyboard().press("Enter");
            
            browser.close();
        }
    }
}
```

---

## 14. 常见问题与技巧

### 14.1 元素定位不到怎么办？

```java
// 问题：元素在 iframe 中
// 解决：先切换到 iframe
FrameLocator frame = page.frameLocator("#iframe-id");
frame.locator("#element").click();

// 问题：元素被遮挡
// 解决：强制点击
page.locator("#element").click(new Locator.ClickOptions().setForce(true));

// 问题：元素在视口外
// 解决：滚动到可见
page.locator("#element").scrollIntoViewIfNeeded();
page.locator("#element").click();

// 问题：页面有动态加载
// 解决：增加等待
page.waitForSelector("#dynamic-element", new Page.WaitForSelectorOptions()
    .setState(WaitForSelectorState.VISIBLE)
    .setTimeout(30000));
```

### 14.2 提高脚本稳定性

```java
// ✅ 使用角色定位（最稳定）
page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("登录")).click();

// ✅ 使用等待代替 sleep
page.locator("#content").waitFor(new Locator.WaitForOptions()
    .setState(WaitForSelectorState.VISIBLE));

// ✅ 使用 try-catch 处理异常
try {
    page.locator("#maybe-exists").click();
} catch (PlaywrightException e) {
    System.out.println("元素不存在: " + e.getMessage());
}

// ✅ 设置合理的超时时间
page.setDefaultTimeout(30000);  // 全局超时30秒
```

### 14.3 调试技巧

```java
// ===== 使用 slowMo 减慢执行 =====
Browser browser = playwright.chromium().launch(
    new BrowserType.LaunchOptions()
        .setHeadless(false)
        .setSlowMo(1000)  // 每个操作延迟1秒
);

// ===== 使用调试模式 =====
// 设置环境变量
// PWDEBUG=1 mvn test

// ===== 在浏览器中暂停执行 =====
page.pause();  // 会打开 Playwright Inspector

// ===== 截图调试 =====
page.screenshot(new Page.ScreenshotOptions()
    .setPath(Paths.get("debug-" + System.currentTimeMillis() + ".png"))
    .setFullPage(true));

// ===== 打印页面内容调试 =====
System.out.println(page.content());

// ===== 高亮元素（调试用）=====
page.locator("#target").evaluate("el => el.style.border = '3px solid red'");
```

### 14.4 常用工具方法封装

```java
public class PlaywrightUtils {
    
    // 等待并点击（带重试）
    public static void waitAndClick(Page page, String selector, int timeout) {
        page.waitForSelector(selector, new Page.WaitForSelectorOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(timeout));
        page.locator(selector).click();
    }
    
    // 等待并输入
    public static void waitAndFill(Page page, String selector, String text, int timeout) {
        page.waitForSelector(selector, new Page.WaitForSelectorOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(timeout));
        page.locator(selector).fill(text);
    }
    
    // 滚动到元素
    public static void scrollToElement(Page page, String selector) {
        page.locator(selector).scrollIntoViewIfNeeded();
    }
    
    // 判断元素是否存在
    public static boolean isElementExists(Page page, String selector) {
        return page.locator(selector).count() > 0;
    }
    
    // 获取表格数据
    public static List<List<String>> getTableData(Page page, String tableSelector) {
        List<List<String>> tableData = new ArrayList<>();
        int rowCount = page.locator(tableSelector + " tr").count();
        
        for (int i = 0; i < rowCount; i++) {
            List<String> rowData = new ArrayList<>();
            int colCount = page.locator(tableSelector + " tr").nth(i)
                .locator("td, th").count();
            
            for (int j = 0; j < colCount; j++) {
                String cellText = page.locator(tableSelector + " tr").nth(i)
                    .locator("td, th").nth(j).textContent();
                rowData.add(cellText.trim());
            }
            tableData.add(rowData);
        }
        return tableData;
    }
}
```

---

## 附录：选择器速查表

| 选择器类型 | 语法 | 示例 |
|-----------|------|------|
| ID选择器 | `#id` | `page.locator("#username")` |
| Class选择器 | `.class` | `page.locator(".btn-primary")` |
| 属性选择器 | `[attr=value]` | `page.locator("input[type='text']")` |
| 文本选择器 | `text=内容` | `page.locator("text=登录")` |
| 角色选择器 | 使用 `getByRole` | `page.getByRole(AriaRole.BUTTON)` |
| Label选择器 | 使用 `getByLabel` | `page.getByLabel("用户名")` |
| XPath | `//tag[@attr='val']` | `page.locator("//input[@id='user']")` |
| 组合选择器 | `parent > child` | `page.locator("form > input")` |
| 第N个匹配 | `:nth-child(n)` | `page.locator("li:nth-child(2)")` |
| 包含文本 | `:has-text()` | `page.locator("button:has-text('确定')")` |

---

## 附录：常用键码速查

| 键 | 代码 |
|----|------|
| 回车 | `Enter` |
| Tab | `Tab` |
| 空格 | `Space` |
| 退格 | `Backspace` |
| 删除 | `Delete` |
| 上 | `ArrowUp` |
| 下 | `ArrowDown` |
| 左 | `ArrowLeft` |
| 右 | `ArrowRight` |
| Home | `Home` |
| End | `End` |
| PageUp | `PageUp` |
| PageDown | `PageDown` |
| F1-F12 | `F1` ~ `F12` |
| Ctrl | `Control` |
| Alt | `Alt` |
| Shift | `Shift` |
| Esc | `Escape` |

---

*文档版本：1.0*
*最后更新：2025年*
*Playwright 版本：1.49.0*
