@echo off
cd /d "%~dp0"
where python >nul 2>nul
if errorlevel 1 (
    py -3 server.py
) else (
    python server.py
)
pause
