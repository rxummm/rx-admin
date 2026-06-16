# AS400 (IBM i) Java 环境 .cer 证书安装、多版本排查与维护规范说明书

---

## 🛠️ 第一部分：Windows 端证书的重新调整与标准导出

当 keytool 导入提示编码错误时，通常是因为证书在跨平台传输时发生了换行符损坏（Windows 的 `\r\n` 变成了 AS400 的 `\n`），或者二进制（DER）与文本（Base64）格式混淆。

利用 Windows 自带的证书向导，将证书强制规范化导出为兼容性最好的 **DER 编码二进制格式**，可以彻底解决此类由于文件底层结构损坏带来的编码问题。

### 步骤 1：利用 Windows 证书向导重新规范化导出

1. 在 Windows 电脑上，双击打开那个报错的 `.cer` 文件。
2. 在弹出的"证书"属性窗口中，切换到 **详细信息 (Details)** 选项卡。
3. 点击右下角的 **复制到文件... (Copy to File...)** 按钮，系统会启动"证书导出向导"。
4. 点击"下一步"，在"导出文件格式"页面中：
   - 🌟 必须单选勾选：**DER 编码二进制 X.509 (.CER)** (DER encoded binary X.509)。
5. 点击"下一步"，点击"浏览"指定一个新路径并起个新名字（例如 `standard_server.cer`），点击保存并完成导出。

### 步骤 2：安全上传至 AS400 (IFS)

DER 编码是纯二进制格式。在上传到 AS400 的集成文件系统（IFS）时，必须确保以二进制模式传输，否则文件会被 AS400 的 EBCDIC 字符集自动转换进而再次损坏：

- **方法 A（推荐）**：打开 ACS (IBM i Access Client Solutions) → 点击 Integrated File System (IFS) → 浏览到目标目录（如 `/tmp`）→ 直接将刚才导出的 `standard_server.cer` 拖拽或上传进去。ACS 会默认使用二进制流传输。
- **方法 B（FTP）**：如果使用传统命令行 FTP，在执行 `put` 上传之前，必须先输入 `bin` 切换为二进制模式。

---

## 🔍 第二部分：多版本 Java 运行环境精准排查与处理

AS400 系统允许安装和并发运行多个不同的 JDK 版本（如 Java 8, Java 11, Java 17 共同存在）。如果系统中看到多个 Java 版本，必须精准定位当前报错的业务系统在使用哪一个，否则将证书安装到错误的信任库（cacerts）中将毫无效果。

### 1. 检查系统已安装的所有 Java 目录

在 AS400 绿屏幕命令行输入 `STRQSH` 进入 Qshell 环境，运行以下命令列出系统里所有已安装的 IBM JDK 目录：

```sh
ls -d /QOpenSys/QIBM/ProdData/JavaVM/jdk*
```

界面会输出类似 `/jdk80`, `/jdk11`, `/jdk17` 等多个目录。

### 2. 精准定位"当前报错的业务应用"在使用哪一个版本

请通过以下三种方法进行排查：

**方法 1：检查应用的启动脚本 (CLP 或 Shell 脚本)**

检查报错业务系统的启动命令或控制脚本，寻找是否包含 `ADDENVVAR ENVVAR(JAVA_HOME)` 或 `export JAVA_HOME=...`。脚本里显式指定的那个路径就是你需要安装证书的地方。

**方法 2：通过活动作业 (WRKACTJOB) 逆向排查**

1. 在绿屏幕输入 `WRKACTJOB` 找到对应的业务子系统（或系统默认的 QSERVER/QBATCH）。
2. 找到你的 Java 业务作业（类型通常为 BCI，作业名可能是 QJVACMDSRV 或特定的应用名）。
3. 在该作业前输入 `5` (Work with) 展开，然后选择 `20` (Work with environment variables)。
4. 在环境变量列表中查找 `JAVA_HOME` 变量的值。这个值对应的就是当前正在运行的 JDK 物理路径。

### 3. 多版本共存时的终极运维策略（宁错杀，不放过）

如果你通过上述方法依然无法百分之百确定是哪个 Java 版本在报错，最稳妥、最标准的运维策略是：

> 👉 将证书依次安装到**所有**排查出来的 JDK 版本的 cacerts 库中。

因为各版本的 keytool 安装证书是相互独立的，把证书同时写进 Java 8、Java 11 和 Java 17 的信任库，完全不会对系统产生任何负面影响或冲突，反而能彻底杜绝因版本"看错"导致的重复排查。

