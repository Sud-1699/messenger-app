@echo off
setlocal
:: Get list of installed wsl distro to run
wsl -l --all
:: Prompt the user for the new nodejs directory path
set /p distroName="Enter the distro name to run: "
if "%distroName%"=="^C" (
    exit /b 1
)
:: Check if the distroName is not empty
if "%distroName%"=="" (
    echo You must enter a distro name.
    exit /b 1
)
echo Entering into %distroName%
wsl -d %distroName%

endlocal