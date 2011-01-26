/*****************************************************************************
 *
 * Blue Star, a Java Port of .
 * Last Modified : 3/20/2006
 *
 * Copyright (c) 2006-2007 Richard Friedman (blue@osadvisors.com)
 * License:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 *****************************************************************************/

package org.blue.star.base; 

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.common.comments;
import org.blue.star.common.downtime;
import org.blue.star.common.statusdata;
import org.blue.star.include.blue_h;
import org.blue.star.include.broker_h;
import org.blue.star.include.common_h;
import org.blue.star.include.locations_h;
import org.blue.star.include.objects_h;

public class blue { 
   
   /** Logger instance */
   private static Logger logger = LogManager.getLogger("org.blue.base.blue");
   public static String cn = "org.blue.base.blue"; 
   
   
   public static boolean is_core = false;
   public static String config_file = null;
   public static String log_file = null;
   public static String command_file = null;
   public static String temp_file= null;
   public static String lock_file= null;
   public static String log_archive_path= null;
   public static String p1_file= null;    /**** EMBEDDED PERL ****/
   public static String auth_file= null;  /**** EMBEDDED PERL INTERPRETER AUTH FILE ****/
   public static String nagios_user= null;
   public static String nagios_group= null;
   
   /* macro_x - macros that define the overall config of blue */
   public static String[] macro_x = new String[blue_h.MACRO_X_COUNT];
   public static String[] macro_x_names = new String[blue_h.MACRO_X_COUNT];
   public static String[] macro_argv = new String[blue_h.MAX_COMMAND_ARGUMENTS];
   
   /* macro_user - macros defined by the user within resource.cfg file */
   public static String[] macro_user = new String[blue_h.MAX_USER_MACROS];
   public static String[] macro_contactaddress = new String[objects_h.MAX_CONTACT_ADDRESSES];
   public static String macro_ondemand;
   
   public static String global_host_event_handler= null;
   public static String global_service_event_handler= null;
   
   public static String ocsp_command= null;
   public static String ochp_command= null;
   
   public static String illegal_object_chars= null;
   public static String illegal_output_chars= null;
   
   public static int use_regexp_matches=common_h.FALSE;
   public static int use_true_regexp_matching=common_h.FALSE;
   
   public static int use_syslog=blue_h.DEFAULT_USE_SYSLOG;
   public static int log_notifications=blue_h.DEFAULT_NOTIFICATION_LOGGING;
   public static int log_service_retries=blue_h.DEFAULT_LOG_SERVICE_RETRIES;
   public static int log_host_retries=blue_h.DEFAULT_LOG_HOST_RETRIES;
   public static int log_event_handlers=blue_h.DEFAULT_LOG_EVENT_HANDLERS;
   public static int log_initial_states=blue_h.DEFAULT_LOG_INITIAL_STATES;
   public static int log_external_commands=blue_h.DEFAULT_LOG_EXTERNAL_COMMANDS;
   public static int log_passive_checks=blue_h.DEFAULT_LOG_PASSIVE_CHECKS;
   
   public static long logging_options=0;  // was unsigned
   public static long syslog_options=0; // was unsigned
   
   public static int service_check_timeout=blue_h.DEFAULT_SERVICE_CHECK_TIMEOUT;
   public static int host_check_timeout=blue_h.DEFAULT_HOST_CHECK_TIMEOUT;
   public static int event_handler_timeout=blue_h.DEFAULT_EVENT_HANDLER_TIMEOUT;
   public static int notification_timeout=blue_h.DEFAULT_NOTIFICATION_TIMEOUT;
   public static int ocsp_timeout=blue_h.DEFAULT_OCSP_TIMEOUT;
   public static int ochp_timeout=blue_h.DEFAULT_OCHP_TIMEOUT;
   
   public static double sleep_time=blue_h.DEFAULT_SLEEP_TIME;
   public static int interval_length=blue_h.DEFAULT_INTERVAL_LENGTH;
   public static int service_inter_check_delay_method=blue_h.ICD_SMART;
   public static int host_inter_check_delay_method=blue_h.ICD_SMART;
   public static int service_interleave_factor_method=blue_h.ILF_SMART;
   public static int max_host_check_spread=blue_h.DEFAULT_HOST_CHECK_SPREAD;
   public static int max_service_check_spread=blue_h.DEFAULT_SERVICE_CHECK_SPREAD;
   
   public static int command_check_interval=blue_h.DEFAULT_COMMAND_CHECK_INTERVAL;
   public static int service_check_reaper_interval=blue_h.DEFAULT_SERVICE_REAPER_INTERVAL;
   public static int max_check_reaper_time=blue_h.DEFAULT_MAX_REAPER_TIME;
   public static int service_freshness_check_interval=blue_h.DEFAULT_FRESHNESS_CHECK_INTERVAL;
   public static int host_freshness_check_interval=blue_h.DEFAULT_FRESHNESS_CHECK_INTERVAL;
   public static int auto_rescheduling_interval=blue_h.DEFAULT_AUTO_RESCHEDULING_INTERVAL;
   
