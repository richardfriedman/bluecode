Welcome to Blue Star, a Java Port of .

This document will get you started with your Blue Monitoring setup.

Contents:
----------
0: Installing Blue
1: Running Blue
2: Blue Components
3: Configuration files for Blue
4: Blue Plugins
5: Using Nagios Plugins with Blue
6: Further Help


0: Installing Blue:
-------------------

With your download in place there is no further work required in installing Blue itself. You can install the blue directory where you wish, however you should not alter the directory structure that comes with the main distribution.

One other requirement of Blue is that you have the Java Runtime Environment (JRE) installed on your machine. If you do not already have the JRE installed, then please visit http://java.sun.com and download the latest version. There is a wide range of information available on the above site on how to install and configure the JRE, therefore this document will not describe the process further.

It should be noted that with this release, Blue only currently supports JRE 1.5 and above.

If you wish to use the existing Nagios plugins with Blue, please refer to section 5 of this document. 

1: Running Blue:
----------------
	
There are several options available to you when running Blue. One way of running Blue is to use the start-up scripts provided in the ~/bin directory of this download. start-blue.sh (unix users) or start-blue.bat (windows users) will get Blue and the Blue Console running for you. 

The startup script supports a few command line arguments to alter the way in which it runs.

Without any arguments the startup script will start the Blue Console and the Blue Configuration Tool only. This allows you to manage the different components as seperate entities, or run Blue in a headless manner should you want to.

Using the -s option will cause the startup script to launch the Blue server as well.

Using -s in conjunction with -i will cause Blue to install an extremely basic configuration for you. This will get you up and running and monitoring your first host in no time. 

Using the -n option assumes that you have your Nagios plugins installed in /usr/local/nagios/libexec. If this is not the case then you must use the -p option to override this setting. For example should my plug-ins reside in /usr/local/nagios-plugins, I would start the server in the following way:

<BLUE_HOME>/bin/start-blue.sh -s -i -p /usr/local/nagios-plugins for Unix Users
or
<BLUE_HOME>/bin/start-blue.bat -s -i -p \Program Files\nagios-plugins for Windows Users.

The final option you can use with the startup script is -c. You can use this should you wish to use an already existing configuration (Perhaps you already have Nagios and are looking to try Blue). The -c option can be used in conjunction with the -s option to launch the Blue server with an alternative configuration file i.e.

<BLUE_HOME>/bin/start-blue.sh -s -c /usr/local/nagios/etc/nagios.cfg for Unix Users
or
<BLUE_HOME>/bin/start-blue.bat -s -c \nagios\nagios.cfg for Windows Users


1.1: Running Blue outside of the startup script.
----------------------------------------------

There are several ways you can launch Blue outside of the startup script and these are available to suite your own needs.

Firstly you can call the .jar files directly from the command line. Using the following commands you can get the Blue Components up and running.

1.1.2: To start the Blue Server
-------------------------------

 java -jar blue-server.jar <options> <path_to_config>

 You can also use the -v switch with the above command to verify the contents of any configuration file you are intending to use. You can also use the -s switch to display current Blue scheduling information and recommendations.

 There is also a wrapper script for starting the blue server titled start-blue-server.sh/.bat. This performs the same functionality as above 

1.1.3: To start the Blue Console
--------------------------------

  java -jar blue-console.jar

 This will start the Blue console at http://localhost:8080/blue and the Blue Configuration Tool at http://localhost:8080/blue-config should you have it installed. It will also provide you with http://localhost:8080/welcome which contains information similar to this document.
 
 Also available within this release of Blue is the experimental ability to Launch the Blue Console as a single process within Blue. To do this use Blue with the -c option.


2: Blue Components:
-------------------

There are several components to the Blue monitoring solution of which you should be aware. The first of these components is blue-server.jar which resides within the $BLUE_HOME/server directory.

2.1: Blue Server:
-----------------

 The blue-server.jar encapsulates all of the monitoring functionality that is found within Nagios. It performs monitoring of Hosts, the Services on these Hosts and also applies Notification and Event Handling logic. As discussed above you can run the Blue Server in a headless manner meaning that you do not use the Blue Console to view the output of the monitoring process. Running Blue in a headless manner will still allow all notifications to be received and event handling to be properly dealt with.

2.2: Blue Console:
------------------

Blue Console should be seen as a way to view your current Blue monitoring situation. It allows you to gather feedback on which Services (if any) are in a problem state. Through the Blue Console it is also possible to add comment data to each Host and Service, schedule downtime of your Hosts and Services, commit commands to the Blue external command file and other such operations. It also provides a graphical layout of your current monitoring situation.

