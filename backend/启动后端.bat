@echo off
echo ============================================
echo   电商数据看板后端 - 启动脚本
echo ============================================
echo.

where mvn >nul 2>nul
if %errorlevel%==0 (
    echo 检测到 Maven，正在编译并启动...
    echo.
    cd /d "%~dp0"
    mvn spring-boot:run
) else (
    echo [提示] 未检测到 Maven 命令
    echo.
    echo 请使用以下方式之一启动：
    echo.
    echo 方式1（推荐）：用 IntelliJ IDEA 打开 backend 文件夹
    echo         - 等待 Maven 依赖下载完成
    echo         - 找到 DashboardApplication.java
    echo         - 点击 main 方法左边的绿色三角运行
    echo.
    echo 方式2：已安装 Maven 但未配置环境变量
    echo         - 请将 mvn.exe 所在目录加入 PATH
    echo.
    echo 启动成功后访问：
    echo   前端: 双击 dashboard\index.html
    echo   API:  http://localhost:8080/api/orders/categories
    echo   Swagger: http://localhost:8080/api/swagger-ui.html
    echo.
    pause
)