   public static int non_parallelized_check_running=common_h.FALSE;
   
   public static int check_external_commands=blue_h.DEFAULT_CHECK_EXTERNAL_COMMANDS;
   public static int check_orphaned_services=blue_h.DEFAULT_CHECK_ORPHANED_SERVICES;
   public static int check_service_freshness=blue_h.DEFAULT_CHECK_SERVICE_FRESHNESS;
   public static int check_host_freshness=blue_h.DEFAULT_CHECK_HOST_FRESHNESS;
   public static int auto_reschedule_checks=blue_h.DEFAULT_AUTO_RESCHEDULE_CHECKS;
   public static int auto_rescheduling_window=blue_h.DEFAULT_AUTO_RESCHEDULING_WINDOW;
   
   public static long last_command_check=0L;
   public static long last_command_status_update=0L;  // UPDATE 2.2
   public static long last_log_rotation=0L; 
   
   public static int use_aggressive_host_checking=blue_h.DEFAULT_AGGRESSIVE_HOST_CHECKING;
   
   public static int soft_state_dependencies=common_h.FALSE;
   
   public static int retain_state_information=common_h.FALSE;
   public static int retention_update_interval=blue_h.DEFAULT_RETENTION_UPDATE_INTERVAL;
   public static int use_retained_program_state=common_h.TRUE;
   public static int use_retained_scheduling_info=common_h.FALSE;
   public static int retention_scheduling_horizon=blue_h.DEFAULT_RETENTION_SCHEDULING_HORIZON;
   public static long modified_host_process_attributes=common_h.MODATTR_NONE; // was unsigned
   public static long modified_service_process_attributes=common_h.MODATTR_NONE; // was unsigned
   
   public static int log_rotation_method=common_h.LOG_ROTATION_NONE;
   
   public static int sigshutdown=common_h.FALSE;
   public static int sigrestart=common_h.FALSE;
   
   public static int restarting=common_h.FALSE;
   
   public static int verify_config=common_h.FALSE;
   public static int test_scheduling=common_h.FALSE;
   
   public static int daemon_mode=common_h.FALSE;
   public static int daemon_dumps_core=common_h.TRUE;
   
   /* RXF Added capability to embed the web server and start ONE process */
   public static boolean console_mode = false;
   /* Are we to copy in a basic configuration & what is the location of the Nagios plugins?*/
   public static boolean initialise_config = false;
   /* Have to guess if the user doesn't tell us */
   public static String nagios_plugins = "/usr/local/nagios/libexec";
   
// public static int[] ipc_pipe = new int[2];
   public static Pipe ipc_pipe = null;
   public static LinkedBlockingQueue ipc_queue = new LinkedBlockingQueue();
   
   public static int max_parallel_service_checks=blue_h.DEFAULT_MAX_PARALLEL_SERVICE_CHECKS;
   public static int currently_running_service_checks=0;
   
   public static long program_start=0L; // was time_t
   public static int blue_pid=0;
   public static int enable_notifications=common_h.TRUE;
   public static int execute_service_checks=common_h.TRUE;
   public static int accept_passive_service_checks=common_h.TRUE;
   public static int execute_host_checks=common_h.TRUE;
   public static int accept_passive_host_checks=common_h.TRUE;
   public static int enable_event_handlers=common_h.TRUE;
   public static int obsess_over_services=common_h.FALSE;
   public static int obsess_over_hosts=common_h.FALSE;
   public static int enable_failure_prediction=common_h.TRUE;
   
   public static int aggregate_status_updates=common_h.TRUE;
   public static int status_update_interval=blue_h.DEFAULT_STATUS_UPDATE_INTERVAL;
   
   public static int time_change_threshold=blue_h.DEFAULT_TIME_CHANGE_THRESHOLD;
   
   public static long event_broker_options=broker_h.BROKER_NOTHING;
   
   public static int process_performance_data=blue_h.DEFAULT_PROCESS_PERFORMANCE_DATA;
   
   public static int enable_flap_detection=blue_h.DEFAULT_ENABLE_FLAP_DETECTION;
   
   public static double low_service_flap_threshold=blue_h.DEFAULT_LOW_SERVICE_FLAP_THRESHOLD;
   public static double high_service_flap_threshold=blue_h.DEFAULT_HIGH_SERVICE_FLAP_THRESHOLD;
   public static double low_host_flap_threshold=blue_h.DEFAULT_LOW_HOST_FLAP_THRESHOLD;
   public static double high_host_flap_threshold=blue_h.DEFAULT_HIGH_HOST_FLAP_THRESHOLD;
   