---

## 📥 第三部分：使用 keytool 安装证书（全版本路径参考）

确定好 JDK 路径和规范后的证书文件后，即可进行安装。以下列出不同版本的标准信任库路径及对应的导入命令。

### 1. 各版本标准路径速查

| JDK 版本 | 64位 |
|----------|------|
| **Java 8** | 信任库路径：`/QOpenSys/QIBM/ProdData/JavaVM/jdk80/64bit/jre/lib/security/cacerts`<br>工具路径：`/QOpenSys/QIBM/ProdData/JavaVM/jdk80/64bit/jre/bin/keytool` |
| **Java 11** | 信任库路径：`/QOpenSys/QIBM/ProdData/JavaVM/jdk11/64bit/lib/security/cacerts`<br>工具路径：`/QOpenSys/QIBM/ProdData/JavaVM/jdk11/64bit/bin/keytool` |
| **Java 17** | 信任库路径：`/QOpenSys/QIBM/ProdData/JavaVM/jdk17/64bit/lib/security/cacerts`<br>工具路径：`/QOpenSys/QIBM/ProdData/JavaVM/jdk17/64bit/bin/keytool` |

### 2. 标准导入步骤（以 Java 8 为例）

1. 在 AS400 命令行输入 `STRQSH` 进入 Qshell。
2. 执行以下导入命令（根据实际情况修改 `-alias` 别名和 `-file` 路径）：

```sh
/QOpenSys/QIBM/ProdData/JavaVM/jdk80/64bit/jre/bin/keytool -import -trustcacerts \
  -keystore /QOpenSys/QIBM/ProdData/JavaVM/jdk80/64bit/jre/lib/security/cacerts \
  -storepass changeit \
  -alias my_ext_api \
  -file /tmp/standard_server.cer
```

**关键参数解析：**

| 参数 | 说明 |
|------|------|
| `-keystore` | 排查出的目标安全信任库 cacerts 文件的绝对路径。 |
| `-storepass changeit` | IBM JDK 默认的信任库密码，通常是固定的 `changeit`（小写）。 |
| `-alias my_ext_api` | 给该证书在库里定义一个唯一别名，不能与库内已有的别名重复。 |
| `-file` | 规范化后的 `.cer` 文件的绝对路径。 |

3. 确认信任：命令回车后，系统会打印出证书的详细所有人信息，并提示：

   ```
   Trust this certificate? [no]:
   ```

   请输入 `yes` 并按回车。

4. 界面打印出 `Certificate was added to keystore`，说明顺利写入。

---

## 🔒 第四部分：安全备份与自动化安装 Shell 脚本

为了防止手动敲命令出错，并确保生产环境的安全，推荐使用自动化脚本。该脚本会自动完成 **"自动备份旧信任库" → "免交互自动安装" → "自动指纹验证"** 的全套标准流程。

### 自动化脚本源码 (install_cert.sh)

您可以将以下文本复制并保存为电脑上的 `install_cert.sh`，通过 ACS 上传到 AS400 的 `/tmp` 目录下：

