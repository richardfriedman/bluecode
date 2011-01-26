/*****************************************************************************
 *
 * Blue Star, a Java Port of .
 * Last Modified : 3/20/2006
 *
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.ListIterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.common.objects;
import org.blue.star.include.blue_h;
import org.blue.star.include.broker_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;

/** 
 * Load all the configuration properties and store them in appropriate
 * locations.
 */

public class config 
{
    
    public static events m_events = new events();
    public static nebmods m_nebmods = new nebmods();
    
    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.base.config");
    public static String cn = "org.blue.base.config";
    
    /*
     * Just a testing main.
     * 
     * @param args
     *            specify the main_config_file to read.
     */
    
    public static void main(String[] args)
    {
        config.read_main_config_file(args[0]);
        config.pre_flight_check();
    }
    
    /******************************************************************/
    /************** CONFIGURATION INPUT FUNCTIONS *********************/
    /******************************************************************/
    
    /* read all configuration data */
    public static int read_all_object_data(String main_config_file)
    {
        int result = common_h.OK;
        int options;
        int cache = common_h.FALSE;
        
        logger.trace( "entering " + cn + ".read_all_config_data" );
        
        options = common_h.READ_ALL_OBJECT_DATA;
        
        /* cache object definitions if we're up and running */
        if(blue.verify_config==common_h.FALSE && blue.test_scheduling==common_h.FALSE)
            cache=common_h.TRUE;
        
        /* read in all host configuration data from external sources */
        result= objects.read_object_config_data(main_config_file,options,cache);
        
        if(result!=common_h.OK)
            return common_h.ERROR;
        
        logger.trace( "exiting " + cn + ".read_all_config_data" );
        
        return common_h.OK;
    }
    
    /**
     * process the main configuration file
     */
    
