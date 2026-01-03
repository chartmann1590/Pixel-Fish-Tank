@echo off
echo Creating Pixel Fish Tank Promotional Video...
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python is not installed or not in PATH
    echo Please install Python 3.7+ from https://www.python.org/
    pause
    exit /b 1
)

REM Check if FFmpeg is installed
ffmpeg -version >nul 2>&1
if errorlevel 1 (
    echo WARNING: FFmpeg is not installed or not in PATH
    echo Please install FFmpeg from https://ffmpeg.org/download.html
    echo Or use: choco install ffmpeg
    echo.
    echo Continuing anyway, but video creation may fail...
    echo.
)

REM Install requirements
echo Installing Python dependencies...
pip install -r promo/requirements.txt
if errorlevel 1 (
    echo ERROR: Failed to install dependencies
    pause
    exit /b 1
)

REM Run the script
echo.
echo Generating promotional video...
python create_promo_video.py

if errorlevel 1 (
    echo.
    echo ERROR: Video creation failed
    pause
    exit /b 1
)

echo.
echo SUCCESS: Promotional video created!
echo Check the 'promo' folder for the output video.
pause

