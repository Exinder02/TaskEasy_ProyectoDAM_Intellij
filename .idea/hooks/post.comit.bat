@echo off
cd /d "%~dp0.."
git add .
git commit -m "Auto commit desde %COMPUTERNAME% - %DATE% %TIME%"
git push origin main