    public static int read_main_config_file(String main_config_file)
    {
        
    	/* Begin logging */
    	
    	logger.trace( "entering " + cn + ".read_main_config_file" );
        
        int result = common_h.OK;
        int command_check_interval_is_seconds = common_h.FALSE;
        BufferedReader reader; 
        
        /* open the config file for reading */
        File thefile = new File(main_config_file);
        
        /* Check that the config file exists and that we can actually read it */
        
        if(!thefile.exists() || !thefile.canRead())
        {
            logger.fatal( "Error: Cannot open main configuration file "+ main_config_file+" for reading! " );
            return common_h.ERROR;
        }
        else
        { 
           try
           {
                reader = new BufferedReader(new FileReader(thefile));
           }
           catch(Exception e)
           {
                return common_h.ERROR;
           }
        }
        
        logger.debug("Config file: " + main_config_file);
        
        /* save the main config file macro */
        /* inserts name of config file into element 62 of blue.macro_x array */
        
        blue.macro_x[blue_h.MACRO_MAINCONFIGFILE] = main_config_file.trim();
        
        String line = "";
        int lineCounter = 0;
        
        /* Begin to read in the config file */
        
        while((line != null) && (result == common_h.OK ))
        {
            try
            {
                line = reader.readLine();
            }
            catch(Exception e)
            {
                logger.error( e.getMessage(), e);
                return common_h.ERROR;
            }
            
            /* If there is nothing to read, continue */
            
            if(line == null)
                continue;
            else 
                line = line.trim();
            
            lineCounter++;
            
            /* We can skip over lines that are blank */
            
            if(line.length() == 0) 
                continue;
            
            /* Check to see if this is a comment line */
            
            if((line.startsWith("#")) || (line.startsWith(";")))
                continue;
            
            // skip external directives, TODO did the port from original apply?
            
            if(line.startsWith("x"))
                continue;
            
            logger.debug("Line "+ lineCounter +" = " + line);
            
            try
            {
                
                /* Split the config line we've just read in */
            	
            	// TODO - Updated Rob 12/01/07 - Previously this broke in the case of event broker modules.
            	// Take the following line as an example
            	// broker_module=/somewhere/module2.o arg1 arg2=3 debug=0;
            	
            	String[] variable_value = line.split("=");
                String variable = variable_value[0];
                String value = "";
            	
            	/* Perform some checks to verify that we have a valid config line */
                
                if(((variable_value.length != 2) || (variable_value[0].trim().length() == 0)) && !variable.equals("broker_module"))
                {
                    throw new IllegalArgumentException("NULL variable");
                }
                else if(variable_value[1].trim().length() == 0)
                {
                    throw new IllegalArgumentException ("NULL value");
                } 
                
                /* Assign the values from the config file */
                
                //String variable = variable_value[0].trim();
                if(!variable.equals("broker_module") || (variable.equals("broker_module") && variable_value.length == 2))
                	 value = variable_value[1].trim();
                else                	
                {
                	for(int i = 1;i<=variable_value.length-1;i++)
                	{
                		if(i == variable_value.length -1)
                			value += variable_value[i];
                		else
                			value += variable_value[i] + "=";
                	}
                }
                
                /* If the config parameter is resource_file, begin reading from that config file
                 * to build up the user defined macros */
                                 
                if(variable.equals("resource_file" ))
                {
                	read_resource_file(handleFile(blue_h.MACRO_RESOURCEFILE, variable, value ));
                }   
                
                /* Setting a lot of variables from blue.cfg..nothing too interesting to see here */

                else if( variable.equals("external_command_buffer_slots")) ; // external_command_buffer_slots=handleInt(value);
                else if( variable.equals("check_result_buffer_slots")) ;  // check_result_buffer_slots=handleInt(value);
                else if (variable.equals("log_file"))blue.log_file = handleFile(blue_h.MACRO_LOGFILE, variable, value);                                
                else if (variable.equals("command_file")) blue.command_file = handleFile(blue_h.MACRO_COMMANDFILE,variable,value);
                else if (variable.equals("temp_file")) blue.temp_file = handleFile(blue_h.MACRO_TEMPFILE,variable,value);
                else if (variable.equals("lock_file")) blue.lock_file = handleFile(-1,variable,value);
                else if (variable.equals("log_archive_path")) blue.log_archive_path = handleFile(-1,variable,value);
                else if (variable.equals("p1_file")) blue.p1_file = handleFile(-1,variable,value);
                else if (variable.equals("auth_file")) blue.auth_file = handleFile(-1,variable,value);  
                else if (variable.equals("global_host_event_handler")) blue.global_host_event_handler = handleString(variable, value);
                else if (variable.equals("global_service_event_handler")) blue.global_service_event_handler = handleString(variable,value);
                else if (variable.equals("ocsp_command")) blue.ocsp_command = handleString(variable,value);
                else if (variable.equals("ochp_command")) blue.ochp_command = handleString(variable,value);
                else if (variable.equals("nagios_user")) blue.nagios_user = handleString(variable,value);
                else if (variable.equals("nagios_group")) blue.nagios_group = handleString(variable,value);
                else if (variable.equals("admin_email")) blue.macro_x[blue_h.MACRO_ADMINEMAIL] = handleString(variable,value); 
                else if (variable.equals("admin_pager")) blue.macro_x[blue_h.MACRO_ADMINPAGER] = handleString(variable,value); 
                else if (variable.equals("use_syslog")) blue.use_syslog = handleBoolean(variable,value);
                else if (variable.equals("log_notifications")) blue.log_notifications = handleBoolean(variable,value);
                else if (variable.equals("log_service_retries")) blue.log_service_retries = handleBoolean(variable,value);
                else if (variable.equals("log_host_retries")) blue.log_host_retries = handleBoolean(variable,value);
                else if (variable.equals("log_event_handlers")) blue.log_event_handlers = handleBoolean(variable,value);
                else if (variable.equals("log_external_commands")) blue.log_external_commands = handleBoolean(variable,value);
                else if (variable.equals("log_passive_checks")) blue.log_passive_checks = handleBoolean(variable,value);
                else if (variable.equals("log_initial_states")) blue.log_initial_states = handleBoolean(variable,value);
                else if (variable.equals("retain_state_information")) blue.retain_state_information = handleBoolean(variable,value);
                else if (variable.equals("retention_update_interval")) blue.retention_update_interval = handlePositiveInt(variable,value);
                else if (variable.equals("use_retained_program_state")) blue.use_retained_program_state = handleBoolean(variable,value);
                else if (variable.equals("use_retained_scheduling_info")) blue.use_retained_scheduling_info = handleBoolean(variable,value);
                else if (variable.equals("retention_scheduling_horizon")) blue.retention_scheduling_horizon = handlePositiveInt(variable,value);
                else if (variable.equals("obsess_over_services")) blue.obsess_over_services = handleBoolean(variable,value);
                else if (variable.equals("obsess_over_hosts")) blue.obsess_over_hosts = handleBoolean(variable,value);
                else if (variable.equals("service_check_timeout")) blue.service_check_timeout = handlePositiveInt(variable,value);
                else if (variable.equals("host_check_timeout")) blue.host_check_timeout = handlePositiveInt(variable,value);
                else if (variable.equals("event_handler_timeout")) blue.event_handler_timeout = handlePositiveInt(variable,value);
                else if (variable.equals("notification_timeout")) blue.notification_timeout = handlePositiveInt(variable,value);
                else if (variable.equals("ocsp_timeout")) blue.ocsp_timeout = handlePositiveInt(variable,value);
                else if (variable.equals("ochp_timeout")) blue.ochp_timeout = handlePositiveInt(variable,value);
                else if (variable.equals("use_aggressive_host_checking")) blue.use_aggressive_host_checking = handleBoolean(variable,value);
                else if (variable.equals("soft_state_dependencies")) blue.soft_state_dependencies = handleBoolean(variable,value);
                
                /* Deal with log rotation method */
                
                else if (variable.equals("log_rotation_method"))
                {
                    if(value.equals("n")) blue.log_rotation_method=common_h.LOG_ROTATION_NONE;
                    else if(value.equals("h")) blue.log_rotation_method=common_h.LOG_ROTATION_HOURLY;
                    else if(value.equals("d")) blue.log_rotation_method=common_h.LOG_ROTATION_DAILY;
                    else if(value.equals("w")) blue.log_rotation_method=common_h.LOG_ROTATION_WEEKLY;
                    else if(value.equals("m")) blue.log_rotation_method=common_h.LOG_ROTATION_MONTHLY;
                    else throw new IllegalArgumentException ("Illegal value for " + variable);
                    
                    logger.debug(variable + " set to " + blue.log_rotation_method );
                }
                
                /* Return to setting variables */
                
                else if (variable.equals("enable_event_handlers") ) blue.enable_event_handlers = handleBoolean( variable, value );
                else if (variable.equals("enable_notifications") ) blue.enable_notifications = handleBoolean( variable, value );
                else if (variable.equals("execute_service_checks") ) blue.execute_service_checks = handleBoolean( variable, value );
                else if (variable.equals("accept_passive_service_checks") ) blue.accept_passive_service_checks = handleBoolean( variable, value );
                else if (variable.equals("execute_host_checks") ) blue.execute_host_checks = handleBoolean( variable, value );
                else if (variable.equals("accept_passive_host_checks") ) blue.accept_passive_host_checks = handleBoolean( variable, value );
                
                /* Deal with service_inter_check_delay_method */
                
                else if(variable.equals("service_inter_check_delay_method"))
                {
                    if(value.equals("n")) blue.service_inter_check_delay_method=blue_h.ICD_NONE;
                    else if(value.equals("d")) blue.service_inter_check_delay_method=blue_h.ICD_DUMB;
                    else if(value.equals("s")) blue.service_inter_check_delay_method=blue_h.ICD_SMART;
                    else
                    {
                        blue.service_inter_check_delay_method=blue_h.ICD_USER;
                        events.scheduling_info.service_inter_check_delay = handlePositiveDouble(variable, value);
                    }
                    
                    logger.debug(variable + " set to " + blue.service_inter_check_delay_method );
                }
                
                else if (variable.equals("max_service_check_spread")) blue.max_service_check_spread = handlePositiveInt( variable, value );             
                
                /* Deal with host_inter_check_delay_method */
                
                else if(variable.equals("host_inter_check_delay_method"))
                {
                    if(value.equals("n")) blue.host_inter_check_delay_method=blue_h.ICD_NONE;
                    else if(value.equals("d")) blue.host_inter_check_delay_method=blue_h.ICD_DUMB;
                    else if(value.equals("s")) blue.host_inter_check_delay_method=blue_h.ICD_SMART;
                    else
                    {
                        blue.host_inter_check_delay_method=blue_h.ICD_USER;
                        events.scheduling_info.host_inter_check_delay = handlePositiveDouble(variable, value);
                    }
                    
                    logger.debug(variable + " set to " + blue.host_inter_check_delay_method );
                }
                else if (variable.equals("max_host_check_spread")) blue.max_host_check_spread = handlePositiveInt( variable, value );             
                
                /* Deal with service_interleave_factor */
                
                else if(variable.equals("service_interleave_factor"))
                {
                    if(value.equals("s")) blue.service_interleave_factor_method=blue_h.ILF_SMART;
                    else
                    {
                        blue.service_interleave_factor_method=blue_h.ILF_USER;
                        events.scheduling_info.service_interleave_factor = handleInt(variable,value);
                        
                        if(events.scheduling_info.service_interleave_factor < 1) 
                            events.scheduling_info.service_interleave_factor = 1;
                    }
                    
                    logger.debug(variable + " set to " + blue.service_interleave_factor_method );
                }

                else if (variable.equals("max_concurrent_checks")) blue.max_parallel_service_checks = handlePositiveInt(variable,value);
                else if (variable.equals("service_reaper_frequency")) blue.service_check_reaper_interval = handlePositiveInt(variable,value);
                else if (variable.equals("sleep_time")) blue.sleep_time = handlePositiveDouble(variable,value);
                else if (variable.equals("interval_length")) blue.interval_length = handlePositiveInt(variable,value);
                else if (variable.equals("check_external_commands")) blue.check_external_commands = handleBoolean(variable,value);
                
                /* Deal with command check interval */
                else if(variable.equals("command_check_interval"))
                {
                    command_check_interval_is_seconds =(value.indexOf("s")> 0)? common_h.TRUE:common_h.FALSE;
                    
                    if(value.indexOf("s") > 0)
                    	value = value.substring(0,value.indexOf("s"));
                    
                    blue.command_check_interval = handleInt(variable,value);
                    
                    if((blue.command_check_interval<-1) || (blue.command_check_interval==0)) 
                        throw new IllegalArgumentException("Illegal value for " + variable);
                    
                    logger.debug( variable + " set to " + blue.command_check_interval );
                }
                
                else if (variable.equals("check_for_orphaned_services")) blue.check_orphaned_services = handleBoolean(variable,value);
                else if (variable.equals("check_service_freshness")) blue.check_service_freshness = handleBoolean(variable,value);
                else if (variable.equals("check_host_freshness")) blue.check_host_freshness = handleBoolean(variable,value);
                else if (variable.equals("freshness_check_interval")) blue.service_freshness_check_interval = handlePositiveInt(variable,value);
                else if (variable.equals("service_freshness_check_interval")) blue.service_freshness_check_interval = handlePositiveInt(variable,value);
                else if (variable.equals("host_freshness_check_interval")) blue.host_freshness_check_interval = handlePositiveInt(variable,value);
                else if (variable.equals("auto_reschedule_checks")) blue.auto_reschedule_checks = handleBoolean(variable,value);
                else if (variable.equals("auto_rescheduling_interval")) blue.auto_rescheduling_interval = handlePositiveInt(variable,value);
                else if (variable.equals("auto_rescheduling_window")) blue.auto_rescheduling_window = handlePositiveInt(variable,value);
                else if (variable.equals("aggregate_status_updates")) blue.aggregate_status_updates = handleBoolean(variable,value);
                else if (variable.equals("status_update_interval")) blue.status_update_interval = handlePositiveInt(variable,value);
                
                /* Deal with time_change_threshold */
                
                else if (variable.equals("time_change_threshold"))
                {
                    blue.time_change_threshold = handleInt(variable,value);
                    
                    if (blue.time_change_threshold <= 5 )
                    {
                        throw new IllegalArgumentException ("Illegable value for " + variable);
                    }
                }

                else if (variable.equals("process_performance_data")) blue.process_performance_data = handleBoolean(variable,value);
                else if (variable.equals("enable_flap_detection")) blue.enable_flap_detection = handleBoolean(variable,value);
                else if (variable.equals("enable_failure_prediction")) blue.enable_failure_prediction = handleBoolean(variable,value);
                
                /* Deal with low_service_flap_threshold */
                
                else if (variable.equals("low_service_flap_threshold"))
                {
                    blue.low_service_flap_threshold = handlePositiveDouble(variable,value);
                    
                    if(blue.low_service_flap_threshold >= 100.0)
                    	throw new IllegalArgumentException ("Illegable value for " + variable );
                }
                
                /* Deal with high_service_flap_threshold */
                
                else if(variable.equals("high_service_flap_threshold"))
                {
                    blue.high_service_flap_threshold = handlePositiveDouble(variable,value);
                    if(blue.high_service_flap_threshold > 100.0) 
                        throw new IllegalArgumentException ( "Illegable value for " + variable );
                }
                
                /* Deal with low_host_flap_threshold */
                
                else if(variable.equals("low_host_flap_threshold"))
                {
                    blue.low_host_flap_threshold = handlePositiveDouble(variable,value);
                    
                    if(blue.low_host_flap_threshold >= 100.0) 
                        throw new IllegalArgumentException ("Illegable value for " + variable);
                }
                
                /* Deal with high_host_flap_threshold */
                
                else if(variable.equals("high_host_flap_threshold"))
                {
                    blue.high_host_flap_threshold = handlePositiveDouble(variable,value);
                    
                    if( blue.high_host_flap_threshold > 100.0) 
                        throw new IllegalArgumentException ("Illegable value for " + variable);
                }
                
                /* Deal with date_format */
                
                else if(variable.equals("date_format"))
                {
                    
                	if(value.equals("euro")) blue.date_format = common_h.DATE_FORMAT_EURO;
                    else if (value.equals("iso8601")) blue.date_format = common_h.DATE_FORMAT_ISO8601;
                    else if (value.equals("strict-iso8601")) blue.date_format = common_h.DATE_FORMAT_STRICT_ISO8601;
                    else blue.date_format = common_h.DATE_FORMAT_US;
                  
                	logger.debug(variable + " set to " + blue.date_format);
                }
                
                /* Deal with event_broker_options */
                
                else if(variable.equals("event_broker_options"))
                {
                    blue.event_broker_options = handleInt(variable,value);
                    if (blue.event_broker_options < 0) 
                       blue.event_broker_options = broker_h.BROKER_EVERYTHING;
                }
                
                else if (variable.equals("illegal_object_name_chars")) blue.illegal_object_chars = handleString(variable,value);
                else if (variable.equals("illegal_macro_output_chars")) blue.illegal_output_chars = handleString(variable,value);
                
                /* Deal with the broker_module */
                
                else if (variable.equals("broker_module"))
                {
                    
                	String[] varList = value.split( "\\s");
                	                    
                	if (varList != null)
                    {
                        // TODO - Rob 15/01/07 - Updated this to deal with multiple broker_module options
                		// within the main configuration file.
                		//m_nebmods.neb_add_module(varList[0],(varList.length == 2)?varList[1]:"",common_h.TRUE);
                		m_nebmods.neb_add_module(varList[0],varList,common_h.TRUE);
                    }
                }
                
                else if (variable.equals("use_regexp_matching")) blue.use_regexp_matches = handleBoolean(variable,value);
                else if (variable.equals("use_true_regexp_matching")) blue.use_true_regexp_matching = handleBoolean(variable,value);
                else if (variable.equals("daemon_dumps_core")) blue.daemon_dumps_core = handleBoolean(variable,value);
                
                // ignore old/external variables, but at least look for them.
                else if (variable.equals("downtime_file")) ;
                else if (variable.equals("status_file")) ;
                else if (variable.equals("comment_file")) ;
                else if (variable.equals("perfdata_timeout")) ;
                else if (variable.startsWith("host_perfdata")) ;
                else if (variable.startsWith("cfg_file"));
                else if (variable.startsWith("cfg_dir")) ;
                else if (variable.startsWith("state_retention_file")) ;
                else if (variable.startsWith("object_cache_file")) ;
                
                // TODO - Rob 15/01/07 - Added checking for variables that are
                // dealt with by the xpddefault class. We Still need to check for these
                // otherwise we receive an unknown variable error with valid variables.
                
                else if(variable.equals("host_perfdata_file_template")) ;
                else if(variable.equals("service_perfdata_file_template")) ;
                else if(variable.equals("host_perfdata_file_processing_command")) ;
                else if(variable.equals("service_perfdata_file_processing_command")) ;
                else if(variable.equals("service_perfdata_file_mode")) ;
                else if(variable.equals("host_perfdata_file_mode")) ;
                else if(variable.equals("service_perfdata_file")) ;
                else if(variable.equals("host_perfdata_file")) ;
                else if(variable.equals("service_perfdata_file_processing_interval")) ;
                else if(variable.equals("host_perfdata_file_processing_interval")) ;
                else
                {
                    throw new IllegalArgumentException ("UNKOWN VARIABLE");
                }
                
            }
            catch(IllegalArgumentException iaE)
            {
                result = common_h.ERROR;
                logger.fatal("Error in configuration file '"+main_config_file+"' - Line "+lineCounter+" ("+iaE.getMessage()+")");       
            }
        }
        
        try
        {
        	reader.close();
        }
        catch(Exception e)
        {
        	logger.fatal("ERROR: Error closing main config file " + main_config_file);
        }
        
        if(command_check_interval_is_seconds == common_h.FALSE && blue.command_check_interval!=-1)
            blue.command_check_interval *= blue.interval_length;
        
        if((blue.log_file == null) || (blue.log_file.length() == 0))
        {
            logger.fatal( "Error: Log file is not specified anywhere in main config file "+main_config_file+"!" );
            result = common_h.ERROR;
        }
        
        logger.trace("exiting " + cn + ".read_main_config_file" );
        return result;
    }    
    