   public static int date_format=common_h.DATE_FORMAT_US;
   
   public static FileChannel command_file_channel;    
   public static int command_file_created= common_h.FALSE;
   
   public static ArrayList notification_list = new ArrayList(); // <notification>  
   
   public static blue_h.service_message svc_msg = new blue_h.service_message();
   
   public static Thread[] worker_threads = new Thread[blue_h.TOTAL_WORKER_THREADS];
   
//   /** added with 2.7 **/
//   public static int external_command_buffer_slots = blue_h.DEFAULT_EXTERNAL_COMMAND_BUFFER_SLOTS;
//   public static int check_result_buffer_slots = blue_h.DEFAULT_CHECK_RESULT_BUFFER_SLOTS;

   public static blue_h.circular_buffer  external_command_buffer = new blue_h.circular_buffer( blue_h.COMMAND_BUFFER_SLOTS );
   public static blue_h.circular_buffer  service_result_buffer = new blue_h.circular_buffer( blue_h.SERVICE_BUFFER_SLOTS );
   
   public static FileLock blue_file_lock = null;
   public static FileChannel blue_file_lock_channel = null;
   
   /**
    * Main method for the BLUE Server. 
    * 
    * Command Line Options.<br />
    * <li>h or ? - help</li>
    * <li>v - verify. Verifys configuration files.</li>
    * <li>V - version information</li>
    * <li>s - shows scheduling information</li>
    * <li>d - start blue as daemon - left over from original.  not sure what yet to do with this in java</li>
    * 
    */
   
   public static void main(String[] args)
   {
      int result;
      int error= common_h.FALSE;
      // char buffer = new char[common_h.MAX_INPUT_BUFFER];
      int display_license=common_h.FALSE;
      int display_help=common_h.FALSE;
      
      is_core = true;
      
      /* make sure we at least have some command line arguments */
      
      if(args== null || args.length <1)
      {
         /* Sets the value of blue.cfg to that specified within locations.h class */
    	  
         blue.config_file = locations_h.DEFAULT_CONFIG_FILE;
         
         /* Verify that specified config file does indeed exist */
         
         if(!new File(blue.config_file).exists())
            error=common_h.TRUE;
      } 
      else
      {
         
    	 /* Begin to parse the command line options */
    	  
    	 Options options = new Options();
         options.addOption( "h", "help", false, "Display Help");
         options.addOption( "?", "help", false, "Display Help");
         options.addOption( "v", "verify", false, "Reads all data in the configuration files and performs a basic verification/sanity check.  Always make sure you verify your config data before (re)starting Blue." );
         options.addOption( "V", "version", false, "Display Version and License information." );
         options.addOption( "s", "schedule", false, "Shows projected/recommended check scheduling information based on the current data in the configuration files." );
         options.addOption( "d", "daemon", false, "Starts Blue in daemon mode (instead of as a foreground process). This is the recommended way of starting Blue for normal operation." );
         options.addOption( "c", "console", false, "Starts an embedded Web Server(Jetty) so you can run in one process" );
         options.addOption( "i", "initialise",false,"Tells Blue to start with an extremely basic configuration so you can get Blue running in no time at all");
         options.addOption( "p", "nagios_plugins",true,"Tell Blue where your Nagios Plugins are. This is needed to help deploy the basic configuration.");
         
         try 
         {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);
            
            /* Display help options. */
            
            if(cmd.hasOption('?') || cmd.hasOption( 'h'))
               display_help = common_h.TRUE;
            
            /* Disply current version information. */
            
            if (cmd.hasOption('V'))
               display_license = common_h.TRUE;
            
            /* Verify the configuration file. */
            
            if (cmd.hasOption ('v'))
               blue.verify_config = common_h.TRUE;
            
            /* Show recommended/projected test scheduling information */
            
            if (cmd.hasOption('s'))
               blue.test_scheduling = common_h.TRUE;
            
            /* Start blue in daemon mode (Recommended operation) */
            
            if (cmd.hasOption('d'))
               blue.daemon_mode = common_h.TRUE;
            
            if (cmd.hasOption('c'))
               blue.console_mode = true;
            
            if(cmd.hasOption('i'))
            	blue.initialise_config = true;
            
            if(cmd.hasOption('p'))
               	blue.nagios_plugins = cmd.getOptionValue('p').trim();
            
            /* set args to be the commands from CLI */ 
            
            args = cmd.getArgs();
            
            /* Verify args are present and set blue.cfg to be arg[0], unless we're copying a new one in.*/
            
            if((args == null || args.length != 1) && !cmd.hasOption('i'))
                blue.config_file = locations_h.DEFAULT_CONFIG_FILE;
            else if(args !=null && !cmd.hasOption('i'))
               blue.config_file = args[0];
            
         }
         catch(ParseException pe)
         {
            logger.error( pe.getMessage() , pe );
            error = common_h.TRUE;
         }
      }
      
