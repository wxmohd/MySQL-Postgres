@echo off
setlocal

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot
set MAVEN_HOME=C:\Users\walaa\Downloads\apache-maven-3.9.14-bin\apache-maven-3.9.14
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo Building Spring Boot application...
cd /d "%~dp0"

"%MAVEN_HOME%\bin\mvn.cmd" clean install -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Build successful! Starting application...
echo.
echo Server starting at http://localhost:8080
echo Press Ctrl+C to stop the server
echo.

"%JAVA_HOME%\bin\java.exe" -jar target\db-performance-test-1.0.0.jar

pause
