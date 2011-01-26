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

package org.blue.star.xdata;

/*********** COMMON HEADER FILES ***********/
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.base.blue;
import org.blue.star.base.utils;
import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.blue_h;
import org.blue.star.include.common_h;
import org.blue.star.include.locations_h;
import org.blue.star.include.objects_h;
import org.blue.star.include.statusdata_h;

public class xsddefault {

    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.xdata.xsddefault");
    private static String cn = "org.blue.xdata.xsddefault";

    public static String xsddefault_status_log = "";
    public static String xsddefault_temp_file = "";
    
    /******************************************************************/
    /***************** COMMON CONFIG INITIALIZATION  ******************/
    /******************************************************************/
    
    /* grab configuration information */
    public static int xsddefault_grab_config_info(String config_file){

        /*** CORE PASSES IN MAIN CONFIG FILE, CGIS PASS IN CGI CONFIG FILE! ***/
        
        /* initialize the location of the status log */
        xsddefault_status_log = locations_h.DEFAULT_STATUS_FILE;
        xsddefault_temp_file = locations_h.DEFAULT_TEMP_FILE;
        
        /* open the config file for reading */
        blue_h.mmapfile thefile = utils.file_functions.mmap_fopen( config_file );
        if( thefile == null )
            return common_h.ERROR;
        
        /* read in all lines from the main config file */
        while( true ){
            
            /* read the next line */
            String input = utils.file_functions.mmap_fgets(thefile);
            if ( input == null )
                break;
            
            input = input.trim();
            
            /* skip blank lines and comments */
            if( input.length() == 0 || input.charAt(0) =='#' )
                continue;
            
            if ( blue.is_core )
            {
                /* core reads variables directly from the main config file */
                xsddefault_grab_config_directives(input); 
                
            }
            else if ( input.startsWith("main_config_file="))
            {
                String config_file2 = input.substring( input.indexOf("=")+1 );
                /* open the config file for reading */
                blue_h.mmapfile thefile2 = utils.file_functions.mmap_fopen( config_file2 );
                if( thefile2 == null )
                    continue;
                
                /* read in all lines from the main config file */
                while( true ){
                    
                    /* read the next line */
                    String input2 = utils.file_functions.mmap_fgets(thefile2);
                    if ( input2 == null )
                        break;
                    
                    input2 = input2.trim();
                    
                    /* skip blank lines and comments */
                    if(input2.length() == 0 ||  input2.charAt(0) =='#' )
                        continue;
                    
                    /* core reads variables directly from the main config file */
                    xsddefault_grab_config_directives(input2); 
                }
            }
            
            /* core reads variables directly from the main config file */
            //xsddefault_grab_config_directives(input);
            // TODO - Rob 16/01/07 - We've already done this.
        }
        
        /* free memory and close the file */
        utils.file_functions.mmap_fclose(thefile);
        
        /* we didn't find the status log name */
        if( xsddefault_status_log.length() == 0 )
            return common_h.ERROR;
        
        /* we didn't find the temp file */
        if( xsddefault_temp_file.length() == 0 )
            return common_h.ERROR;
        
        if (blue.is_core)
            /* save the status file macro */
            blue.macro_x [ blue_h.MACRO_STATUSDATAFILE ]  = xsddefault_status_log;
        
        return common_h.OK;
    }
    
    
    public static void xsddefault_grab_config_directives( String input_buffer){
        
        /* status log definition */
        if ( input_buffer.startsWith( "status_file") || input_buffer.startsWith( "xsddefault_status_log" ))
        {
            int index = input_buffer.indexOf( "=");
            if ( index <0 || index == input_buffer.length() )
                return;
            xsddefault_status_log = input_buffer.substring( index + 1 );
        } 
        
        /* temp file definition */
        else if( input_buffer.startsWith( "temp_file") || input_buffer.startsWith("xsddefault_temp_file" ) )
        {
            int index = input_buffer.indexOf( "=");
            if ( index <0 || index == input_buffer.length() )
                return;
            xsddefault_temp_file = input_buffer.substring( index + 1);
        }
        return;
    }
    
    
    /******************************************************************/
    /********************* INIT/CLEANUP FUNCTIONS *********************/
    /******************************************************************/
    
    
    /* initialize status data */
    public static int xsddefault_initialize_status_data( String config_file)
    {
        int result;
        
        /* grab configuration data */
        result = xsddefault_grab_config_info(config_file);
        if(result== common_h.ERROR)
            return  common_h.ERROR;
        
        /* delete the old status log (it might not exist) */
        new File ( xsddefault_status_log ).delete();
        
        return common_h.OK;
    }
    
    
    /* cleanup status data before terminating */
    public static int xsddefault_cleanup_status_data( String config_file, int delete_status_data)
    {
        
        // TODO - Rob 15/01/07 - This is really not what we want to do...
    	// This will cause us to delete the main configuration file.
    	
    	/* delete the status log */
        if(delete_status_data== common_h.TRUE)
        {
            //if (! new File(config_file).delete())
        	  if(! new File(xsddefault_status_log).delete())
                return common_h.ERROR;
        }
        
        return common_h.OK;
    }
    
    
    /******************************************************************/
    /****************** STATUS DATA OUTPUT FUNCTIONS ******************/
    /******************************************************************/
    