      /* Are we running Blue in daemon mode? */
      
      if(blue.daemon_mode==common_h.FALSE)
      {
         
    	 /* If not running in daemon mode, begin printing of blue info */
    	  
    	 System.out.println("Blue Star, A Java Port of " + common_h.PROGRAM_VERSION);
         System.out.println("Copyright (c) 2006 BLUE (http://blue.sourceforge.net/)");
         System.out.println("Last Modified: " + common_h.PROGRAM_MODIFICATION_DATE );
         System.out.println("License: GPL");
      }
      
      /* just display the license */
      
      if(display_license==common_h.TRUE)
      {
         
         System.out.println("This program is free software; you can redistribute it and/or modify");
         System.out.println("it under the terms of the GNU General Public License version 2 as");
         System.out.println("published by the Free Software Foundation.");
         System.out.println("This program is distributed in the hope that it will be useful,");
         System.out.println("but WITHOUT ANY WARRANTY; without even the implied warranty of");
         System.out.println("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
         System.out.println("GNU General Public License for more details.");
         System.out.println("You should have received a copy of the GNU General Public License");
         System.out.println("along with this program; if not, write to the Free Software");
         System.out.println("Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.");
         
         // If running this in an appserver,,is this going to shutdown the app server?
         // something to be considered for later on.
         System.exit(common_h.OK);
      }
      
      /* If there are no command line options (or if we encountered an error), or user want to 
       * see how to use Blue, print usage */
      
      if(error==common_h.TRUE || display_help==common_h.TRUE)
      {
         System.out.println("Usage: java -jar blue-server.jar [option] <main_config_file>" );
         System.out.println("");
         System.out.println("Options:");
         System.out.println("");
         System.out.println("  -v   Reads all data in the configuration files and performs a basic");
         System.out.println("       verification/sanity check.  Always make sure you verify your");
         System.out.println("       config data before (re)starting Blue.");
         System.out.println("");
         System.out.println("  -s   Shows projected/recommended check scheduling information based");
         System.out.println("       on the current data in the configuration files.");
         System.out.println("");
         System.out.println("  -d   Starts Blue in daemon mode (instead of as a foreground process).");
         System.out.println("       This is the recommended way of starting Blue for normal operation.");
         System.out.println("       This is legacy from original base, irrelevant currently in java.");
         System.out.println("");
         System.out.println("  -c   Experimental!  Starts the blue-console within the same server.");
         System.out.println("");
         System.out.println("  -i   Initialise Blue with an extremely basic monitoring configuration.");
         System.out.println("		This is designed to get you up and Running with Blue in no time at");
         System.out.println("		at all my showing you how Blue operates. Your new configuration will");
         System.out.println("       be stored in the etc/basic directory!");
         System.out.println("");
         System.out.println("  -p   Location of Nagios Plugins. Use this option to tell Blue where your");
         System.out.println("		Nagios plugins are installed. This is used to help build the initial");
         System.out.println("		basic configuration and is not a required option");
         System.out.println("");
         System.out.println("Visit the Blue website at http://blue.sourceforge.net/ for bug fixes, new");
         System.out.println("releases, online documentation, FAQs, information on subscribing to");
         System.out.println("the mailing lists, and commercial and contract support for Blue.");
         System.out.println("");
         
         System.exit(common_h.ERROR);
      }
      
      /* Are we providing the user with a basic config? */
      if(blue.initialise_config)
      {
     	/* Find out where the user has installed Blue to, we need to copy files relative to this */
    	String installLocation = System.getProperty("user.dir");
     	result = common_h.OK;
    	
     	try
     	{
     		logger.info("Copying requested Basic Configuration");
     		
     		/* Make the structure we're going to need */
     		File myFile = new File(installLocation + "/etc/basic/xml/templates");
     		if(!myFile.exists())
     			myFile.mkdirs();
     		
     		/* Check to see if there is a config already in place */
     		/* If there is we abort the copy process, don't want to overwrite anyones config */
     		
     		myFile = new File(installLocation + "/etc/basic/blue.cfg");
     		if(myFile.exists())
     		{
     			logger.info("Requested configuration file already exists. Aborting copying process!");
     			result = common_h.ERROR;
     		}
     		
     		if(result == common_h.OK)
     			result = utils.copyDirectory(new File(installLocation + "/etc/samples/basic/"),new File(installLocation + "/etc/basic"));
     		
     		/* Essentially emulating Sed statements */
     		
     		if(result == common_h.OK)
     		{
     			utils.replaceString("/usr/local/blue/",installLocation + "/",new File(installLocation + "/etc/basic/resource.cfg"));
     			utils.replaceString("/usr/local/nagios/libexec",blue.nagios_plugins + "/",new File(installLocation + "/etc/basic/resource.cfg"));
     			utils.replaceString("/usr/local/blue/",installLocation + "/",new File(installLocation + "/etc/basic/xml/macros.xml"));
     			utils.replaceString("/usr/local/nagios/libexec",blue.nagios_plugins + "/",new File(installLocation + "/etc/basic/xml/macros.xml"));
     			utils.replaceString("/usr/local/blue/",installLocation + "/",new File(installLocation + "/etc/basic/blue.cfg"));
     			utils.replaceString("/usr/local/blue/",installLocation + "/",new File(installLocation + "/etc/basic/xml/blue.xml"));
     			utils.replaceString("/usr/local/blue/",installLocation + "/",new File(installLocation + "/etc/basic/cgi.cfg"));
     			
     			/* If the user has copied in a config, we should be nice to them and update the web.xml file aswell */
     			utils.replaceString("etc/cgi.cfg","etc/basic/cgi.cfg",new File(installLocation + "/etc/webdefault.xml"));
     		}
     		
     		/* Update the config tool with output location */
     		/* for next release should look at fixing this */
     		result = utils.lockConfigTool();
     		     		
     		if(result != common_h.OK)
     		{
     			logger.info("Bailing out due to Error in copying basic configuration Files!");
     			utils.cleanup();
     			System.exit(result);
     		}
     		
     		/* Remember to set the new config file location */
     		blue.config_file = installLocation + "/etc/basic/blue.cfg";
       	}
     	catch(Exception e)
     	{
     		logger.fatal("Bailing out due to Error in copying basic configuration Files!");
     		utils.cleanup();
     		System.exit(common_h.ERROR);
     	}
     	
      }
      
      /* Retrieve the absolute address of the config file. */
      File file = new File(blue.config_file);
      
      if(!file.isAbsolute())
      {
         blue.config_file = file.getAbsoluteFile().toString();
      }    
      
      /* we're just verifying the configuration... */
      if(blue.verify_config == common_h.TRUE)
      {
         /* reset program variables to defaults. */
         utils.reset_variables();
         
         /* Tell the user what we're upto */
         
         System.out.println("Reading configuration data...");
         System.out.println();
         
         /* read in the configuration files (main config file, resource and object config files) */
         
         if((result=config.read_main_config_file(blue.config_file)) == common_h.OK)
         {
            
        	 /* TODO drop privileges 
          	if((result= drop_privileges( blue.nagios_user, blue.nagios_group))== common_h.ERROR)
          	System.out.println("Failed to drop privileges.  Aborting.");
          	else */
            
        	 /* If we have passed initial checking of the blue.cfg, read object config files */
            result=config.read_all_object_data(blue.config_file);
         }
         
         /* there was a problem reading the config files */
         if(result!= common_h.OK)
         {
            
            /* if the config filename looks fishy, warn the user */
        	
        	 // likely that this won't work most of the time because we are comparing blue.cfg to and absolute path!
        	 // Updated it to reflect a more logical test that the final part of the config file location string should be
        	 // the blue.cfg itself.            
        	 
        	//if(!"blue.cfg".equals(blue.config_file))
        	if(!blue.config_file.endsWith("blue.cfg"))
            {
               System.out.println("***> The name of the main configuration file looks suspicious...");
               System.out.println();
               System.out.println("     Make sure you are specifying the name of the MAIN configuration file on");
               System.out.println("     the command line and not the name of another configuration file.  The");
               System.out.println("     main configuration file is typically '$BLUE_HOME/config/blue.cfg'");
            }
            
            /* If not then there may be a problem with the syntax specified within the config file */
            System.out.println();
            System.out.println("***> One or more problems was encountered while processing the config files...");
            System.out.println("");
            System.out.println("     Check your configuration file(s) to ensure that they contain valid");
            System.out.println("     directives and data defintions.  If you are upgrading from a previous");
            System.out.println("     version of Nagios, you should be aware that some variables/definitions");
            System.out.println("     may have been removed or modified in this version.  Make sure to read");
            System.out.println("     the HTML documentation regarding the config files, as well as the");
            System.out.println("     'Whats New' section to find out what has changed.");
            System.out.println();
         }
         
         /* Must have passed initial config checking, therefore run the pre-flight checks */
         
         else
         {
            
            System.out.println("Running pre-flight check on configuration data...");
            System.out.println();
            
            /* run the pre-flight check to make sure things look okay... */
            result= config.pre_flight_check();
            
            /* Have we passed pre-flight checking? */
            if(result== common_h.OK)
            {
               System.out.println();
               System.out.println("Things look okay - No serious problems were detected during the pre-flight check");
               System.out.println();
            }
            
            /* Encountered a problem with the pre-flight */
            else
            {
               System.out.println();
               System.out.println("***> One or more problems was encountered while running the pre-flight check...");
               System.out.println();
               System.out.println();
               System.out.println("     Check your configuration file(s) to ensure that they contain valid");
               System.out.println("     directives and data defintions.  If you are upgrading from a previous");
               System.out.println("     version of Nagios, you should be aware that some variables/definitions");
               System.out.println("     may have been removed or modified in this version.  Make sure to read");
               System.out.println("     the HTML documentation regarding the config files, as well as the");
               System.out.println("     'Whats New' section to find out what has changed.");
               System.out.println();
            }
         }
         
         /* clean up after ourselves */
         utils.cleanup();
         
         /* free config_file */
         config_file = null;
         
         /* exit */
         System.exit(result);
      }
      /* ---------- END OF VERIFY CONFIG OPTION ------------- */
      
      
      
      /*  if we're just testing scheduling... */
      else if(test_scheduling == common_h.TRUE)
      {
         
         /* reset program variables */
         utils.reset_variables();
         
         /* read in the configuration files (main config file and all host config files) */
         if((result=config.read_main_config_file(config_file))== common_h.OK){
            
            /* drop privileges */
//          if((result=drop_privileges(nagios_user,nagios_group))==ERROR)
//          printf("Failed to drop privileges.  Aborting.");
//          else
//          /* read object config files */
            result=config.read_all_object_data(config_file);
         }
         
         if(result!=common_h.OK)
            System.out.println("***> One or more problems was encountered while reading configuration data...");
         
         /* run the pre-flight check to make sure everything looks okay */
         else if((result=config.pre_flight_check())!=common_h.OK)
            System.out.println("***> One or more problems was encountered while running the pre-flight check...");
         
         if(result==common_h.OK){
            
            /* initialize the event timing loop */
            events.init_timing_loop();
            
            /* display scheduling information */
            events.display_scheduling_info();
         }
         
         /* clean up after ourselves */
         utils.cleanup();
         
         /* exit */
         System.exit(result);
      }
      /*-------- END OF SCHEDULING TESTING ---------*/
      
      
      
      /* ----- OTHERWISE BEGIN BLUE MONITORING ------- */
      else
      {
         File file_lock = null;
         
        /* keep monitoring things until we get a shutdown command */
        do
        {
            /* reset program variables */
            utils.reset_variables();
            
            /* get program (re)start time and save as macro */
            program_start= utils.currentTimeInSeconds();
            macro_x[blue_h.MACRO_PROCESSSTARTTIME]= "" + program_start;
            
           /* get PID */
           //blue_pid=(int)getpid();
          
            /* read in the configuration files (main and resource config files) */
            result = config.read_main_config_file(config_file);
          
//          /* drop privileges */
//          if(drop_privileges(nagios_user,nagios_group)==ERROR){
//          
//          snprintf(buffer,sizeof(buffer),"Failed to drop privileges.  Aborting.");
//          buffer[sizeof(buffer)-1]='\x0';
//          write_to_logs_and_console(buffer,NSLOG_PROCESS_INFO | NSLOG_RUNTIME_ERROR | NSLOG_CONFIG_ERROR,TRUE);
//          
//          cleanup();
//          exit(ERROR);
//          }
          
            /* initialize Event Broker modules */
            nebmods.neb_init_modules();
            nebmods.neb_init_callback_list();
          
            /* this must be logged after we read config data, as user may have changed location of main log file */
            logger.info( "Blue "+ common_h.PROGRAM_VERSION+" starting... (PID="+blue_pid+")");            

//            /* write log version/info */
//          write_log_file_info( null );
          
            /* load modules */
            nebmods.neb_load_all_modules();
           
            /* send program data to broker */
            broker.broker_program_state(broker_h.NEBTYPE_PROCESS_PRELAUNCH,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,null);
           
            /* read in all object config data */
            if(result== common_h.OK)
               result = config.read_all_object_data(config_file);
            
            /* there was a problem reading the config files */
            
            if(result!= common_h.OK)
            {
               
               logger.fatal( "Bailing out due to one or more errors encountered in the configuration files.  Run Blue from the command line with the -v option to verify your config before restarting. (PID=UNKNOWN)\n");
               
               /* close and delete the external command file if we were restarting */
               if(sigrestart==common_h.TRUE)
                  utils.close_command_file();
               
               /* send program data to broker */
               broker.broker_program_state(broker_h.NEBTYPE_PROCESS_SHUTDOWN,broker_h.NEBFLAG_PROCESS_INITIATED,broker_h.NEBATTR_SHUTDOWN_ABNORMAL,null);
               
               utils.cleanup();
               
               System.exit(common_h.ERROR);
            }
             
                        
            /* run the pre-flight check to make sure everything looks okay*/
            result = config.pre_flight_check();
            
            /* there was a problem running the pre-flight check */
            if(result != common_h.OK)
            {
               logger.fatal( "Bailing out due to errors encountered while running the pre-flight check.  Run Nagios from the command line with the -v option to verify your config before restarting. (PID=UNKNOWN)");
               
               /* close and delete the external command file if we were restarting */
               if(sigrestart==common_h.TRUE)
                  utils.close_command_file();
               
               /* send program data to broker */
               broker.broker_program_state( broker_h.NEBTYPE_PROCESS_SHUTDOWN,broker_h.NEBFLAG_PROCESS_INITIATED,broker_h.NEBATTR_SHUTDOWN_ABNORMAL,null);
               
               utils.cleanup();
               System.exit(common_h.ERROR);
            }
            
            logger.info("Pre-flight checks passed successfully!");

            // Rob 15/01/07 - Seems like a reasonable place to check to see if there is another
            // version of Blue running as far as we can tell..
            
            result = utils.lock_file_exists();
            
            if(result == common_h.OK)
            {
            	logger.fatal("Bailing out due to another version of Blue running.");
            	
            	if(sigrestart == common_h.TRUE)
            		utils.close_command_file();
            	
            	broker.broker_program_state(broker_h.NEBTYPE_PROCESS_SHUTDOWN,broker_h.NEBFLAG_PROCESS_INITIATED,broker_h.NEBATTR_SHUTDOWN_ABNORMAL,null);
            	utils.cleanup();
            	System.exit(common_h.ERROR);
            }
            
//          
//          /* initialize embedded Perl interpreter */
//          if(sigrestart==FALSE){
//          #ifdef EMBEDDEDPERL
//          init_embedded_perl(env);
//          #else
//          init_embedded_perl(NULL);
//          #endif
//          }
//          
            /* handle signals (interrupts) */
            utils.setup_sighandler();
                       
            /* send program data to broker */
            broker.broker_program_state(broker_h.NEBTYPE_PROCESS_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,null);
            
//          /* enter daemon mode (unless we're restarting...) */
//          if(daemon_mode==TRUE && sigrestart==FALSE){
//          #if (defined DEBUG0 || defined DEBUG1 || defined DEBUG2 || defined DEBUG3 || defined DEBUG4 || defined DEBUG5)
//          printf("$0: Cannot enter daemon mode with DEBUG option(s) enabled.  We'll run as a foreground process instead...\n");
//          daemon_mode=FALSE;
//          #else
//          daemon_init();
//          
//          snprintf(buffer,sizeof(buffer),"Finished daemonizing... (New PID=%d)\n",(int)getpid());
//          buffer[sizeof(buffer)-1]='\x0';
//          write_to_all_logs(buffer,NSLOG_PROCESS_INFO);
//          
//          /* get new PID */
//          blue_pid=(int)getpid();
//          #endif
//          }

            /* Create the needed file lock. */
            try
            {
               file_lock = new File (blue.lock_file);
               blue_file_lock_channel = new RandomAccessFile(file_lock,"rw").getChannel();
                              
               /* Blue currently hanging here on Windows, switched to tryLock() method */
               //blue_file_lock = blue_file_lock_channel.lock();
               
               blue_file_lock = blue_file_lock_channel.tryLock();
               logger.info("Lock File LOCKED");
            }
            catch (Exception e)
            {
               logger.fatal( "Lock File FAILED");
            }
            
            /* open the command file (named pipe) for reading */
            result = utils.open_command_file();
                        
            if(result!= common_h.OK)
            {                
               logger.fatal( "Bailing out due to errors encountered while trying to initialize the external command file... (PID= UNKNOWN)\n" );
               
               /* send program data to broker */
               broker.broker_program_state(broker_h.NEBTYPE_PROCESS_SHUTDOWN,broker_h.NEBFLAG_PROCESS_INITIATED,broker_h.NEBATTR_SHUTDOWN_ABNORMAL,null);
               
               utils.cleanup();
               System.exit( common_h.ERROR);
            }
            
            /* Start the console if we are doing so */
            if(blue.console_mode)
            {
               try
               {
                   logger.info( "Blue Embedded Console "+ common_h.PROGRAM_VERSION+" starting... (PID="+blue_pid+")");
                   String path = new File(blue.config_file).getParent();
                   org.mortbay.start.Main.main( new String[] { path + "/jetty.xml" } );
               }
               catch (Exception e)
               {
                  logger.fatal("Bailing out could not launch embedded console.", e);
                  utils.cleanup();
                  System.exit(common_h.ERROR);
               }
            }
            
            /* initialize status data */
            statusdata.initialize_status_data(config_file);
            
            /* initialize comment data */
            comments.initialize_comment_data(config_file);
            
            /* initialize scheduled downtime data */
            downtime.initialize_downtime_data(config_file);
                        
            /* initialize performance data */
            perfdata.initialize_performance_data(config_file);
                        
            /* read initial service and host state information  */
            sretention.read_initial_state_information(config_file);
                        
            /* initialize the event timing loop */
            events.init_timing_loop();
                        
            /* update all status data (with retained information) */
            statusdata.update_all_status_data();
                        
            /* log initial host and service state */
            logging.log_host_states(blue_h.INITIAL_STATES);
            logging.log_service_states(blue_h.INITIAL_STATES);
            
            
            /* create pipe used for service check IPC */
            try 
            {
               blue.ipc_pipe = Pipe.open();
               // TODO make sure read end of Pipe is non blocking
            }
            catch (IOException ioE )
            {
               logger.fatal( "Error: Could not initialize service check IPC pipe...");
               
               /* send program data to broker */
               broker.broker_program_state(broker_h.NEBTYPE_PROCESS_SHUTDOWN,broker_h.NEBFLAG_PROCESS_INITIATED,broker_h.NEBATTR_SHUTDOWN_ABNORMAL,null);
               
               utils.cleanup();
               System.exit( common_h.ERROR );
            }
            
            /* initialize service result worker threads */
            result = service_result_worker_thread.init_service_result_worker_thread();
            
            if(result!=common_h.OK)
            {
               logger.fatal( "Bailing out due to errors encountered while trying to initialize service result worker thread... (PID=UNKNONW)\n");
               
               /* send program data to broker */
               broker.broker_program_state(broker_h.NEBTYPE_PROCESS_SHUTDOWN,broker_h.NEBFLAG_PROCESS_INITIATED,broker_h.NEBATTR_SHUTDOWN_ABNORMAL,null);
               
               utils.cleanup();
               System.exit( common_h.ERROR);
            }
            
            /* reset the restart flag */
            sigrestart= common_h.FALSE;

            /* send program data to broker */
            broker.broker_program_state(broker_h.NEBTYPE_PROCESS_EVENTLOOPSTART,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,null);
            
            /***** start monitoring all services *****/
            /* (doesn't return until a restart or shutdown signal is encountered) */
            events.event_execution_loop();
                       
            /* send program data to broker */
            broker.broker_program_state(broker_h.NEBTYPE_PROCESS_EVENTLOOPEND,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,null);
            if(sigshutdown==common_h.TRUE)
               broker.broker_program_state(broker_h.NEBTYPE_PROCESS_SHUTDOWN,broker_h.NEBFLAG_USER_INITIATED,broker_h.NEBATTR_SHUTDOWN_NORMAL,null);
            else if(sigrestart==common_h.TRUE)
               broker.broker_program_state(broker_h.NEBTYPE_PROCESS_RESTART,broker_h.NEBFLAG_USER_INITIATED,broker_h.NEBATTR_RESTART_NORMAL,null);
            
            /* save service and host state information */
            sretention.save_state_information(config_file, common_h.FALSE);
           
            /* clean up the status data */
            statusdata.cleanup_status_data(config_file, common_h.TRUE);
                       
            /* clean up the comment data */
            comments.cleanup_comment_data(config_file);
                        
            /* clean up the scheduled downtime data */
            downtime.cleanup_downtime_data(config_file);
                        
            /* clean up performance data */
            perfdata.cleanup_performance_data(config_file);
                        
            /* close the original pipe used for IPC (we'll create a new one if restarting) */
            try
            {
               ipc_pipe.sink().close();
               ipc_pipe.source().close();
            }
            catch(IOException ioE)
            {
               logger.error(ioE.getMessage(),ioE);
            }
            
            /* close and delete the external command file FIFO unless we're restarting */
            if(sigrestart==common_h.FALSE)
               utils.close_command_file();
            
//          /* cleanup embedded perl interpreter */
//          if(sigrestart==FALSE)
//          deinit_embedded_perl();
            
            /* cleanup worker threads */
            service_result_worker_thread.shutdown_service_result_worker_thread();
            
            /* shutdown stuff... */
            if(sigshutdown==common_h.TRUE)
            {
               
//             /* make sure lock file has been removed - it may not have been if we received a shutdown command */
//             if(daemon_mode==TRUE)
//             unlink(lock_file);
               
               /* log a shutdown message */
               logger.info( "Successfully shutdown... (PID=UNKNOWN)" );
            }
            
            /* clean up after ourselves */
            utils.cleanup();
            
         }
        while(sigrestart==common_h.TRUE && sigshutdown==common_h.FALSE);

         /* Release the lock file, so others are sure we are not running! */
         try
         { 
            logger.info( "Releasing LOCK file!" );
            blue_file_lock.release();
            blue_file_lock_channel.close();
         }
         catch (Exception e)
         {            
         }
      }
   }
   
   /**
    * Method to work out the process id of spawned blue server.
    * NOTE: No native method of doing this in java...
    * 
    * is on the to-do list.
    * @return, the pid that the blue server has spawned under.
    */
   /*
   private static int getpid()
   {
	   return 1;
   }*/
}