    /**
     * Method for reading in values set within a resource config file specified within the blue.cfg file.
     * Remember that the resource file is used for specifying user defined macros
     * Currently there is a maximum permitted number of 32 user defined macros.
     * 
     * @param resource_file, the name of the resource file specified.
     * @return //TODO
     */    
   
    public static int read_resource_file(String resource_file)
    {    
        BufferedReader reader;
        
        logger.trace( "entering " + cn + ".read_resource_file" );
        logger.debug( "processing resource file " + resource_file );
                        
        File thefile = new File(resource_file);
        
        /* Make sure that the config file exists and that we can actually read it */
        
        if(!thefile.exists() || !thefile.canRead())
        {
            logger.fatal( "Error: Cannot open resource file "+ resource_file+" for reading! " );
            return common_h.ERROR;
        }
        else
        {
            try
            {
                reader = new BufferedReader(new FileReader(thefile));
            }
            catch(Exception e)
            {
                return common_h.ERROR;
            }
        }
        
        String line = "";
        int lineCounter = 0;
        
        while(line != null)
        {
            try
            {
            	line = reader.readLine();
            }
            catch(Exception e)
            {
            	return common_h.ERROR;
            }
            
            /* Make sure that there is something to read */
            if ( line == null )
            	continue;
            
            line = line.trim();
            lineCounter++;
            
            /* Skip all comments and blank lines */
            
            if ((line.length() == 0 ) || (line.startsWith("#")))
                continue;
            
            /* Split the line to retrieve variable/value pairings */
            
            String[] result = line.split("=");
            
            if ((result.length != 2) || (result[0].trim().length() == 0))
            {
                throw new IllegalArgumentException ( "Error: NULL variable - Line"+lineCounter+" of resource file '"+resource_file+"'" );
                
            }
            else if(result[1].trim().length() == 0)
            {
                throw new IllegalArgumentException ( "Error: NULL variable value - Line"+lineCounter+" of resource file '"+resource_file+"'" );
            }
            
            /* Fetch variable and value from split string */
            
            String variable = result[0].trim();
            String value = result[1].trim();
            
            /* $USERx$ macro declarations */
            
            if(variable.startsWith("$USER") && variable.endsWith("$"))
            {
                /* Retrieve the value of the macro that we are defining */
            	
            	int user_index = Integer.parseInt(variable.substring(5,variable.length()- 1));
                user_index--;
                
                if((user_index >= 0) && (user_index < blue_h.MAX_USER_MACROS))
                {
                    blue.macro_user[user_index] = value;
                }
                
                logger.debug( "$USER"+user_index+"$ set to '"+value+"'" );
            }
        }
        
        try
        {
        	reader.close();
        }
        catch(Exception e)
        {
        	logger.fatal("ERROR: Error closing main config file " + resource_file);
        }
        
        logger.trace( "exiting " + cn + ".read_resource_file" );
        return common_h.OK;
    }
    
    
    /****************************************************************/
    /**************** CONFIG VERIFICATION FUNCTIONS *****************/
    /****************************************************************/
    
