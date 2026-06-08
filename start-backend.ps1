# 强制 UTF-8 编码，绕过 PowerShell 管道编码问题
chcp 65001 > $null

Set-Location D:\vueprojects\RX

# 使用 cmd /c 确保编码一致，避免 PowerShell 管道转换时乱码
cmd /c "chcp 65001 > nul && mvn spring-boot:run -Dspring-boot.run.profiles=local -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8"
