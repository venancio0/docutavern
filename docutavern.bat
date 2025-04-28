@echo off

REM Find the directory where this script resides
SET SCRIPT_DIR=%~dp0
REM Remove trailing backslash if present
IF %SCRIPT_DIR:~-1%==\ SET SCRIPT_DIR=%SCRIPT_DIR:~0,-1%

REM Assume the JAR is in a 'lib' subdirectory relative to the script
SET CLI_JAR_PATH=%SCRIPT_DIR%\lib\docutavern-cli-0.1.0-SNAPSHOT.jar
REM Adjust version/path as needed

REM Check if JAR exists
if not exist "%CLI_JAR_PATH%" (
  echo [ERROR] Docutavern CLI JAR not found at %CLI_JAR_PATH%
  echo Please ensure Docutavern is installed correctly.
  exit /b 1
)

REM Get the User's Current Working Directory
SET USER_CWD=%CD%

REM Execute the Java CLI, passing the CWD and all other arguments
java -jar "%CLI_JAR_PATH%" --cwd "%USER_CWD%" %*