    /* do a pre-flight check to make sure object relationships make sense */
    public static int pre_flight_check()
    {
        
    	logger.trace( "entering " + cn + ".pre_flight_check" );
        
        int warnings=0;
        int errors=0;
        int total_objects = 0;
        
        //
        // /*****************************************/
        // /* check sanity of service message size... */
        // /*****************************************/
        // if(sizeof(service_message)>512){
        // writeError("Warning: Size of service_message struct (%d bytes) is >
        // POSIX-guaranteed atomic write size (512 bytes). Service checks results
        // may get lost or mangled!",sizeof(service_message));
        // warnings++;
        // }
        //
        //
        
        /*****************************************/
        /* 			check each service
        /*****************************************/
        
        if(blue.verify_config == common_h.TRUE)
        	logger.info("Checking services...\n");
        
        total_objects = 0;
        
        if ((objects.service_hashlist==null) || ( objects.service_hashlist.size() == 0))
        {
            logger.fatal( "Error: There are no services defined!" );
            errors ++;
        }
        else
        {
            // Walk the list of services.
            for(ListIterator slIterator = objects.service_list.listIterator(); slIterator.hasNext();total_objects++)
            {
                objects_h.service temp_service = (objects_h.service) slIterator.next();
                
                /* check for an associated host! */
                if(objects.find_host(temp_service.host_name) == null)
                {
                    logger.fatal( "Error: Host "+temp_service.host_name+" specified in service "+temp_service.description+" not defined anywhere!" );
                    errors++;
                }
                
                // if the service has an event handler, check it.
                if(temp_service.event_handler!=null)
                {
                    String temp_command_name = temp_service.event_handler.split( "!" )[0];
                    
                    if(objects.find_command(temp_command_name) == null)
                    {
                        logger.fatal("Error: Event handler command '"+temp_command_name+"'specified in service '"+temp_service.description+"' for host '"+temp_service.host_name+"' not defined anywhere");
                        errors++;
                    }
                }
                
                // service check command
                if (temp_service.service_check_command != null)
                {
                    String temp_command_name = temp_service.service_check_command.split( "\\!" )[0];
                    
                    if( objects.find_command(temp_command_name) == null )
                    {
                        logger.fatal( "Error: Service check command '"+temp_command_name+"'specified in service '"+temp_service.description+"' for host'"+temp_service.host_name+"' not defined anywhere" );
                        errors++;
                    }
                }
                
                /* check for sane recovery options */
                if(temp_service.notify_on_recovery == common_h.TRUE && temp_service.notify_on_warning==common_h.FALSE && temp_service.notify_on_critical== common_h.FALSE){
                    logger.warn( "Warning: Recovery notification option in service'"+temp_service.description+"' for host '"+temp_service.host_name+"'doesn't make any sense - specify warning and/or critical options as well");
                    warnings++;
                }
                
                /* check to see if there is at least one contact group */
                if(temp_service.contact_groups == null ){
                    logger.warn( "Warning: Service '"+temp_service.description+"' on host'"+temp_service.host_name+"' has no default contact group(s) defined!" );
                    warnings++;
                } else {
                    /* check for valid contactgroups */
                    for( ListIterator cgmIterator = temp_service.contact_groups.listIterator(); cgmIterator.hasNext(); ){
                        objects_h.contactgroupsmember temp_contactgroupsmember = (objects_h.contactgroupsmember) cgmIterator.next();
                        if( objects.find_contactgroup(temp_contactgroupsmember.group_name) == null){
                            logger.fatal("Error: Contact group '"+temp_contactgroupsmember.group_name+"' specified in service'"+temp_service.description+"' for host '"+temp_service.host_name+"' is not defined anywhere!");
                            errors++;
                        }
                    }
                }
                
                /* verify service check timeperiod */
                if(temp_service.check_period == null ){
                    logger.warn( "Warning: Service '"+temp_service.description+"' on host '"+temp_service.host_name+"' has no check time period defined!" );
                    warnings++;
                } else if ( objects.find_timeperiod(temp_service.check_period) == null){
                    logger.fatal( "Error: Check period '"+temp_service.check_period+"'specified for service '"+temp_service.description+"' on host'"+temp_service.host_name+"' is not defined anywhere!" );
                    errors++;
                }
                
                /* check service notification timeperiod */
                if(temp_service.notification_period==null ){
                    logger.warn( "Warning: Service '"+temp_service.description+"' on host '"+temp_service.host_name+"' has no notification time period defined!" );
                    warnings++;
                } else if( objects.find_timeperiod(temp_service.notification_period) == null ){
                    logger.fatal("Error: Notification period '"+temp_service.notification_period+"' specified for service '"+temp_service.description+"' on host '"+temp_service.host_name+"' is not defined anywhere!");
                    errors++;
                }
                
                /* see if the notification interval is less than the check interval */
                if(temp_service.notification_interval < temp_service.check_interval && temp_service.notification_interval!=0){
                    logger.warn("Warning: Service '"+temp_service.description+"' on host '"+temp_service.host_name+"' has a notification interval less than its check interval! Notifications are only re-sent after checks are made, so the effective notification interval will be that of the check interval.");
                    warnings++;
                }
                
                /* check for illegal characters in service description */
                if(utils.contains_illegal_object_chars(temp_service.description)== common_h.TRUE){
                    logger.fatal("Error: The description string for service '"+temp_service.description+"' on host '"+temp_service.host_name+"' contains one or more illegal characters.");
                    errors++;
                }
                
            }
        }
        if ( blue.verify_config == common_h.TRUE ) logger.info( "Checked " + total_objects + " services." );
        logger.debug( "Completed service verification checks");
        
        
        /*****************************************/
        /* check all hosts... */
        /*****************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checking hosts...\n");
        
        total_objects = 0;
        if(objects.host_hashlist==null){
            logger.fatal( "Error: There are no hosts defined!" );
            errors ++;
        } else {
            
            // Walk the list of hosts.
            for( ListIterator hIterator = objects.host_list.listIterator(); hIterator.hasNext(); total_objects++ ){
                objects_h.host temp_host = (objects_h.host) hIterator.next();
                boolean found = false;
                
                /* make sure each host has at least one service associated with it */
                for( ListIterator serviceIterator = objects.service_list.listIterator(); serviceIterator.hasNext() && !found ; ){
                    objects_h.service temp_service = (objects_h.service) serviceIterator.next();
                    found = temp_service.host_name.equals( temp_host.name );
                }
                if ( !found ) {
                    logger.warn( "Warning: Host '"+temp_host.name+"' has no services associated with it!" );
                    warnings ++;
                }
                
//              #ifdef REMOVED_061303
                /* make sure each host is a member of at least one hostgroup */
                found = false;
                for ( ListIterator hgIterator = objects.hostgroup_list.listIterator(); hgIterator.hasNext() && !found ; ) {
                    objects_h.hostgroup temp_hostgroup = (objects_h.hostgroup) hgIterator.next();
                    found = objects.find_hostgroupmember(temp_host.name, temp_hostgroup) != null;
                }
                
                if ( !found ) {
                    logger.warn("Warning: Host '"+temp_host.name+"' is not a member of any host groups!" );
                }
//              #endif
                
                /* check the event handler command */
                if( temp_host.event_handler != null ){
                    String temp_command_name = temp_host.event_handler.split( "!" )[0];
                    if( objects.find_command(temp_command_name) == null ){
                        logger.fatal( "Error: Event handler command '"+temp_command_name+"' specified for host '"+temp_host.name+"' not defined anywhere" );
                        errors++;
                    }
                }
                
                /* hosts that don't have check commands defined shouldn't ever be checked... */
                // service check command
                if ( temp_host.host_check_command != null ) {
                    String temp_command_name = temp_host.host_check_command.split( "!" )[0];
                    if( objects.find_command(temp_command_name) == null ){
                        logger.fatal( "Error: Host check command '"+temp_command_name+"' specified for host '"+temp_host.name+"' is not defined anywhere!" );
                        errors++;
                    }
                }
                
                /* check host check timeperiod */
                if ( (temp_host.check_period != null ) && (objects.find_timeperiod(temp_host.check_period)==null) ){
                    logger.fatal( "Error: Check period '"+temp_host.check_period+"' specified for host '"+temp_host.name+"' is not defined anywhere!" );
                    errors++;
                }
                
                if ( temp_host.contact_groups == null ) {
                    logger.fatal( "Error: Host '"+temp_host.name+"' has no default contact group(s) defined!" );
                    errors++;
                } else {
                    /* check all contact groups */
                    for( ListIterator cgmIterator = temp_host.contact_groups.listIterator(); cgmIterator.hasNext(); ) {
                        objects_h.contactgroupsmember temp_contactgroupsmember = (objects_h.contactgroupsmember ) cgmIterator.next();
                        if ( objects.find_contactgroup(temp_contactgroupsmember.group_name) == null ) {
                            logger.fatal( "Error: Contact group '"+temp_contactgroupsmember.group_name+"' specified in host '"+temp_host.name+"' is not defined anywhere!" );
                            errors++;
                        }
                    }
                }
                
                /* check notification timeperiod */
                if ( (temp_host.notification_period!=null) && (objects.find_timeperiod(temp_host.notification_period)==null)) {
                    logger.fatal( "Error: Notification period '"+temp_host.notification_period+"' specified for host '"+temp_host.name+"' is not defined anywhere!" );
                    errors++;
                }
                
                /* check all parent parent host */
                if ( temp_host.parent_hosts != null ) {
                    for( ListIterator hmIterator = temp_host.parent_hosts.listIterator(); hmIterator.hasNext(); ) {
                        objects_h.hostsmember temp_hostsmember = (objects_h.hostsmember) hmIterator.next();
                        if ( objects.find_host( temp_hostsmember.host_name) == null ) {
                            logger.fatal("Error: '"+temp_hostsmember.host_name+"' is not a valid parent for host '"+temp_host.name+"'!");
                            errors++;
                        }
                    }
                }
                
                
                /* check for sane recovery options */
                if(temp_host.notify_on_recovery==common_h.TRUE && temp_host.notify_on_down==common_h.FALSE && temp_host.notify_on_unreachable==common_h.FALSE){
                    logger.warn( "Warning: Recovery notification option in host '"+temp_host.name+"' definition doesn't make any sense - specify down and/or unreachable options as well" );
                    warnings++;
                }
                
                /* check for illegal characters in host name */
                if( utils.contains_illegal_object_chars(temp_host.name)==common_h.TRUE){
                    logger.fatal( "Error: The name of host '"+temp_host.name+"' contains one or more illegal characters." );
                    errors++;
                }
            }
        }
        
        if ( blue.verify_config == common_h.TRUE ) logger.info( "Checked " + total_objects + " hosts." );
        logger.debug("Completed host verification checks.");
        
        /*****************************************/
        /* check each host group... */
        /*****************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info( "Checking host groups...");
        
        total_objects = 0;
        if ( (objects.hostgroup_list==null) || ( objects.hostgroup_list.size() ==0 ) ){
            logger.fatal( "Error: There are no host groups defined!" );
            errors++;
        } else {
            
            for ( ListIterator hgIterator = objects.hostgroup_list.listIterator(); hgIterator.hasNext(); total_objects++ ) {
                objects_h.hostgroup temp_hostgroup = (objects_h.hostgroup) hgIterator.next();
                
                /* check all group members */
                for ( ListIterator hgmIterator = temp_hostgroup.members.listIterator(); hgmIterator.hasNext(); ) {
                    objects_h.hostgroupmember temp_hostgroupmember = (objects_h.hostgroupmember) hgmIterator.next();
                    if ( objects.find_host( temp_hostgroupmember.host_name) == null ) {
                        logger.fatal("Error: Host '"+temp_hostgroupmember.host_name+"' specified in host group '"+temp_hostgroup.group_name+"' is not defined anywhere!" );
                        errors++;
                    }
                }
                
                /* check for illegal characters in hostgroup name */
                if( utils.contains_illegal_object_chars(temp_hostgroup.group_name)==common_h.TRUE){
                    logger.fatal( "Error: The name of hostgroup '"+temp_hostgroup.group_name+"' contains one or more illegal characters.");
                    errors++;
                }
            }
        }
        
        if ( blue.verify_config == common_h.TRUE ) logger.info( "Checked " + total_objects + " host groups." );
        logger.debug( "Completed hostgroup verification checks" );
        
        /*****************************************/
        /* check each service group... */
        /*****************************************/
        total_objects = 0;
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checking service groups...");
        if ( objects.servicegroup_list != null ) {
            for ( ListIterator sgIterator = objects.servicegroup_list.listIterator(); sgIterator.hasNext(); total_objects++ ) {
                objects_h.servicegroup temp_servicegroup = (objects_h.servicegroup) sgIterator.next();
                
                /* check all group members */
                for ( ListIterator sgmIterator =
                    temp_servicegroup.members.listIterator(); sgmIterator.hasNext(); ) {
                    objects_h.servicegroupmember temp_servicegroupmember = (objects_h.servicegroupmember) sgmIterator.next();
                    if ( objects.find_service( temp_servicegroupmember.host_name, temp_servicegroupmember.service_description) == null ) {
                        logger.fatal( "Error: Service '"+temp_servicegroupmember.service_description+"' on host '"+temp_servicegroupmember.host_name+"' specified in service group '"+temp_servicegroup.group_name+"' is not defined anywhere!" );
                        errors++;
                    }
                }
                
                /* check for illegal characters in servicegroup name */
                if( utils.contains_illegal_object_chars(temp_servicegroup.group_name)== common_h.TRUE){
                    logger.fatal( "Error: The name of servicegroup '"+temp_servicegroup.group_name+"' contains one or more illegal characters." );
                    errors++;
                }
            }
        }
        
        if ( blue.verify_config == common_h.TRUE ) logger.info( "Checked " + total_objects + " service groups." );
        logger.debug( "Completed servicegroup verification checks");
        
        /*****************************************/
        /* check all contacts... */
        /*****************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info( "Checking contacts...\n");
        total_objects = 0;   
        if ( (objects.contact_list == null ) || ( objects.contact_list.size() == 0 )){
            logger.fatal( "Error: There are no contacts defined!" );
            errors++;
        } else {
            
            for ( ListIterator cIterator = objects.contact_list.listIterator(); cIterator.hasNext(); total_objects++ ) {
                objects_h.contact temp_contact = (objects_h.contact) cIterator.next();
                
                /* check service notification commands */
                if(temp_contact.service_notification_commands== null || temp_contact.service_notification_commands.isEmpty() ) {
                    logger.fatal( "Error: Contact '"+temp_contact.name+"' has no service notification commands defined!");
                    errors++;
                } else {
                    for ( ListIterator cmIterator = temp_contact.service_notification_commands.listIterator(); cmIterator.hasNext(); ) {
                        objects_h.commandsmember temp_commandsmember = (objects_h.commandsmember) cmIterator.next();
                        String temp_command_name = temp_commandsmember.command.split( "!" )[0];
                        if( objects.find_command(temp_command_name) == null ){
                            logger.fatal( "Error: Service notification command '"+temp_command_name+"' specified for contact '"+temp_contact.name+"' is not defined anywhere!" );
                            errors++;
                        }
                    }
                }
                
                /* check host notification commands */
                if( temp_contact.host_notification_commands== null || temp_contact.host_notification_commands.isEmpty() ){
                    logger.fatal( "Error: Contact '"+temp_contact.name+"' has no host notification commands defined!");
                    errors++;
                } else {
                    for ( ListIterator cmIterator = temp_contact.host_notification_commands.listIterator(); cmIterator.hasNext(); ) {
                        objects_h.commandsmember temp_commandsmember = (objects_h.commandsmember) cmIterator.next();
                        String temp_command_name = temp_commandsmember.command.split( "!" )[0];
                        if( objects.find_command(temp_command_name) == null ){
                            logger.fatal("Error: Host notification command '"+temp_command_name+"' specified for contact '"+temp_contact.name+"' is not defined anywhere!");
                            errors++;
                        }
                    }
                }
                
                /* check service notification timeperiod */
                if( temp_contact.service_notification_period == null){
                    logger.warn("Warning: Contact '"+temp_contact.name+"' has no service notification time period defined!");
                    warnings++;
                } else
                    if( objects.find_timeperiod(temp_contact.service_notification_period) == null ){
                        logger.fatal( "Error: Service notification period '"+temp_contact.service_notification_period+"' specified for contact '"+temp_contact.name+"' is not defined anywhere!");
                        errors++;
                    }
                
                /* check host notification timeperiod */
                if( temp_contact.host_notification_period == null ){
                    logger.warn("Warning: Contact '"+temp_contact.name+"' has no host notification time period defined!");
                    warnings++;
                } else if ( objects.find_timeperiod(temp_contact.host_notification_period) == null ) {
                    logger.fatal("Error: Host notification period '"+temp_contact.host_notification_period+"' specified for contact '"+temp_contact.name+"' is not defined anywhere!");
                    errors++;
                }
                
                boolean found = false;
                /* make sure the contact belongs to at least one contact group */
                for ( ListIterator cgIterator = objects.contactgroup_list.listIterator(); cgIterator.hasNext() && !found; ) {
                    objects_h.contactgroup temp_contactgroup = (objects_h.contactgroup) cgIterator.next();
                    found = ( objects.find_contactgroupmember( temp_contact.name, temp_contactgroup ) != null );
                }
                /* we couldn't find the contact in any contact groups */
                if( !found ){
                    logger.warn("Warning: Contact '"+temp_contact.name+"' is not a member of any contact groups!");
                    warnings++;
                }
                
                /* check for sane host recovery options */
                if( (temp_contact.notify_on_host_recovery ==common_h.TRUE) && (temp_contact.notify_on_host_down == common_h.FALSE) && (temp_contact.notify_on_host_unreachable==common_h.FALSE)){
                    logger.warn("Warning: Host recovery notification option for contact '"+temp_contact.name+"' doesn't make any sense - specify down and/or unreachable options as well");
                    warnings++;
                }
                
                /* check for sane service recovery options */
                if( (temp_contact.notify_on_service_recovery == common_h.TRUE) && (temp_contact.notify_on_service_critical == common_h.FALSE)&& (temp_contact.notify_on_service_warning== common_h.FALSE)){
                    logger.warn("Warning: Service recovery notification option for contact '"+temp_contact.name+"' doesn't make any sense - specify critical and/or warning options as well");
                    warnings++;
                }
                
                /* check for illegal characters in contact name */
                if( utils.contains_illegal_object_chars(temp_contact.name)== common_h.TRUE){
                    logger.fatal("Error: The name of contact '"+temp_contact.name+"' contains one or more illegal characters.");
                    errors++;
                }
            }
        }
        
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checked "+total_objects+" contacts.");
        logger.debug("Completed contact verification checks");
        
        /*****************************************/
        /* check each contact group... */
        /*****************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checking contact groups...");
        total_objects = 0;
        if ( (objects.contactgroup_list== null) || (objects.contactgroup_list.size()==0 ) ){
            logger.fatal("Error: There are no contact groups defined!");
            errors++;
        } else {
            
            /* validate all contactgroups */
            for ( ListIterator cgIterator = objects.contactgroup_list.listIterator(); cgIterator.hasNext(); total_objects++ ) {
                objects_h.contactgroup temp_contactgroup = (objects_h.contactgroup) cgIterator.next();
                
                /* make sure each contactgroup is used in at least one host or service
                 definition or escalation */
                boolean found = false;
                for ( ListIterator hIterator = objects.host_list.listIterator(); hIterator.hasNext() && !found ; ) {
                    objects_h.host temp_host = (objects_h.host) hIterator.next();
                    for ( ListIterator cgmIterator = temp_host.contact_groups.listIterator();
                    cgmIterator.hasNext() && !found ; ) {
                        objects_h.contactgroupsmember temp_contactgroupsmember = (objects_h.contactgroupsmember) cgmIterator.next();
                        found = temp_contactgroupsmember.group_name.equals( temp_contactgroup.group_name );
                    }
                }
                
                for ( ListIterator sIterator = objects.service_list.listIterator(); sIterator.hasNext() && !found; ) {
                    objects_h.service temp_service = (objects_h.service) sIterator.next();
                    for ( ListIterator cgmIterator = temp_service.contact_groups.listIterator(); cgmIterator.hasNext() && !found ; ) {
                        objects_h.contactgroupsmember temp_contactgroupsmember = (objects_h.contactgroupsmember) cgmIterator.next();
                        found = temp_contactgroupsmember.group_name.equals( temp_contactgroup.group_name );
                    }
                }
                for ( ListIterator seIterator = objects.serviceescalation_list.listIterator(); seIterator.hasNext() && !found; ) {
                    objects_h.serviceescalation temp_se = (objects_h.serviceescalation) seIterator.next();
                    for ( ListIterator cgmIterator = temp_se.contact_groups.listIterator(); cgmIterator.hasNext() && !found ; ) {
                        objects_h.contactgroupsmember temp_contactgroupsmember = (objects_h.contactgroupsmember) cgmIterator.next();
                        found = temp_contactgroupsmember.group_name.equals( temp_contactgroup.group_name );
                    }
                }
                for ( ListIterator heIterator = objects.hostescalation_list.listIterator(); heIterator.hasNext() && !found ; ) {
                    objects_h.hostescalation temp_he = (objects_h.hostescalation) heIterator.next();
                    for ( ListIterator cgmIterator = temp_he.contact_groups.listIterator(); cgmIterator.hasNext() && !found ; ) {
                        objects_h.contactgroupsmember temp_contactgroupsmember = (objects_h.contactgroupsmember) cgmIterator.next();
                        found = temp_contactgroupsmember.group_name.equals( temp_contactgroup.group_name );
                    }
                }
                
                /* we couldn't find a hostgroup or service */
                if( !found ){
                    logger.warn( "Warning: Contact group '"+temp_contactgroup.group_name+"' is not used in any host/service definitions or host/service escalations!");
                    warnings++;
                }
                
                /* check all the group members */
                for ( ListIterator cgmIterator = temp_contactgroup.members.listIterator(); cgmIterator.hasNext(); ) {
                    objects_h.contactgroupmember temp_contactgroupmember = (objects_h.contactgroupmember) cgmIterator.next();
                    if ( objects.find_contact( temp_contactgroupmember.contact_name ) == null ) {
                        logger.fatal( "Error: Contact '"+temp_contactgroupmember.contact_name+"' specified in contact group '"+temp_contactgroup.group_name+"' is not defined anywhere!" );
                        errors++;
                    }
                }
                
                /* check for illegal characters in contactgroup name */
                if( utils.contains_illegal_object_chars(temp_contactgroup.group_name) == common_h.TRUE){
                    logger.fatal( "Error: The name of contact group '"+temp_contactgroup.group_name+"' contains one or more illegal characters.");
                    errors++;
                }
            }
        }
        
        if ( blue.verify_config == common_h.TRUE ) logger.info( "Checked "+ total_objects +" contact groups." );
        logger.debug("Completed contact group verification checks");
        
        /*****************************************/
        /* check all service escalations... */
        /*****************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checking service escalations...");
        
        total_objects = 0;
        if ( objects.serviceescalation_list != null ) {
            for ( ListIterator seIterator = objects.serviceescalation_list.listIterator(); seIterator.hasNext(); total_objects++) {
                objects_h.serviceescalation temp_se = (objects_h.serviceescalation) seIterator.next();
                
                /* find the service */
                if( objects.find_service(temp_se.host_name,temp_se.description)== null ){
                    logger.fatal("Error: Service '"+temp_se.description+"' on host '"+temp_se.host_name+"' specified in service escalation is not defined anywhere!");
                    errors++;
                }
                
                /* find the timeperiod */
                if ( (temp_se.escalation_period!= null) && ( objects.find_timeperiod(temp_se.escalation_period)== null ) ) {
                    logger.fatal("Error: Escalation period '"+temp_se.escalation_period+"' specified in service escalation for service '"+temp_se.description+"' on host '"+temp_se.host_name+"' is not defined anywhere!");
                    errors++;
                }
                
                /* find the contact groups */
                for ( ListIterator cgmIterator = temp_se.contact_groups.listIterator(); cgmIterator.hasNext(); ) {
                    objects_h.contactgroupsmember temp_contactgroupsmember = (objects_h.contactgroupsmember) cgmIterator.next();
                    
                    if ( objects.find_contactgroup(temp_contactgroupsmember.group_name) ==  null ) {
                        logger.fatal( "Error: Contact group '"+temp_contactgroupsmember.group_name+"' specified in service escalation for service '"+temp_se.description+"' on host '"+temp_se.host_name+"' is not defined anywhere!");
                        errors++;
                    }
                }
            }
        }
        
        if ( blue.verify_config == common_h.TRUE ) logger.info( "\tChecked "+ total_objects +" service escalations.");
        logger.debug("Completed service escalation checks");
        
        /*****************************************/
        /* check all service dependencies... */
        /*****************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checking service dependencies...");
        total_objects = 0;
        if ( objects.servicedependency_list != null ) {
            for ( ListIterator sdIterator = objects.servicedependency_list.listIterator(); sdIterator.hasNext(); total_objects++) {
                objects_h.servicedependency temp_sd = (objects_h.servicedependency) sdIterator.next();
                
                /* find the dependent service */
                objects_h.service temp_service = objects.find_service(temp_sd.dependent_host_name,temp_sd.dependent_service_description);
                if( temp_service == null ){
                    logger.fatal("Error: Dependent service '"+temp_sd.dependent_service_description+"' on host '"+temp_sd.dependent_host_name+"' specified in service dependency for service '"+temp_sd.service_description+"' on host '"+temp_sd.host_name+"' is not defined anywhere!");
                    errors++;
                }
                
                /* find the service we're depending on */
                objects_h.service temp_service2 = objects.find_service(temp_sd.host_name,temp_sd.service_description);
                if( temp_service2 == null ){
                    logger.fatal ("Error: Service specified in service dependency for service '"+temp_sd.dependent_service_description+"' on host '"+temp_sd.dependent_host_name+"' is not defined anywhere!");
                    errors++;
                }
                
                /* make sure they're not the same service */
                if( temp_service==temp_service2){
                    logger.fatal( "Error: Service '"+temp_sd.service_description+"' on host '"+temp_sd.host_name+"' specified in service dependency for service '"+temp_sd.dependent_service_description+"' on host '"+temp_sd.dependent_host_name+"' is not defined anywhere!");
                    errors++;
                }
            }
        }
        
        if ( blue.verify_config == common_h.TRUE )
           logger.info("Checked "+total_objects+" service dependencies.");
        
        logger.debug("Completed service dependency checks");
        
        /*****************************************/
        /* check all host escalations... */
        /*****************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checking host escalations...");
        total_objects = 0;
        if ( objects.hostescalation_list != null ) {
            for( ListIterator heIterator = objects.hostescalation_list.listIterator(); heIterator.hasNext(); total_objects++) {
                objects_h.hostescalation temp_he = (objects_h.hostescalation) heIterator.next();
                
                /* find the host */
                if( objects.find_host(temp_he.host_name)== null ){
                    logger.fatal("Error: Host '"+temp_he.host_name+"' specified in host escalation is not defined anywhere!");
                    errors++;
                }
                
                /* find the timeperiod */
                if ( (temp_he.escalation_period!=null) && ( objects.find_timeperiod(temp_he.escalation_period) == null )) {
                    logger.fatal("Error: Escalation period '"+temp_he.escalation_period+"' specified in host escalation for host '"+temp_he.host_name+"' is not defined anywhere!");
                    errors++;
                }
                
                /* find the contact groups */
                for ( ListIterator cgmIterator = temp_he.contact_groups.listIterator(); cgmIterator.hasNext(); ) {
                    objects_h.contactgroupsmember temp_contactgroupsmember = (objects_h.contactgroupsmember) cgmIterator.next();
                    if ( objects.find_contactgroup(temp_contactgroupsmember.group_name) == null ) {
                        logger.fatal( "Error: Contact group '"+temp_contactgroupsmember.group_name+"' specified in host escalation for host '"+temp_he.host_name+"' is not defined anywhere!" );
                        errors++;
                    }
                }
            }
        }
        
        if ( blue.verify_config == common_h.TRUE ) logger.info( "Checked "+ total_objects+" host escalations.");
        logger.debug( "Completed host escalation checks");
        
        /*****************************************/
        /* check all host dependencies... */
        /*****************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checking host dependencies...");
        total_objects = 0;
        if ( objects.hostdependency_list != null ) {
            for ( ListIterator hdIterator = objects.hostdependency_list.listIterator(); hdIterator.hasNext(); total_objects++) {
                objects_h.hostdependency temp_hd = (objects_h.hostdependency) hdIterator.next();
                
                /* find the dependent host */
                objects_h.host temp_host = objects.find_host(temp_hd.dependent_host_name);
                if(temp_host==null){
                    logger.fatal("Error: Dependent host specified in host dependency for host '"+temp_hd.dependent_host_name+"' is not defined anywhere!");
                    errors++;
                }
                
                /* find the host we're depending on */
                objects_h.host temp_host2=objects.find_host(temp_hd.host_name);
                if(temp_host2==null){
                    logger.fatal("Error: Host specified in host dependency for host '"+temp_hd.dependent_host_name+"' is not defined anywhere!");
                    errors++;
                }
                
                /* make sure they're not the same host */
                if(temp_host == temp_host2){
                    logger.fatal("Error: Host dependency definition for host '"+temp_hd.dependent_host_name+"' is circular (it depends on itself)!");
                    errors++;
                }
            }
        }
        
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checked "+total_objects+" host dependencies.");
        logger.debug("Completed host dependency checks");
        
        /*****************************************/
        /* check all commands... */
        /*****************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checking commands...");
        total_objects = 0;
        if ( objects.command_list != null ) {
            for(ListIterator cIterator = objects.command_list.listIterator(); cIterator.hasNext(); total_objects++) {
                objects_h.command temp_command = (objects_h.command) cIterator.next();
                /* check for illegal characters in command name */
                if(utils.contains_illegal_object_chars(temp_command.name)== common_h.TRUE){
                    logger.fatal("Error: The name of command '"+temp_command.name+"' contains one or more illegal characters.");
                    errors++;
                }
            }
        }
        
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checked " + total_objects + " commands.\n");
        logger.debug("Completed command checks");
        
        /*****************************************/
        /* check all timeperiods... */
        /*****************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info( "Checking time periods...");
        total_objects = 0;
        if ( objects.timeperiod_list != null ) {
            for ( ListIterator tpIterator = objects.timeperiod_list.listIterator(); tpIterator.hasNext(); total_objects++ ) {
                objects_h.timeperiod temp_timeperiod = (objects_h.timeperiod) tpIterator.next();
                /* check for illegal characters in timeperiod name */
                if(utils.contains_illegal_object_chars(temp_timeperiod.name)==common_h.TRUE){
                    logger.fatal( "Error: The name of time period '"+temp_timeperiod.name+"' contains one or more illegal characters." );
                    errors++;
                }
            }
        }
        
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checked "+total_objects+" time periods.\n" );
        logger.debug("Completed command checks");
        
        /*****************************************/
        /* check extended host information... */
        /*****************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checking extended host info definitions...");
        total_objects = 0;
        if ( objects.hostextinfo_list != null ) {
            for ( ListIterator heiIterator = objects.hostextinfo_list.listIterator(); heiIterator.hasNext(); total_objects++ ) {
                objects_h.hostextinfo temp_hostextinfo = (objects_h.hostextinfo) heiIterator.next();
                /* find the host */
                if( objects.find_host(temp_hostextinfo.host_name)== null ){
                    logger.fatal("Error: Host '"+temp_hostextinfo.host_name+"' specified in extended host information is not defined anywhere!");
                    errors++;
                }
            }
        }
        
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checked "+total_objects+" extended host info definitions.");
        logger.debug("Completed extended host info checks");
        
        /*****************************************/
        /* check extended service information... */
        /*****************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checking extended service info definitions...");
        total_objects = 0;
        if ( objects.serviceextinfo_list != null ) {
            for ( ListIterator seiIterator = objects.serviceextinfo_list.listIterator(); seiIterator.hasNext(); total_objects++ ) {
                objects_h.serviceextinfo temp_serviceextinfo = (objects_h.serviceextinfo) seiIterator.next();
                /* find the service */
                if( objects.find_service(temp_serviceextinfo.host_name,temp_serviceextinfo.description)== null){
                    logger.fatal( "Error: Service '"+temp_serviceextinfo.description+"' on host '"+temp_serviceextinfo.host_name+"' specified in extended service information is not defined anywhere!");
                    errors++;
                }
            }
        }
        
        if ( blue.verify_config == common_h.TRUE ) 
           logger.info("Checked "+ total_objects+" extended service info definitions.");
        
        logger.debug("Completed extended service info checks");
        
        /********************************************/
        /* check for circular paths between hosts */
        /********************************************/
        if ( blue.verify_config == common_h.TRUE ) 
           logger.info("Checking for circular paths between hosts...");
        
        /* check routes between all hosts */
        if ( objects.host_list != null ) {
            for ( ListIterator hIterator = objects.host_list.listIterator(); hIterator.hasNext();  ) {
                objects_h.host temp_host = (objects_h.host) hIterator.next();

                /* clear checked flag for all hosts */
                for ( objects_h.host clear_host : (ArrayList<objects_h.host>) objects.host_list )
                   clear_host.circular_path_checked = common_h.FALSE;
                
                if( objects.check_for_circular_path(temp_host,temp_host)==common_h.TRUE){
                    logger.fatal("Error: There is a circular parent/child path that exists for host '"+temp_host.name+"'!");
                    errors++;
                    break;
                }
            }
        }
        
        logger.debug("Completed circular path checks");
        
        /********************************************/
        /* check for circular dependencies */
        /********************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checking for circular host and service dependencies...");
        
        /* check execution dependencies between all services */
        if ( objects.servicedependency_list != null ) {
            for ( ListIterator sdIterator = objects.servicedependency_list.listIterator(); sdIterator.hasNext(); ) {
                objects_h.servicedependency temp_sd = (objects_h.servicedependency) sdIterator.next();
                
                /* clear checked flag for all dependencies */
                for ( objects_h.servicedependency clear_servicedependency : (ArrayList<objects_h.servicedependency>) objects.servicedependency_list )
                   clear_servicedependency.circular_path_checked = common_h.FALSE;
                
                if( objects.check_for_circular_servicedependency(temp_sd,temp_sd, common_h.EXECUTION_DEPENDENCY) == common_h.TRUE ){
                    logger.fatal("Error: A circular execution dependency (which could result in a deadlock) exists for service '"+temp_sd.service_description+"' on host '"+temp_sd.host_name+"'!");
                    errors++;
                }
            }
            
            /* check notification dependencies between all services */
            for ( ListIterator sdIterator = objects.servicedependency_list.listIterator(); sdIterator.hasNext(); ) {
                objects_h.servicedependency temp_sd = (objects_h.servicedependency) sdIterator.next();
                
                /* clear checked flag for all dependencies */
                for ( objects_h.servicedependency clear_servicedependency : (ArrayList<objects_h.servicedependency>) objects.servicedependency_list )
                   clear_servicedependency.circular_path_checked = common_h.FALSE;
                
                if( objects.check_for_circular_servicedependency(temp_sd,temp_sd, common_h.NOTIFICATION_DEPENDENCY) == common_h.TRUE ){
                    logger.fatal("Error: A circular notification dependency (which could result in a deadlock) exists for service '"+temp_sd.service_description+"' on host '"+temp_sd.host_name+"'!");
                    errors++;
                }
            }
        }
        
        /* check execution dependencies between all hosts */
        if ( objects.hostdependency_list != null ) {
            for ( ListIterator hdIterator = objects.hostdependency_list.listIterator(); hdIterator.hasNext(); ) {
                objects_h.hostdependency temp_hd = (objects_h.hostdependency) hdIterator.next();
                
                /* clear checked flag for all dependencies */
                for ( objects_h.hostdependency clear_hostdependency : (ArrayList<objects_h.hostdependency>) objects.hostdependency_list )
                   clear_hostdependency.circular_path_checked = common_h.FALSE;
                
                if( objects.check_for_circular_hostdependency(temp_hd,temp_hd,common_h.EXECUTION_DEPENDENCY) == common_h.TRUE ){
                    logger.fatal("Error: A circular execution dependency (which could result in a deadlock) exists for host '"+temp_hd.host_name+"'!");
                    errors++;
                }
            }
            
            /* check notification dependencies between all hosts */
            for ( ListIterator hdIterator = objects.hostdependency_list.listIterator(); hdIterator.hasNext(); ) {
                objects_h.hostdependency temp_hd = (objects_h.hostdependency) hdIterator.next();
                
                /* clear checked flag for all dependencies */
                for ( objects_h.hostdependency clear_hostdependency : (ArrayList<objects_h.hostdependency>) objects.hostdependency_list )
                   clear_hostdependency.circular_path_checked = common_h.FALSE;
                
                if( objects.check_for_circular_hostdependency(temp_hd,temp_hd,common_h.NOTIFICATION_DEPENDENCY) == common_h.TRUE ){
                    logger.fatal("Error: A circular notification dependency (which could result in a deadlock) exists for host '"+temp_hd.host_name+"'!");
                    errors++;
                }
            }
        }
        
        logger.debug("Completed circular host and service dependency checks");
        
        /********************************************/
        /* check global event handler commands... */
        /********************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checking global event handlers...");
        
        if(blue.global_host_event_handler!=null){
            String temp_command_name = blue.global_host_event_handler.split( "!" )[0];
            if( objects.find_command(temp_command_name) == null ){
                logger.fatal( "Error: Global host event handler command '"+temp_command_name+"' is not defined anywhere!" );
                errors++;
            }
        }
        
        if( blue.global_service_event_handler!=null){
            String temp_command_name = blue.global_service_event_handler.split( "!" )[0];
            if( objects.find_command(temp_command_name) == null ){
                logger.fatal( "Error: Global service event handler command '"+temp_command_name+"' is not defined anywhere!" );
                errors++;
            }
        }
        
        logger.debug("Completed global event handler command checks");
        
        /**************************************************/
        /* check obsessive processor commands... */
        /**************************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checking obsessive compulsive processor commands...");
        
        if(blue.ocsp_command!=null){
            String temp_command_name = blue.ocsp_command.split( "!" )[0];
            if( objects.find_command(temp_command_name) == null ){
                logger.fatal( "Error: Obsessive compulsive service processor command '"+temp_command_name+"' is not defined anywhere!" );
                errors++;
            }
        }
        
        if(blue.ochp_command!= null ){
            String temp_command_name = blue.ochp_command.split( "!" )[0];
            if( objects.find_command(temp_command_name) == null ){
                logger.fatal( "Error: Obsessive compulsive host processor command '"+temp_command_name+"' is not defined anywhere!" );
                errors++;
            }
        }
        
        logger.debug("Completed obsessive compulsive processor command checks");
        
        /**************************************************/
        /* check various settings... */
        /**************************************************/
        if ( blue.verify_config == common_h.TRUE ) logger.info("Checking misc settings...");
        
        /* warn if user didn't specify any illegal macro output chars */
        if( blue.illegal_output_chars == null){
            logger.fatal("Warning: Nothing specified for illegal_macro_output_chars variable!");
            warnings++;
        }
        
        /* count number of services associated with each host (we need this for flap detection)... */
        if ( objects.service_list != null ) {
            for ( ListIterator sIterator = objects.service_list.listIterator(); sIterator.hasNext(); ) {
                objects_h.service temp_service = (objects_h.service) sIterator.next();
                objects_h.host temp_host = objects.find_host( temp_service.host_name );
                if ( temp_host != null ) {
                    temp_host.total_services++;
                    temp_host.total_service_check_interval+=temp_service.check_interval;
                }
            }
        }
        
        if ( blue.verify_config == common_h.TRUE ) logger.info("Total Warnings: " + warnings);
        if ( blue.verify_config == common_h.TRUE ) logger.info("Total Errors:   " + errors);
        
        logger.trace( "exiting " + cn + ".pre_flight_check" );
        
        return (errors>0)? common_h.ERROR: common_h.OK;
    }
    
    
    /**
     *  Method that handles setting of file names within blue config.
     * 
     * @param = int index, position within blue.macro_x array that information should be stored
     * @param = string variable, the variable that is to be set
     * @param = string value, the value of the variable to be set.
     * 
     * @ return  = String, the value of the of variable. 
     */
    
    private static String handleFile(int index, String variable, String value)
    {
        /* Verify that specified file name is within acceptable parameter lengths */
    	
        if(value.length() > common_h.MAX_FILENAME_LENGTH-1){
            throw new IllegalArgumentException(variable + "is too long");
        }
        
        /* Verify that we are inserting the value in a logical position within the array */
        
        if (index >= 0 ) {
            blue.macro_x[index] = value;
        }
        
        logger.debug( variable + " set to " + value );
        return value;
    }        
    
    /**
     * Method that handles setting of string values within blue config
     * 
     * @param = String, variable to be set;
     * @param = String, value of variable to be set;
     * 
     * @return = String, value of variable that has been set.
     */
    
    private static String handleString(String variable, String value )
    {
        logger.debug( variable + " set to " + value );
        return value;
    }
    
    /**
     * Method for dealing with boolean values within blue config.
     * @param variable, the variable to be set
     * @param value, the value of the variable to be set.
     * 
     * @return, boolean value of variable just set. 
     */
    
    private static int handleBoolean(String variable,String value)
    {
        
    	/* Make sure that we have either a 0 || 1 for the value (we are dealing with boolean) */
    	
    	if( value.length()!=1 || value.charAt(0)<'0' || value.charAt(0)>'1')
        {
            throw new IllegalArgumentException ("Illegal value for " + variable );
        }
        
        boolean result = value.charAt(0) == '1';
        
        logger.debug( variable + " set to " +  result);
        
        return result?common_h.TRUE:common_h.FALSE;
    }
    
    /**
     * Method for handling positive integers within blue config.
     * @param variable, the variable to be set.
     * @param value, the value of the variable to be set.
     * 
     * @return, int value of the variable set.
     */
    
    private static int handlePositiveInt(String variable, String value ){
    
    	int result = -1;
        
        try
        {
            result = Integer.parseInt(value);
        }
        catch(NumberFormatException nfE)
        {
            ;
        }
        
        if(result < 0)
        {
            throw new IllegalArgumentException(  "Illegal value for " + variable );
        }
        
        logger.debug( variable + " set to " +  result);
        return result;
    }
    
    /**
     * Method for handling int with possible negative value within blue config.
     * @param variable, the variable to be set.
     * @param value, the value of the variable to be set.
     * 
     * @return, the value of the variable
     */
    
    private static int handleInt(String variable, String value)
    {
        int result = -1;
        
        try
        {
            result = Integer.parseInt(value);
        }
        catch(NumberFormatException nfE)
        {
            throw new IllegalArgumentException("Illegal value for " + variable);
        }
        
        logger.debug(variable + " set to " +  result);
        return result;
    }
    
    /**
     * Method for handling positive doubles within blue config. 
     * @param variable, the variable to be set.
     * @param value, the value of the variable to be set.
     * 
     * @return, the value of the variable.
     */
    
    private static double handlePositiveDouble( String variable, String value )
    {
        double result = -1;
        
        try
        {
            result = Double.parseDouble(value);
        }
        catch(NumberFormatException nfE)
        {
            ;
        }
        
        if(result <= 0.0)
        {
            throw new IllegalArgumentException("Illegal value for " + variable);
        }
        
        logger.debug( variable + " set to " +  result);
        return result;
    }
    
}
