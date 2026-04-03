@echo off
REM Database Performance Test - CLI Runner for Windows
REM This script builds and runs the CLI testing application

setlocal enabledelayedexpansion

echo.
echo ╔════════════════════════════════════════════════════╗
echo ║   Database Performance Test - CLI Testing Tool    ║
echo ╚════════════════════════════════════════════════════╝
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Maven is not installed or not in PATH
    echo Please install Maven or add it to your PATH
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install Java or add it to your PATH
    exit /b 1
)

echo [*] Building the application...
call mvn clean package -q
if errorlevel 1 (
    echo [ERROR] Build failed
    exit /b 1
)

echo [*] Build successful!
echo.
echo [*] Starting CLI testing application...
echo.

if "%1"=="" (
    REM Interactive mode
    java -jar target/db-performance-test-1.0.0.jar
) else (
    REM Command-line mode with arguments
    java -jar target/db-performance-test-1.0.0.jar %*
)

endlocal
