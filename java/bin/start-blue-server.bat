@echo off
rem ----------------------------------------------------
rem - Start Blue Server bat file version 0.1
rem - Comment and Suggestions to Rob.Blake@arjuna.com
rem ----------------------------------------------------

set SCHEDULING_INFO=0
set VERIFY_CONFIG=0
set CONFIG_FILE=..\etc\blue.cfg

rem Process any arguments

rem Process first argument to see if it is empty

if not ""%1"" == """" goto processArguments
goto usage

:processArguments

if ""%1"" == """" goto endArguments
if ""%1"" == ""-h"" goto usage
if ""%1"" == ""-s"" set SCHEDULING_INFO=1
if ""%1"" == ""-v"" set VERIFY_CONFIG=1
if ""%1"" == ""-c"" goto setConfig

shift
goto processArguments

:setConfig

shift
set CONFIG_FILE=%1
shift
goto :processArguments

:endArguments

cd ..
rem - Check for existence of current Blue lockfile
if exist var\blue.lock goto warningExit

if %SCHEDULING_INFO% == 1 goto schedulingInfo
if %VERIFY_CONFIG% == 1 goto verifyConfig

java -jar blue-server.jar %CONFIG_FILE%

goto end

:verifyConfig

java -jar blue-server.jar -v %CONFIG_FILE%
goto end

:schedulingInfo

java -jar blue-server.jar -s %CONFIG_FILE%
goto end

:usage

echo Usage of the Blue Server start-up script:
echo.
echo This script is used to launch the Blue Server. You should use this
echo script should you wish to manage your Blue Server seperately from
echo Blue Console and Blue Configuration Tool. If you require a single
echo method of launching Blue Server, Blue Console and Blue Configuration
echo Tool, please refer to start-blue.bat in this directory.	
echo.	
echo Options:
echo.
echo -c: Using this option in conjunction with the -s option will cause
echo      the Blue Server to be launched with the configuration file at
echo      the specified path. e.g
echo      start-blue-server.bat -c /var/tmp/blue/blue.cfg
echo. 
echo -v: Use Blue to verify your configuration files. Should be used in
echo      conjunction with -c if your configuration file is not in the
echo      default location.
echo.
echo      start-blue-server.bat -v -c config_file
echo.
echo -s: Use Blue to show projected/recommended check scheduling information
echo      Should be used in conjunction with the -c option if your configuration
echo      file is not in the default location
echo.
echo      start-blue-server.bat -s -c config_file
echo.
echo -h: Display this help information.
echo.

goto end

:warningExit

echo There appears to be another version of Blue already running. Aborting launch.
goto end

:end