```bash
#!/usr/bin/env bash
# ==============================================================================
# 脚本名称: install_cert.sh
# 适用系统: IBM i (AS400) Qshell 环境
# 脚本功能: 自动化备份 Java 信任库、安全导入新证书、并自动执行验证
# ==============================================================================

# --------- 1. 配置区域 (根据实际情况修改) ---------
# 目标 JDK 路径 (如果需要安装到多个版本，可多次修改此路径并重复运行脚本)
TARGET_JDK="/QOpenSys/QIBM/ProdData/JavaVM/jdk80/64bit"
# 规范化后的证书在 AS400 上的临时存放路径
CERT_FILE="/tmp/standard_server.cer"
# 证书别名 (必须唯一)
ALIAS_NAME="my_ext_api_2026"
# 信任库默认密码
STORE_PWD="changeit"

CACERTS_PATH="${TARGET_JDK}/jre/lib/security/cacerts"
KEYTOOL_BIN="${TARGET_JDK}/jre/bin/keytool"
# 如果是 Java 11/17，cacerts 路径和工具路径可能没有 /jre/ 目录级
if [ ! -f "$CACERTS_PATH" ]; then
    CACERTS_PATH="${TARGET_JDK}/lib/security/cacerts"
    KEYTOOL_BIN="${TARGET_JDK}/bin/keytool"
fi

echo "=================================================="
echo "      AS400 Java 证书自动化安装与验证脚本"
echo "=================================================="

# --------- 2. 环境检查 ---------
if [ ! -f "$CERT_FILE" ]; then
    echo "❌ 错误: 在 $CERT_FILE 找不到上传的证书文件！请检查第一步是否成功。"
    exit 1
fi
if [ ! -f "$KEYTOOL_BIN" ]; then
    echo "❌ 错误: 找不到 keytool 工具，请检查 TARGET_JDK 路径是否正确。"
    exit 1
fi

# --------- 3. 自动备份 (安全第一) ---------
BACKUP_PATH="${CACERTS_PATH}.bak_$(date +%Y%m%d%H%M%S)"
echo "🔄 [1/4] 正在备份原有的 cacerts 信任库..."
cp "$CACERTS_PATH" "$BACKUP_PATH"
if [ $? -eq 0 ]; then
    echo "   ✅ 备份成功! 备份文件存放在: $BACKUP_PATH"
else
    echo "   ❌ 备份失败! 请检查当前用户是否有权限操作此 JDK 目录。"
    exit 1
fi

# --------- 4. 自动化导入 ---------
echo "📥 [2/4] 正在将证书导入到 Java 信任库..."
# 使用 echo yes 实现免人工交互自动确认
echo "yes" | $KEYTOOL_BIN -import -trustcacerts \
    -keystore "$CACERTS_PATH" \
    -storepass "$STORE_PWD" \
    -alias "$ALIAS_NAME" \
    -file "$CERT_FILE" \
    -noprompt

if [ $? -eq 0 ]; then
    echo "   ✅ 证书成功写入库中！"
else
    echo "   ❌ 导入失败！可能原因：编码仍损坏、别名重复或密码错误。"
    echo "   🔄 正在还原备份..."
    mv "$BACKUP_PATH" "$CACERTS_PATH"
    exit 1
fi

# --------- 5. 自动验证 ---------
echo "🔍 [3/4] 正在执行库内证书验证..."
$KEYTOOL_BIN -list -keystore "$CACERTS_PATH" -storepass "$STORE_PWD" -alias "$ALIAS_NAME" > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "   ✅ [验证通过] 证书已成功在库中激活！别名为: $ALIAS_NAME"
    echo "=================================================="
    echo "🚀 [4/4] 最终步骤提醒："
    echo "    证书已生效。请务必重启 AS400 上对应的 Java 业务应用/子系统！"
    echo "=================================================="
else
    echo "   ❌ [验证失败] 库中未检索到刚安装的别名，请检查错误日志。"
fi
```

### 运行脚本方法

1. 进入 AS400 绿屏幕，执行 `STRQSH` 进入命令行。
2. 赋予脚本执行权限并运行：

```sh
chmod 755 /tmp/install_cert.sh
/tmp/install_cert.sh
```

---

## ✅ 第五部分：如何验证是否安装成功与正式生效

### 验证方法 1：通过 keytool 逆向检索（命令行验证）

在 Qshell 中运行以下命令，直接去信任库中根据你定义的**别名（alias）**检索该证书：

```sh
/QOpenSys/QIBM/ProdData/JavaVM/jdk80/64bit/jre/bin/keytool -list \
  -keystore /QOpenSys/QIBM/ProdData/JavaVM/jdk80/64bit/jre/lib/security/cacerts \
  -storepass changeit \
  -alias my_ext_api_2026
```

- **安装成功表现**：系统会正确打印出该别名、证书类型（如 `trustedCertEntry`）、证书的 MD5/SHA 校验指纹以及导入时间。
- **安装失败表现**：如果提示 `Alias <xxx> does not exist`，说明证书未真正写入该库。

### 验证方法 2：业务生效情况验证

1. **必须重启 Java 宿主应用**：Java 虚拟机（JVM）为了提高性能，只会在启动时加载一次 cacerts 信任库。因此，即便 keytool 提示成功，如果不重启应用，内存中依旧是旧的信任库。
2. **执行重启**：根据业务需要，重启运行在 AS400 上的 Java 业务系统、WebSphere 实例、Tomcat 容器或对应的子系统作业。
3. **观察业务日志**：重启后，再次触发原本报错的外部接口调用任务。如果原本报的 `javax.net.ssl.SSLHandshakeException` 或编码错误彻底消失，且数据能正常加密交互，则证明证书完全安装成功并正式生效。

---

> **文档版本**: v1.0  
> **适用范围**: IBM i (AS400) 所有版本  
> **维护团队**: RX 系统运维组
