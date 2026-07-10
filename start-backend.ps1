# RX Admin 后端启动脚本（本地开发）
# 使用 application-local.yml（已 gitignore）配置数据库/邮件等凭据
# 如不存在，可从 application-local.template.yml 复制后修改

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location -LiteralPath $ProjectRoot

if (-not (Test-Path "$ProjectRoot\src\main\resources\application-local.yml")) {
  Write-Host "⚠ 未找到 application-local.yml" -ForegroundColor Yellow
  Write-Host "  从模板复制: cp src\main\resources\application-local.template.yml src\main\resources\application-local.yml" -ForegroundColor Cyan
}

# 也可通过环境变量覆盖（优先级高于 application-local.yml）：
# $env:MYSQL_PASSWORD = "your_password"
# $env:MAIL_PASSWORD = "your_mail_auth_code"
# $env:WHISPERX_API_KEY = "your_key"

# SkyWalking Agent（可选，设置 SW_HOME 启用）
$swAgent = ""
if ($env:SW_HOME) {
    $agentPath = "$env:SW_HOME\agent\skywalking-agent.jar"
    if (Test-Path $agentPath) {
        $swAgent = "-javaagent:$agentPath"
        Write-Host "✓ SkyWalking Agent 已启用: $agentPath" -ForegroundColor Green
    } else {
        Write-Host "⚠ SkyWalking Agent 未找到: $agentPath" -ForegroundColor Yellow
    }
}

mvn spring-boot:run -Dspring-boot.run.profiles=local "-Dspring-boot.run.jvmArguments=$swAgent -Dfile.encoding=UTF-8"
if ($LASTEXITCODE -ne 0) {
  Write-Host "启动失败，请检查 application-local.yml 配置" -ForegroundColor Red
  Read-Host "Press Enter to exit"
}
