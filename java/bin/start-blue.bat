@echo off
rem ----------------------------------------------------------------------------------------------
rem - Version 0.1 Beta - Comments and Suggestions to Rob.blake@arjuna.com
rem -
rem - This script is a lot less effective than it's Unix counterpart, mainly due to the lack
rem - of functionality within DOS.
rem -
rem - Launch script for Blue-server & Blue-console. This script will automatically launch
rem - blue-console which includes the servlet interface to Blue, and the config tool should
rem - the user have it installed. If the user has the config tool installed, this script will put
rem - in place a default location for the output of configuration files. The user does have the
rem - option to override this, but for the time being we will advise against this.
rem - 
rem - The user also has the option to start Blue-server by providing the location of a config file 
rem - through the use of -c in combination with the -s option. Should the user wish to use an
rem - extremely basic config that monitors a single service on localhost (just to get them into
rem - the swing of things), they can use the -n option.
rem -----------------------------------------------------------------------------------------------

echo Welcome to Blue Star, a Java Port of .
echo For more information, visit http://localhost:8080/welcome once the console has loaded.
echo.

setlocal
set START_SERVER=0
set COPY_CONFIG=0
set BASEDIR=%cd%
set CONFIG_FILE=etc\blue.cfg
set PLUGINS=""
set CHANGE_PLUGINS=0
set SERVER_RUNNING=0
set CONSOLE_RUNNING=0

rem - Process the rest of the command line

:processArguments
if ""%1"" == """" goto endArguments
if ""%1"" == ""-h"" goto usage
if ""%1"" == ""-s"" set START_SERVER=1
if ""%1"" == ""-c"" goto setConfig
if ""%1"" == ""-i"" set COPY_CONFIG=1
if ""%1"" == ""-p"" goto setPlugins
shift

goto processArguments

:setConfig
shift
set CONFIG_FILE=%1
shift
goto processArguments

:setPlugins
set CHANGE_PLUGINS=1
shift
set PLUGINS=%1
shift
goto processArguments

:endArguments

rem - MAIN WORKINGS OF FILE -

if %START_SERVER% == 1 goto serverRunning
:endServerRunning

if %COPY_CONFIG% == 1 goto copyLaunch

if %START_SERVER% == 1 start "Blue Server - Java Port of Nagios 2.7" call start-blue-server.bat -c %CONFIG_FILE%

:copyLaunch
cd ..
if %START_SERVER% == 1 start "Blue Server - Java Port of Nagios 2.7" call java -jar blue-server.jar -i -p %PLUGINS%

rem - Give the server a chance to launch so that we can check for presence of a lock file
rem - No real alternative way of doing this in windows unfortunately.
if %START_SERVER% == 1 goto launchConsole

echo Attempting to launch Blue Console.
java -jar blue-console.jar

rem - END MAIN WORKINGS OF FILE -
goto end

rem --------------------------------------
rem - **USAGE INFORMATION**
:usage

echo Usage of the Blue start-up script:
echo.
echo This script can be used to launch the Blue server is a variety of ways.
echo Using the options below, it is possible for you to include a very basic configuration
echo so that you can get your Blue monitoring up and running extremely quickly.
echo You can also 
echo for details on starting your Blue Server.
echo.	
echo Options:
echo.
echo -s : Using this option causes the startup script to attempt to launch the Blue Server.
echo      Please note that the Server will be launched in another Window. For information on
echo      using a basic configuration and altering the location of your Nagios plugins, please
echo      see options -n and -p.
echo.
echo -c : Using this option will tell the Blue framework the location of your main configuration
echo      file. The startup script will default to a location of %BASEDIR%\config\blue.cfg. If 
echo      this is not the case then it can be overridden using the -c option e.g
echo.     
echo 	  start-blue.bat -s -c /var/tmp/blue/blue.cfg
echo.
echo -i : Using this option will provide you with a very basic monitoring
echo      setup. This can be used to get a flavour of how Blue works and
echo      how to manage your monitoring setup. The configuration is placed
echo      into the $BLUE_HOME/config directory. This option has a safety
echo      mechanism whereby it will not run should a configuration file
echo      already be present in the above location. 
echo      This option also assumes your Nagios plug-ins are installed in the
echo      following location:
echo      /usr/local/nagios/libexec
echo. 
echo      If this IS NOT the case, please refer to the -p option below
echo.
echo -p : Used to specify the location of your Nagios plug-ins directory in
echo      conjunction with the -n option. e.g:
echo      start-blue.bat -n -p /var/tmp/blue/plug-ins
echo.	
echo -h : Display this help information.
goto end
rem ---------------------------------------

rem ----------------------------------------
rem - ** CHECK TO SEE IF THE SERVER IS ALREADY RUNNING ** -
:serverRunning

if exist ..\var\blue.lock goto lockWarning

goto endServerRunning
rem ----------------------------------------

rem ---------------------------------------
rem - ** LAUNCH THE BLUE CONSOLE **
:launchConsole
echo Attempting to Launch the Blue Console.
ping -n 3 localhost > nul
if not exist var\blue.lock goto serverProblem

java -jar blue-console.jar
goto end
rem ---------------------------------------

rem ----------------------------------------
rem - ** LOCK FILE WARNING **
:lockWarning
echo The presence of a lock file indicates that there is already another version of Blue running. Please verify.
goto end
rem ----------------------------------------

rem-----------------------------------------
rem ** Error if there was a problem launching the Server **
:serverProblem
echo There appears to have been a problem launching the Blue Server component.
echo Please verify that everything is in place and try again.
goto end
rem ----------------------------------------
:end
pause
