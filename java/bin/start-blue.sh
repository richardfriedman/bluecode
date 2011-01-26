#!/bin/sh
# Version 0.1 Beta - Comments and Suggestions to Rob.blake@arjuna.com
#
#
# Launch script for Blue-server & Blue-console. This script will automatically launch
# blue-console which includes the servlet interface to Blue, and the config tool should
# the user have it installed. If the user has the config tool installed, this script will put
# in place a default location for the output of configuration files. The user does have the
# option to override this, but for the time being we will advise against this.
#
# The user also has the option to start Blue-server by providing the location of a config file 
# through the use of -c in combination with the -s option. Should the user wish to use an
# extremely basic config that monitors a single service on localhost (just to get them into
# the swing of things), they can use the -n option.
#

trap cleanup 1 2 3 6

NO_ARGS=$#
START_SERVER=0
COPY_CONFIG=0
BASEDIR=`/bin/pwd`
CONFIG_FILE=$BASEDIR/etc/blue.cfg
PLUGINS=""
CHANGE_PLUGINS=0
JAVA=`which java`
SERVER_PID=0
SERVER_RUNNING=0
CONSOLE_RUNNING=0

start_console()
{
	echo 'Starting Blue Console and Blue Configuration Tool.'
	$JAVA -jar blue-console.jar
	exit 0
}

cleanup()
{
	if [ $SERVER_PID -ne 0 ]
		then
			
			/bin/kill $SERVER_PID
			sleep 2
	fi
	
	if [ -e var/blue.lock ]
		then
			/bin/rm var/blue.lock
	fi
	
	echo 'Graceful Shutdown complete.'
	exit 1
}

usage()
{
	echo 'Usage of the Blue start-up script:'
	echo ''
	echo 'By default this script will launch the Blue Console & Blue'
	echo 'Configuration tool only. This allows you to manage the Blue'
	echo 'Server and Blue Console as seperate entities. If using the'
	echo 'script in this manner, you should refer to start-blue-server.sh'
	echo 'for details on starting your Blue Server.'
	echo ''	
	echo 'Options:'
	echo ''
  	echo '-s : Using this option causes the startup script to attempt to launch'
	echo '     the Blue Server component. The process is started as a background'
	echo '     process, in effect daemonising the Blue Server. Without the'
	echo '     combined use of the -c option, the Blue Server will default to a'
	echo '     config location of /usr/local/blue/config/blue.cfg.'
	echo ''
	echo '-c : Using this option in conjunction with the -s option will cause'
	echo '     the Blue Server to be launched with the configuration file at'
	echo '     the specified path. e.g:'
	echo '	   ./start-blue.sh -s -c /var/tmp/blue/blue.cfg'
	echo ''
	echo '-i : Using this option will provide you with a very basic monitoring'
	echo '     setup. This can be used to get a flavour of how Blue works and'
	echo '     how to manage your monitoring setup. The configuration is placed'
	echo '     into the $BLUE_HOME/config directory. This option has a safety'
	echo '     mechanism whereby it will not run should a configuration file'
	echo '     already be present in the above location. This option MUST be'
	echo '     used in conjunction with the -s option.'
	echo '     This option also assumes your plug-ins are installed in the'
	echo '     following location:'
	echo '     /usr/local/nagios/libexec'
	echo ''
	echo '     If this IS NOT the case, please refer to the -p option below'
	echo ''
	echo '-p : Used to specify the location of your plug-ins directory in'
	echo '     conjunction with the -s & -n options. e.g:'
	echo '     ./start-blue.sh -s -i -p /var/tmp/blue/plug-ins'
	echo ''	
	echo '-h : Display this help information.'
	
	exit 1
}	

# Check to see if the console is currently running
check_console()
{
	/bin/ps -ef | grep blue-console.jar | grep -v grep > /dev/null 2>&1
	
	if [ $? -eq 0 ]
		then 
			CONSOLE_RUNNING=1
	fi
}

# Check to see if the server is currently running
check_server()
{
	/bin/ps -ef | grep blue-server.jar | grep -v grep > /dev/null 2>&1

	if [ $? -eq 0 ]
		then
			SERVER_RUNNING=1
	fi
}
	
while getopts ":sihc:p:" Option
do
	case $Option in
	s) START_SERVER=1;;
	i) COPY_CONFIG=1;;
	h) usage;;
	c) CONFIG_FILE=$OPTARG;;
	p) PLUGINS=$OPTARG; CHANGE_PLUGINS=1;;
	*) usage;;
	esac
done

shift $(($OPTIND -1))

check_console
check_server

if [ $SERVER_RUNNING -eq 1 ]
	then
		echo 'There appears to be an instance of Blue Server already running.'
		exit 1
fi

if [ $CONSOLE_RUNNING -eq 1 ]
	then
		echo 'There appears to be an instance of Blue Console already running.'
		exit 1
fi

echo 'Welcome to Blue a Java Port of Nagios'
echo 'For more information on getting started, visit http://localhost:8080/welcome'

cd ..

# If no arguments were passed, then we can simply start the Blue Console & Config Tool.

if [ $# -eq "$NO_ARGS" ]
	then
		start_console

fi

if [ $START_SERVER -eq 1 ]
	then
		# Test to see if user requested simple config?
		
		if [ $COPY_CONFIG -eq 1 ]
			then
				$JAVA -jar blue-server.jar -d -i -p $PLUGINS&
			else
				$JAVA -jar blue-server.jar -d $CONFIG_FILE&
		fi
		
		SERVER_PID=$!
		sleep 4

		ps $SERVER_PID > /dev/null 2>&1

		if [ $? -eq 1 ]
			then
				echo 'Problem starting Blue Server, see error messages for details.'
				SERVER_PID=0
				exit 1
		fi	
		
		start_console
		exit 1
fi
