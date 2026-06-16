# RX Admin 前端启动脚本（本地开发）
# 环境变量在 ui/.env.development 中配置

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location (Join-Path $ProjectRoot ui)

Write-Host "启动前端开发服务器 → http://localhost:3000" -ForegroundColor Cyan
Write-Host "API 代理 → http://localhost:8088" -ForegroundColor Cyan
npm run dev
