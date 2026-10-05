@echo off
setlocal EnableExtensions

set "RUN_DIR=%~dp0"
if "%RUN_DIR:~-1%"=="\" set "RUN_DIR=%RUN_DIR:~0,-1%"
set "LOG=%TEMP%\freemaf-run.log"

echo ==== run.bat start %DATE% %TIME% ====>>"%LOG%"
echo RUN_DIR=%RUN_DIR%>>"%LOG%"

rem Locate pom.xml (the Maven project directory).
set "BUILD_DIR="
if exist "%RUN_DIR%\pom.xml" set "BUILD_DIR=%RUN_DIR%"
if not defined BUILD_DIR (
    pushd "%RUN_DIR%\.."
    if exist "pom.xml" set "BUILD_DIR=%CD%"
    popd
)
if not defined BUILD_DIR (
    echo [Free MAF Code] pom.xml not found near run.bat.
    echo Checked: %RUN_DIR% and its parent.
    echo pom.xml not found>>"%LOG%"
    pause
    exit /b 1
)
echo BUILD_DIR=%BUILD_DIR%>>"%LOG%"

rem Detect layout: dev (agent subfolder has rules) vs dist (BUILD_DIR itself is the agent folder).
if exist "%BUILD_DIR%\agent\rules" goto :dev_layout
set "AGENT_DIR=%BUILD_DIR%"
pushd "%BUILD_DIR%\.."
set "PROJECT_ROOT=%CD%"
popd
goto :layout_ready

:dev_layout
set "AGENT_DIR=%BUILD_DIR%\agent"
set "PROJECT_ROOT=%BUILD_DIR%"

:layout_ready
set "JAR=%BUILD_DIR%\target\app.jar"
echo AGENT_DIR=%AGENT_DIR%>>"%LOG%"
echo PROJECT_ROOT=%PROJECT_ROOT%>>"%LOG%"
echo JAR=%JAR%>>"%LOG%"

rem Prerequisites.
if not exist "%RUN_DIR%\ensure-prerequisites.ps1" goto :legacy_prereq
if exist "%TEMP%\freemaf-env.bat" del "%TEMP%\freemaf-env.bat" >nul 2>nul
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%RUN_DIR%\ensure-prerequisites.ps1"
if errorlevel 1 goto :prereq_failed
if exist "%TEMP%\freemaf-env.bat" call "%TEMP%\freemaf-env.bat"
goto :prereq_ok

:legacy_prereq
where java >nul 2>nul
if errorlevel 1 (
    echo [Free MAF Code] java not found in PATH.
    pause
    exit /b 1
)
where mvn >nul 2>nul
if errorlevel 1 (
    echo [Free MAF Code] Maven not found in PATH.
    pause
    exit /b 1
)
goto :prereq_ok

:prereq_failed
echo [Free MAF Code] Prerequisites are not satisfied.
pause
exit /b 1

:prereq_ok

rem Verify existing JAR contains the main class; otherwise rebuild.
if not exist "%JAR%" goto :do_build
powershell.exe -NoProfile -Command "try { Add-Type -AssemblyName System.IO.Compression.FileSystem | Out-Null; $z=[System.IO.Compression.ZipFile]::OpenRead('%JAR%'); $ok=$z.Entries | Where-Object { $_.FullName -eq 'com/freemaf/agent/Main.class' }; $z.Dispose(); if ($ok) { exit 0 } else { exit 1 } } catch { exit 1 }"
if not errorlevel 1 goto :run
echo [Free MAF Code] Existing JAR is stale or invalid; rebuilding...

:do_build
echo [Free MAF Code] Building framework, this may take a few minutes...
pushd "%BUILD_DIR%"
call mvn -q -DskipTests package
set "MVN_RC=%ERRORLEVEL%"
popd
if not "%MVN_RC%"=="0" (
    echo [Free MAF Code] Build failed with exit code %MVN_RC%.
    pause
    exit /b 1
)
if not exist "%JAR%" (
    echo [Free MAF Code] Build finished but %JAR% is missing.
    pause
    exit /b 1
)

:run
cd /d "%PROJECT_ROOT%"
echo [Free MAF Code] Starting...
java -jar "%JAR%"
set "JAVA_RC=%ERRORLEVEL%"
if not "%JAVA_RC%"=="0" (
    echo [Free MAF Code] Application exited with code %JAVA_RC%.
    pause
)
exit /b %JAVA_RC%