    /* write all status data to file */
    public static int xsddefault_save_status_data( ){
        /* open a safe temp file for output */
        File xsddefault_aggregate_temp_file;
        try {
            xsddefault_aggregate_temp_file = File.createTempFile( "status", "file" );
        } catch ( IOException ioE ) {
            logger.fatal( "Error: Unable to create temp file for writing status data!" , ioE);
            return common_h.ERROR;
        }

        PrintWriter pw;
        try {
            pw = new PrintWriter ( new FileWriter ( xsddefault_aggregate_temp_file ) );
        } catch ( IOException ioE ) {
            logger.fatal( "Error: Unable to open temp file '"+xsddefault_aggregate_temp_file.toString() +"' for writing status data!" , ioE );
            return common_h.ERROR;
        }
        
        long current_time = utils.currentTimeInSeconds();
        
        /* write version info to status file */
        pw.println( "########################################");
        pw.println( "#          NAGIOS STATUS FILE");
        pw.println( "#");
        pw.println( "# THIS FILE IS AUTOMATICALLY GENERATED");
        pw.println( "# BY NAGIOS.  DO NOT MODIFY THIS FILE!");
        pw.println( "########################################");
        pw.println();
        
        /* write file info */
        pw.println( "info {");
        pw.println( "\tcreated=" + current_time );
        pw.println( "\tversion=" + common_h.PROGRAM_VERSION);
        pw.println( "\t}");
        pw.println();
        
        /* save program status data */
        pw.println( "program {");
        pw.println( "\tmodified_host_attributes=" + blue.modified_host_process_attributes);
        pw.println( "\tmodified_service_attributes=" + blue.modified_service_process_attributes);
        pw.println( "\tnagios_pid=" + blue.blue_pid);
        pw.println( "\tdaemon_mode=" + blue.daemon_mode);
        pw.println( "\tprogram_start=" + blue.program_start);
        pw.println( "\tlast_command_check=" + blue.last_command_check);
        pw.println( "\tlast_log_rotation=" + blue.last_log_rotation);
        pw.println( "\tenable_notifications=" + blue.enable_notifications);
        pw.println( "\tactive_service_checks_enabled=" + blue.execute_service_checks);
        pw.println( "\tpassive_service_checks_enabled=" + blue.accept_passive_service_checks);
        pw.println( "\tactive_host_checks_enabled=" + blue.execute_host_checks);
        pw.println( "\tpassive_host_checks_enabled=" + blue.accept_passive_host_checks);
        pw.println( "\tenable_event_handlers=" + blue.enable_event_handlers);
        pw.println( "\tobsess_over_services=" + blue.obsess_over_services);
        pw.println( "\tobsess_over_hosts=" + blue.obsess_over_hosts);
        pw.println( "\tcheck_service_freshness=" + blue.check_service_freshness);
        pw.println( "\tcheck_host_freshness=" + blue.check_host_freshness);
        pw.println( "\tenable_flap_detection=" + blue.enable_flap_detection);
        pw.println( "\tenable_failure_prediction=" + blue.enable_failure_prediction);
        pw.println( "\tprocess_performance_data=" + blue.process_performance_data);
        pw.println( "\tglobal_host_event_handler=" + ((blue.global_host_event_handler==null)?"":blue.global_host_event_handler));
        pw.println( "\tglobal_service_event_handler=" + ((blue.global_service_event_handler==null)?"":blue.global_service_event_handler));
        pw.println( "\t}");
        pw.println();

        
        /* save host status data */
        for ( objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list ) {
            
            pw.println( "host {");
            pw.println( "\thost_name=" + temp_host.name);
            pw.println( "\tmodified_attributes=" + temp_host.modified_attributes);
            pw.println( "\tcheck_command=" + ((temp_host.host_check_command==null)?"":temp_host.host_check_command));
            pw.println( "\tevent_handler=" + ((temp_host.event_handler==null)?"":temp_host.event_handler));
            pw.println( "\thas_been_checked=" + temp_host.has_been_checked);
            pw.println( "\tshould_be_scheduled=" + temp_host.should_be_scheduled);
            pw.println( "\tcheck_execution_time=" + temp_host.execution_time);
            pw.println( "\tcheck_latency=" + temp_host.latency);
            pw.println( "\tcheck_type=" + temp_host.check_type);
            pw.println( "\tcurrent_state=" + temp_host.current_state);
            pw.println( "\tlast_hard_state=" + temp_host.last_hard_state);
            pw.println( "\tplugin_output=" + ((temp_host.plugin_output==null)?"":temp_host.plugin_output));
            pw.println( "\tperformance_data=" + ((temp_host.perf_data==null)?"":temp_host.perf_data));
            pw.println( "\tlast_check=" + temp_host.last_check);
            pw.println( "\tnext_check=" + temp_host.next_check);
            pw.println( "\tcurrent_attempt=" + temp_host.current_attempt);
            pw.println( "\tmax_attempts=" + temp_host.max_attempts);
            pw.println( "\tstate_type=" + temp_host.state_type);
            pw.println( "\tlast_state_change=" + temp_host.last_state_change);
            pw.println( "\tlast_hard_state_change=" + temp_host.last_hard_state_change);
            pw.println( "\tlast_time_up=" + temp_host.last_time_up);
            pw.println( "\tlast_time_down=" + temp_host.last_time_down);
            pw.println( "\tlast_time_unreachable=" + temp_host.last_time_unreachable);
            pw.println( "\tlast_notification=" + temp_host.last_host_notification);
            pw.println( "\tnext_notification=" + temp_host.next_host_notification);
            pw.println( "\tno_more_notifications=" + temp_host.no_more_notifications);
            pw.println( "\tcurrent_notification_number=" + temp_host.current_notification_number);
            pw.println( "\tnotifications_enabled=" + temp_host.notifications_enabled);
            pw.println( "\tproblem_has_been_acknowledged=" + temp_host.problem_has_been_acknowledged);
            pw.println( "\tacknowledgement_type=" + temp_host.acknowledgement_type);
            pw.println( "\tactive_checks_enabled=" + temp_host.checks_enabled);
            pw.println( "\tpassive_checks_enabled=" + temp_host.accept_passive_host_checks);
            pw.println( "\tevent_handler_enabled=" + temp_host.event_handler_enabled);
            pw.println( "\tflap_detection_enabled=" + temp_host.flap_detection_enabled);
            pw.println( "\tfailure_prediction_enabled=" + temp_host.failure_prediction_enabled);
            pw.println( "\tprocess_performance_data=" + temp_host.process_performance_data);
            pw.println( "\tobsess_over_host=" + temp_host.obsess_over_host);
            pw.println( "\tlast_update=" + current_time);
            pw.println( "\tis_flapping=" + temp_host.is_flapping);
            pw.println( "\tpercent_state_change=" + temp_host.percent_state_change);
            pw.println( "\tscheduled_downtime_depth=" + temp_host.scheduled_downtime_depth);
            pw.print( "\tstate_history=");
            for(int x=0;x< objects_h.MAX_STATE_HISTORY_ENTRIES;x++)
                pw.print( ((x>0)?",":"") + temp_host.state_history[(x+temp_host.state_history_index) % objects_h.MAX_STATE_HISTORY_ENTRIES]);
            pw.println();
            pw.println( "\t}");
            pw.println();

        }
        
        /* save service status data */
        for ( objects_h.service temp_service : (ArrayList<objects_h.service>) objects.service_list ) {
            
            pw.println( "service {");
            pw.println( "\thost_name=" + temp_service.host_name);
            pw.println( "\tservice_description=" + temp_service.description);
            pw.println( "\tmodified_attributes=" + temp_service.modified_attributes);
            pw.println( "\tcheck_command=" + ((temp_service.service_check_command==null)?"":temp_service.service_check_command));
            pw.println( "\tevent_handler=" + ((temp_service.event_handler==null)?"":temp_service.event_handler));
            pw.println( "\thas_been_checked=" + temp_service.has_been_checked);
            pw.println( "\tshould_be_scheduled=" + temp_service.should_be_scheduled);
            pw.println( "\tcheck_execution_time=" + temp_service.execution_time);
            pw.println( "\tcheck_latency=" + temp_service.latency);
            pw.println( "\tcheck_type=" + temp_service.check_type);
            pw.println( "\tcurrent_state=" + temp_service.current_state);
            pw.println( "\tlast_hard_state=" + temp_service.last_hard_state);
            pw.println( "\tcurrent_attempt=" + temp_service.current_attempt);
            pw.println( "\tmax_attempts=" + temp_service.max_attempts);
            pw.println( "\tstate_type=" + temp_service.state_type);
            pw.println( "\tlast_state_change=" + temp_service.last_state_change);
            pw.println( "\tlast_hard_state_change=" + temp_service.last_hard_state_change);
            pw.println( "\tlast_time_ok=" + temp_service.last_time_ok);
            pw.println( "\tlast_time_warning=" + temp_service.last_time_warning);
            pw.println( "\tlast_time_unknown=" + temp_service.last_time_unknown);
            pw.println( "\tlast_time_critical=" + temp_service.last_time_critical);
            pw.println( "\tplugin_output=" + ((temp_service.plugin_output==null)?"":temp_service.plugin_output));
            pw.println( "\tperformance_data=" + ((temp_service.perf_data==null)?"":temp_service.perf_data));
            pw.println( "\tlast_check=" + temp_service.last_check);
            pw.println( "\tnext_check=" + temp_service.next_check);
            pw.println( "\tcurrent_notification_number=" + temp_service.current_notification_number);
            pw.println( "\tlast_notification=" + temp_service.last_notification);
            pw.println( "\tnext_notification=" + temp_service.next_notification);
            pw.println( "\tno_more_notifications=" + temp_service.no_more_notifications);
            pw.println( "\tnotifications_enabled=" + temp_service.notifications_enabled);
            pw.println( "\tactive_checks_enabled=" + temp_service.checks_enabled);
            pw.println( "\tpassive_checks_enabled=" + temp_service.accept_passive_service_checks);
            pw.println( "\tevent_handler_enabled=" + temp_service.event_handler_enabled);
            pw.println( "\tproblem_has_been_acknowledged=" + temp_service.problem_has_been_acknowledged);
            pw.println( "\tacknowledgement_type=" + temp_service.acknowledgement_type);
            pw.println( "\tflap_detection_enabled=" + temp_service.flap_detection_enabled);
            pw.println( "\tfailure_prediction_enabled=" + temp_service.failure_prediction_enabled);
            pw.println( "\tprocess_performance_data=" + temp_service.process_performance_data);
            pw.println( "\tobsess_over_service=" + temp_service.obsess_over_service);
            pw.println( "\tlast_update=" + current_time);
            pw.println( "\tis_flapping=" + temp_service.is_flapping);
            pw.println( "\tpercent_state_change= " + temp_service.percent_state_change);
            pw.println( "\tscheduled_downtime_depth=" + temp_service.scheduled_downtime_depth);
            pw.print("\tstate_history=");
            for(int x=0;x<objects_h.MAX_STATE_HISTORY_ENTRIES;x++)
                pw.print(((x>0)?",":"") + temp_service.state_history[(x+temp_service.state_history_index)%objects_h.MAX_STATE_HISTORY_ENTRIES]);
            pw.println();
            pw.println( "\t}");
            pw.println();

        }
        
        
//        /* reset file permissions */
//        fchmod(fd,S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH);
        
        /* close the temp file */
        pw.close();
        
        /* move the temp file to the status log (overwrite the old status log) */
        
        if( utils.file_functions.my_rename( xsddefault_aggregate_temp_file.toString(), xsddefault_status_log) != 0 ){
            logger.fatal( "Error: Unable to update status data file '"+xsddefault_status_log+"'!");
            return common_h.ERROR;
        }
        
        xsddefault_aggregate_temp_file.delete();
        
        return common_h.OK;
    }


/******************************************************************/
/****************** DEFAULT DATA INPUT FUNCTIONS ******************/
/******************************************************************/

/* read all program, host, and service status information */
public static int xsddefault_read_status_data( String config_file,int options){
	int data_type=xsddefault_h.XSDDEFAULT_NO_DATA;
	statusdata_h.hoststatus temp_hoststatus=null;
    statusdata_h.servicestatus temp_servicestatus=null;
	int result;

	/* grab configuration data */
	result=xsddefault_grab_config_info(config_file);
	if(result==common_h.ERROR)
		return common_h.ERROR;

	/* open the status file for reading */
    blue_h.mmapfile thefile = utils.file_functions.mmap_fopen(xsddefault_status_log);
	if( thefile == null )
		return common_h.ERROR;

	/* read all lines in the status file */
	while( true ){

		/* read the next line */
        String input = utils.file_functions.mmap_fgets( thefile );
		if( input == null )
			break;

		input = input.trim();

		/* skip blank lines and comments */
        if( input.length() == 0 || input.charAt(0) =='#')
            continue;

		else if( input.equals( "info {"))
			data_type= xsddefault_h.XSDDEFAULT_INFO_DATA;
		else if( input.equals( "program {"))
			data_type=xsddefault_h.XSDDEFAULT_PROGRAM_DATA;
		else if( input.equals( "host {")){
			data_type=xsddefault_h.XSDDEFAULT_HOST_DATA;
			temp_hoststatus = new statusdata_h.hoststatus();
		        }
		else if( input.equals( "service {")){
			data_type=xsddefault_h.XSDDEFAULT_SERVICE_DATA;
			temp_servicestatus= new statusdata_h.servicestatus();
		        }

		else if( input.equals( "}")){

			switch(data_type){

			case xsddefault_h.XSDDEFAULT_INFO_DATA:
				break;

			case xsddefault_h.XSDDEFAULT_PROGRAM_DATA:
				break;

			case xsddefault_h.XSDDEFAULT_HOST_DATA:
                statusdata.add_host_status(temp_hoststatus);
				temp_hoststatus=null;
				break;

			case xsddefault_h.XSDDEFAULT_SERVICE_DATA:
                statusdata.add_service_status(temp_servicestatus);
				temp_servicestatus=null;
				break;

			default:
				break;
			        }

			data_type=xsddefault_h.XSDDEFAULT_NO_DATA;
		        }

		else if(data_type!=xsddefault_h.XSDDEFAULT_NO_DATA){

            String[] split = input.split( "=", 2);
            if ( split.length != 2 )
                continue;
            
            String var = split[0];
            String val = split[1];

			switch(data_type){

			case xsddefault_h.XSDDEFAULT_INFO_DATA:
				break;

			case xsddefault_h.XSDDEFAULT_PROGRAM_DATA:
				/* NOTE: some vars are not read, as they are not used by the CGIs (modified attributes, event handler commands, etc.) */
				if(var.equals( "nagios_pid"))
					blue.blue_pid=atoi(val);
				else if(var.equals( "daemon_mode"))
                    blue.daemon_mode=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				else if(var.equals( "program_start"))
                    blue.program_start=strtoul(val,null,10);
				else if(var.equals( "last_command_check"))
                    blue.last_command_check=strtoul(val,null,10);
				else if(var.equals( "last_log_rotation"))
                    blue.last_log_rotation=strtoul(val,null,10);
				else if(var.equals( "enable_notifications"))
                    blue.enable_notifications=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				else if(var.equals( "active_service_checks_enabled"))
                    blue.execute_service_checks=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				else if(var.equals( "passive_service_checks_enabled"))
                    blue.accept_passive_service_checks=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				else if(var.equals( "active_host_checks_enabled"))
                    blue.execute_host_checks=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				else if(var.equals( "passive_host_checks_enabled"))
                    blue.accept_passive_host_checks=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				else if(var.equals( "enable_event_handlers"))
                    blue.enable_event_handlers=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				else if(var.equals( "obsess_over_services"))
                    blue.obsess_over_services=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				else if(var.equals( "obsess_over_hosts"))
                    blue.obsess_over_hosts=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				else if(var.equals( "check_service_freshness"))
                    blue.check_service_freshness=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				else if(var.equals( "check_host_freshness"))
                    blue.check_host_freshness=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				else if(var.equals( "enable_flap_detection"))
                    blue.enable_flap_detection=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				else if(var.equals( "enable_failure_prediction"))
                    blue.enable_failure_prediction=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				else if(var.equals( "process_performance_data"))
                    blue.process_performance_data=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				break;

			case xsddefault_h.XSDDEFAULT_HOST_DATA:
				/* NOTE: some vars are not read, as they are not used by the CGIs (modified attributes, event handler commands, etc.) */
				if(temp_hoststatus!=null){
					if(var.equals( "host_name"))
						temp_hoststatus.host_name=val;
					else if(var.equals( "has_been_checked"))
						temp_hoststatus.has_been_checked=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "should_be_scheduled"))
						temp_hoststatus.should_be_scheduled=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "check_execution_time"))
						temp_hoststatus.execution_time=strtod(val,null);
					else if(var.equals( "check_latency"))
						temp_hoststatus.latency=strtod(val,null);
					else if(var.equals( "check_type"))
						temp_hoststatus.check_type=atoi(val);
					else if(var.equals( "current_state"))
						temp_hoststatus.status=atoi(val);
					else if(var.equals( "last_hard_state"))
						temp_hoststatus.last_hard_state=atoi(val);
					else if(var.equals( "plugin_output"))
						temp_hoststatus.plugin_output=val;
					else if(var.equals( "performance_data"))
						temp_hoststatus.perf_data=val;
					else if(var.equals( "current_attempt"))
						temp_hoststatus.current_attempt=atoi(val);
					else if(var.equals( "max_attempts"))
						temp_hoststatus.max_attempts=atoi(val);
					else if(var.equals( "last_check"))
						temp_hoststatus.last_check=strtoul(val,null,10);
					else if(var.equals( "next_check"))
						temp_hoststatus.next_check=strtoul(val,null,10);
					else if(var.equals( "current_attempt"))
						temp_hoststatus.current_attempt=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "state_type"))
						temp_hoststatus.state_type=atoi(val);
					else if(var.equals( "last_state_change"))
						temp_hoststatus.last_state_change=strtoul(val,null,10);
					else if(var.equals( "last_hard_state_change"))
						temp_hoststatus.last_hard_state_change=strtoul(val,null,10);
					else if(var.equals( "last_time_up"))
						temp_hoststatus.last_time_up=strtoul(val,null,10);
					else if(var.equals( "last_time_down"))
						temp_hoststatus.last_time_down=strtoul(val,null,10);
					else if(var.equals( "last_time_unreachable"))
						temp_hoststatus.last_time_unreachable=strtoul(val,null,10);
					else if(var.equals( "last_notification"))
						temp_hoststatus.last_notification=strtoul(val,null,10);
					else if(var.equals( "next_notification"))
						temp_hoststatus.next_notification=strtoul(val,null,10);
					else if(var.equals( "no_more_notifications"))
						temp_hoststatus.no_more_notifications=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "current_notification_number"))
						temp_hoststatus.current_notification_number=atoi(val);
					else if(var.equals( "notifications_enabled"))
						temp_hoststatus.notifications_enabled=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "problem_has_been_acknowledged"))
						temp_hoststatus.problem_has_been_acknowledged=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "acknowledgement_type"))
						temp_hoststatus.acknowledgement_type=atoi(val);
					else if(var.equals( "active_checks_enabled"))
						temp_hoststatus.checks_enabled=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "passive_checks_enabled"))
						temp_hoststatus.accept_passive_host_checks=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "event_handler_enabled"))
						temp_hoststatus.event_handler_enabled=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "flap_detection_enabled"))
						temp_hoststatus.flap_detection_enabled=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "failure_prediction_enabled"))
						temp_hoststatus.failure_prediction_enabled=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "process_performance_data"))
						temp_hoststatus.process_performance_data=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "obsess_over_host"))
						temp_hoststatus.obsess_over_host=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "last_update"))
						temp_hoststatus.last_update=strtoul(val,null,10);
					else if(var.equals( "is_flapping"))
						temp_hoststatus.is_flapping=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "percent_state_change"))
						temp_hoststatus.percent_state_change=strtod(val,null);
					else if(var.equals( "scheduled_downtime_depth"))
						temp_hoststatus.scheduled_downtime_depth=atoi(val);
					/*
					else if(!strcmp(var,"state_history")){
						temp_ptr=val;
						for(x=0;x<MAX_STATE_HISTORY_ENTRIES;x++)
							temp_hoststatus->state_history[x]=atoi(my_strsep(&temp_ptr,","));
						temp_hoststatus->state_history_index=0;
					        }
					*/
				        }
				break;

			case xsddefault_h.XSDDEFAULT_SERVICE_DATA:
				/* NOTE: some vars are not read, as they are not used by the CGIs (modified attributes, event handler commands, etc.) */
				if(temp_servicestatus!=null){
					if(var.equals( "host_name"))
						temp_servicestatus.host_name=val;
					else if(var.equals( "service_description"))
						temp_servicestatus.description=val;
					else if(var.equals( "has_been_checked"))
						temp_servicestatus.has_been_checked=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "should_be_scheduled"))
						temp_servicestatus.should_be_scheduled=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "check_execution_time"))
						temp_servicestatus.execution_time=strtod(val,null);
					else if(var.equals( "check_latency"))
						temp_servicestatus.latency=strtod(val,null);
					else if(var.equals( "check_type"))
						temp_servicestatus.check_type=atoi(val);
					else if(var.equals( "current_state"))
						temp_servicestatus.status=atoi(val);
					else if(var.equals( "last_hard_state"))
						temp_servicestatus.last_hard_state=atoi(val);
					else if(var.equals( "current_attempt"))
						temp_servicestatus.current_attempt=atoi(val);
					else if(var.equals( "max_attempts"))
						temp_servicestatus.max_attempts=atoi(val);
					else if(var.equals( "state_type"))
						temp_servicestatus.state_type=atoi(val);
					else if(var.equals( "last_state_change"))
						temp_servicestatus.last_state_change=strtoul(val,null,10);
					else if(var.equals( "last_hard_state_change"))
						temp_servicestatus.last_hard_state_change=strtoul(val,null,10);
					else if(var.equals( "last_time_ok"))
						temp_servicestatus.last_time_ok=strtoul(val,null,10);
					else if(var.equals( "last_time_warning"))
						temp_servicestatus.last_time_warning=strtoul(val,null,10);
					else if(var.equals( "last_time_unknown"))
						temp_servicestatus.last_time_unknown=strtoul(val,null,10);
					else if(var.equals( "last_time_critical"))
						temp_servicestatus.last_time_critical=strtoul(val,null,10);
					else if(var.equals( "plugin_output"))
						temp_servicestatus.plugin_output=val;
					else if(var.equals( "performance_data"))
						temp_servicestatus.perf_data=val;
					else if(var.equals( "last_check"))
						temp_servicestatus.last_check=strtoul(val,null,10);
					else if(var.equals( "next_check"))
						temp_servicestatus.next_check=strtoul(val,null,10);
					else if(var.equals( "current_notification_number"))
						temp_servicestatus.current_notification_number=atoi(val);
					else if(var.equals( "last_notification"))
						temp_servicestatus.last_notification=strtoul(val,null,10);
					else if(var.equals( "next_notification"))
						temp_servicestatus.next_notification=strtoul(val,null,10);
					else if(var.equals( "no_more_notifications"))
						temp_servicestatus.no_more_notifications=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "notifications_enabled"))
						temp_servicestatus.notifications_enabled=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "active_checks_enabled"))
						temp_servicestatus.checks_enabled=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "passive_checks_enabled"))
						temp_servicestatus.accept_passive_service_checks=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "event_handler_enabled"))
						temp_servicestatus.event_handler_enabled=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "problem_has_been_acknowledged"))
						temp_servicestatus.problem_has_been_acknowledged=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "acknowledgement_type"))
						temp_servicestatus.acknowledgement_type=atoi(val);
					else if(var.equals( "flap_detection_enabled"))
						temp_servicestatus.flap_detection_enabled=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "failure_prediction_enabled"))
						temp_servicestatus.failure_prediction_enabled=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "process_performance_data"))
						temp_servicestatus.process_performance_data=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "obsess_over_service"))
						temp_servicestatus.obsess_over_service=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "last_update"))
						temp_servicestatus.last_update=strtoul(val,null,10);
					else if(var.equals( "is_flapping"))
						temp_servicestatus.is_flapping=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
					else if(var.equals( "percent_state_change"))
						temp_servicestatus.percent_state_change=strtod(val,null);
					else if(var.equals( "scheduled_downtime_depth"))
						temp_servicestatus.scheduled_downtime_depth=atoi(val);
					/*
					else if(!strcmp(var,"state_history")){
						temp_ptr=val;
						for(x=0;x<MAX_STATE_HISTORY_ENTRIES;x++)
							temp_servicestatus->state_history[x]=atoi(my_strsep(&temp_ptr,","));
						temp_servicestatus->state_history_index=0;
					        }
					*/
				        }
				break;

			default:
				break;
			        }

		        }
	        }

	/* free memory and close the file */
	utils.file_functions.mmap_fclose(thefile);

	return common_h.OK;
        }

    private static int atoi(String value) {
        try {
            return Integer.parseInt(value);
        } catch ( NumberFormatException nfE ) {
           logger.error("warning: " + nfE.getMessage(), nfE);
            return 0;
        }
    }

    private static double strtod(String value, Object ignore ) {
        try {
            return Double.parseDouble(value);
        } catch ( NumberFormatException nfE ) {
           logger.error("warning: " + nfE.getMessage(), nfE);
            return 0.0;
        }
    }
    
    private static long strtoul(String value, Object ignore, int base ) {
        try {
            return Long.parseLong(value);
        } catch ( NumberFormatException nfE ) {
           logger.error("warning: " + nfE.getMessage(), nfE);
            return 0L;
        }
    }

}