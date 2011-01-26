#!/bin/sh
# Version 0.1 Beta - Comments and Suggestions to Rob.blake@arjuna.com
#
# A wrapper script that allows the user to run the Blue Server. The 
# functionality of this script can be emulated using 
# java -jar blue-server.jar <options> <location_of_config_file>
#

NO_ARGS=$#
SERVER_RUNNING=0
VERIFY_CONFIG=0
PRINT_INFO=0
CONFIG_FILE=$BASEDIR/etc/blue.cfg
JAVA=`which java`

usage()
{
	echo 'Usage of the Blue Server start-up script:'
	echo ''
	echo 'This script is used to launch the Blue Server. You should use this'
	echo 'script should you wish to manage your Blue Server seperately from'
	echo 'Blue Console & Blue Configuration Tool. If you require a single '
	echo 'method of launching Blue Server, Blue Console & Blue Configuration'
	echo 'Tool, please refer to start-blue.sh in this directory.'	
	echo ''	
	echo 'Options:'
	echo ''
	echo '-c : Using this option in conjunction with the -s option will cause'
	echo '     the Blue Server to be launched with the configuration file at'
	echo '     the specified path. e.g:'
	echo '	   ./start-server -c /var/tmp/blue/blue.cfg'
	echo ''
	echo '-v : Use Blue to verify your configuration files. Should be used in'
	echo '     conjunction with -c if your configuration file is not in the'
	echo '     default location.'
	echo ''
	echo '	 e.g ./start-server.sh -v -c <config_file>'
	echo ''
	echo '-s : Use Blue to show projected/recommended check scheduling information'
	echo '     Should be used in conjunction with the -c option if your configuration'
	echo '     file is not in the default location'
	echo ''
	echo '   e.g ./start-server.sh -s -c <config_file>'
	echo ''
	echo '-h : Display this help information.'
	echo ''
	
	exit 1
}	

# Check to see if the Server is currently running.

check_server()
{
	/bin/ps -ef | grep blue-server.jar | grep -v grep > /dev/null 2>&1

	if [ $? -eq 0 ]
		then
			SERVER_RUNNING=1
	fi
}
	
while getopts ":sc:v" Option
do
	case $Option in
	s) PRINT_INFO=1;;
	h) usage;;
	c) CONFIG_FILE=$OPTARG;;
	v) VERIFY_CONFIG=1;;	
	*) usage;;
	esac
done

shift $(($OPTIND -1))

check_server

if [ $SERVER_RUNNING -eq 1 ]
	then
		echo 'There appears to be an instance of Blue Server already running.'
		exit 1
fi


echo 'Welcome to Blue a Java Port of Nagios'

cd ..
if [ $NO_ARGS -eq 0 ]
	then
		echo 'Starting Blue Server'
		$JAVA -jar blue-server.jar $CONFIG_FILE
		exit 1
fi

if [ $VERIFY_CONFIG -eq 1 ]
	then
		echo 'Verifying your Configuration files.'
		$JAVA -jar blue-server.jar -v $CONFIG_FILE
		exit 1
fi

if [ $PRINT_INFO -eq 1 ]
	then
		echo 'Blue projected & recommended Scheduling Info:'
		$JAVA -jar blue-server.jar -s $CONFIG_FILE
		exit 1
fi

# If none of the above predicates were fulfilled, simply start the server
# using the supplied config file location.

$JAVA -jar blue-server.jar $CONFIG_FILE
exit 1

		