The Blue Console is automatically deployed when running the <BLUE_HOME>/bin/start-blue script. Blue Console includes an embedded version of the popular Web Server Jetty (currently version 6.1.1), so there is no need to configure any other form of Web Server to run Blue. Upon successful start-up of the Blue Console, you will be able to access it at http://localhost:8080/blue.

2.3: Blue Config Tool:
----------------------

The Blue Config Tool is an application that is designed to help you create and manage your current Blue configuration. It tries to take you away from needing to edit text files by hand and instead provides you with a GUI interface to generate the files that are used by Blue to complete your monitoring solution. Should you have it installed, the Config Tool will be available to you at http://localhost:8080/config. It alllows you to specify a directory into which it will place the configuration files needed to start Blue. We suggest that you use the $BLUE_HOME/config directory to store your configuration files. 

The Blue Config Tool also comes with it's own set of documentation which is available once the tool has started.

3: Blue Configuration Files:
----------------------------

Blue Configuration files do not differ in any way from Nagios configuration files. Blue has support for all options that are available within the Nagios framework. Object definition rules within the Nagios Framework also apply to the Blue framework.

It is possible to use your existing Nagios configuration files with Blue. When starting the Blue Server, you simply need to identify the location of your Nagios configuration. If using the start-up scripts this can be achieved with the -c option, if running the blue-server.jar directly, simply use 

	java -jar <BLUE_HOME_DIRECTORY>/blue-server.jar <location_of_nagios_config>

As previously discussed there is a beta version of the Blue Configuration Tool available. This is available at http://localhost:8080/blue-config should you have it installed. Through this tool it is possible to generate the configuration files needed to create your monitoring solution.

4: Blue Plugins:
------------------

Blue comes with a selection of Java plugins available. These are Java ports of many of the popular plugins that are associated with Nagios. The available plugins live within the $BLUE_HOME/plugins directory. To execute any of the Blue plugins, simply use the following java -jar <plugin_name> <options>. For more help on how to use each of the plugins, simply use the above command with the -h switch as your only option. This will cause the plugin to display usage help on the command line.

To include the Java plugins in your monitoring setup, we suggest defining a second macro. The macro $USER2$ should have a value of $BLUE_HOME/plugins. With this macro defined you can add the Java plugins as you would any other command within the Blue framework. An example here for using the blue-check-local-time plugin:

define command{
	command_name	check_local_time
	command_line	java -jar $USER2$/blue-check-local-time.jar -H europe.pool.ntp.org -w 20 -c 40
}

You can then continue to define Services using this command as you would normally within Nagios.

If you use the generated configuration provided by the startup scripts, then this macro is automatically defined for you.


5: Using Nagios Plugins with Blue:
----------------------------------

It is possible to use the existing Nagios plugins with Blue. In order to do this you must have them compiled for the system on which you intend to use them. If you already have the Nagios plugins downloaded and compiled, then you can use them in conjunction with Blue as you would with Nagios. If you do not have the plugins already available then you will first need to download them from the Nagiossite at http://www.nagios.org.

With the plugins downloaded, there is excellent documentation for how to compile them for your individual system contained within the download itself. As the process of compiling the plugins can be tailored for individual systems, it is not within the scope of this document to provide a walk through on how to install the plugins. 

For more information on how to compile the Nagios plugins for Windows, please see http://localhost:8080/welcome

6: Further Help:
-----------------

6.1 Some common errors:
-----------------------

6.1.2: When attempting to run the java -jar command i receive the following error
	java: command not found (*nix systems) 

	java is not recognized as an internal or external command, operable program or batch file. (Windows systems)

This is caused by the java executable not being on your path. For information onplacing the Java executable onto your path, please see the following links:

Windows: http://www.computerhope.com/issues/ch000549.htm
*nix: http://kb.iu.edu/data/acar.html 
 
6.1.3: When attempting to start the Blue Console I receive java.lang.ClassNotFoundException: org.mortbay.xml.XmlConfiguration.

This is caused by the required libraries for Jetty not being on your Classpath. By default Jetty will check for the presence of these libraries within the ~lib directory relative to where the blue-console.jar is installed. To resolve the above issue you can perform one of the following:

- run the blue-console from within the <BLUE_HOME> directory thus allowing Jetty to find the required .jar files from the ~lib directory

- add the jetty.jar and jetty-utils.jar jar files to your classpath.

For further help visit the Blue website at http://blue.sourceforge.net.

