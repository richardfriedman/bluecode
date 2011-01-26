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

import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.common.objects;
import org.blue.star.include.blue_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;

/*
#include "../include/config.h"
#include "../include/common.h"
#include "../include/statusdata.h"
#include "../include/nagios.h"
#include "../include/broker.h"
*/

public class logging { 
    
    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.base.logging");
    public static String cn = "org.blue.base.logging"; 

    
/******************************************************************/
/************************ LOGGING FUNCTIONS ***********************/
/******************************************************************/

///* write something to the log file, syslog, and possibly the console */
//public static int write_to_logs_and_console( String buffer, long data_type, int display){
//	int len;
//	int x;
//
//	logger.entering( cn, "write_to_logs_and_console");
//
//	/* write messages to the logs */
//	write_to_all_logs(buffer,data_type);
//
//	/* write message to the console */
//	if(display==common_h.TRUE)
//		write_to_console(buffer);
//
//	logger.exiting( cn, "write_to_logs_and_console");
//	return common_h.OK;
//        }
//
//    /* write something to the console */
//    public static int write_to_console(String buffer){
//        logger.entering( cn, "write_to_console");
//        
//        /* should we print to the console? */
//        if(nagios.daemon_mode==common_h.FALSE)
//            System.out.println( buffer );
//        
//        logger.exiting( cn, "write_to_console");
//        return common_h.OK;
//    }

//    /* write something to the log file and syslog facility */
//    public static int write_to_all_logs(String buffer, long data_type){
//        logger.entering( cn, "write_to_all_logs");
//        
//        /* write to syslog */
//        // TODO check to see if there is ajava package to write to syslog (maybe jtux)
////        write_to_syslog( buffer, data_type);
//        
//        /* write to main log */
//        write_to_log( buffer, data_type, 0);
//        
//        logger.exiting( cn, "write_to_all_logs");
//        return common_h.OK;
//    }
//
//    /* write something to the log file and syslog facility */
//    public static int write_to_all_logs_with_timestamp(char *buffer, unsigned long data_type, time_t *timestamp){
//        logger.entering( cn, "write_to_all_logs_with_timestamp");
//        
//        /* write to syslog */
//        write_to_syslog(buffer,data_type);
//        
//        /* write to main log */
//        write_to_log(buffer,data_type,timestamp);
//        
//        logger.exiting( cn, "write_to_all_logs_with_timestamp");
//        return common_h.OK;
//    }
//
//
///* write something to the nagios log file */
//public static int write_to_log(String buffer, long data_type, long timestamp){
//	
//	logger.entering( cn, "write_to_log");
//
//	/* write the buffer to the log file */
//	// TODO Set log4j settings to this format.
////	fprintf(fp,"[%lu] %s\n",log_time,buffer);
//	if ( timestamp == 0 ) {
//	    logger.info( buffer );
//	} else {
//	    LogRecord record = new LogRecord( Level.INFO, buffer );
//	    record.setMillis( timestamp );
//	    logger.log( record );
//	}
//	
////#ifdef USE_EVENT_BROKER
////	/* send data to the event broker */
////	broker_log_data(NEBTYPE_LOG_DATA,NEBFLAG_NONE,NEBATTR_NONE,buffer,data_type,NULL);
////#endif
//
//	logger.exiting( cn, "write_to_log");
//	return common_h.OK;
//	}
//
///* write something to the syslog facility */
//int write_to_syslog(char *buffer, unsigned long data_type){
//
//#ifdef DEBUG0
//	printf("write_to_syslog() start\n");
//#endif
//
//	/* don't log anything if we're not actually running... */
//	if(verify_config==TRUE || test_scheduling==TRUE)
//		return OK;
//
//	/* bail out if we shouldn't write to syslog */
//	if(use_syslog==FALSE)
//		return OK;
//
//	/* make sure we should log this type of entry */
//	if(!(data_type & syslog_options))
//		return OK;
//
//	/* write the buffer to the syslog facility */
//	syslog(LOG_USER|LOG_INFO,"%s",buffer);
//
//#ifdef DEBUG0
//	printf("write_to_syslog() end\n");
//#endif
//
//	return OK;
//	}
//
//
/* write a service problem/recovery to the nagios log file */
public static void log_service_event(objects_h.service svc)
{
	String temp_buffer;
	long log_options;
	objects_h.host temp_host;

    logger.trace( "entering " + cn + ".log_service_event");

	/* don't log soft errors if the user doesn't want to */
	if(svc.state_type==common_h.SOFT_STATE && (blue.log_service_retries==0) )
		return ;

	/* get the log options */
	if(svc.current_state==blue_h.STATE_UNKNOWN)
		log_options=blue_h.NSLOG_SERVICE_UNKNOWN;
	else if(svc.current_state==blue_h.STATE_WARNING)
		log_options=blue_h.NSLOG_SERVICE_WARNING;
	else if(svc.current_state==blue_h.STATE_CRITICAL)
		log_options=blue_h.NSLOG_SERVICE_CRITICAL;
	else
		log_options=blue_h.NSLOG_SERVICE_OK;

	/* find the associated host */
	temp_host= objects.find_host(svc.host_name);

	/* grab service macros */
	utils.clear_volatile_macros();
	utils.grab_host_macros(temp_host);
	utils.grab_service_macros(svc);
	utils.grab_summary_macros(null);

	temp_buffer = "SERVICE ALERT: "+svc.host_name+";"+svc.description+";"+blue.macro_x[blue_h.MACRO_SERVICESTATE]+";"+blue.macro_x[blue_h.MACRO_SERVICESTATETYPE]+";"+blue.macro_x[blue_h.MACRO_SERVICEATTEMPT]+";"+svc.plugin_output;
	logger.info( temp_buffer );
	logger.trace( "exiting " + cn + ".log_service_event");

	}

/* write a host problem/recovery to the log file */
public static void log_host_event(objects_h.host hst){
	String temp_buffer;
	long log_options=0L;

    logger.trace( "entering " + cn + ".log_host_event");

	/* grab the host macros */
	utils.clear_volatile_macros();
	utils.grab_host_macros(hst);
	utils.grab_summary_macros(null);

	/* get the log options */
	if(hst.current_state==blue_h.HOST_DOWN)
		log_options=blue_h.NSLOG_HOST_DOWN;
	else if(hst.current_state==blue_h.HOST_UNREACHABLE)
		log_options=blue_h.NSLOG_HOST_UNREACHABLE;
	else
		log_options=blue_h.NSLOG_HOST_UP;


	temp_buffer = "HOST ALERT: "+hst.name+";"+blue.macro_x[blue_h.MACRO_HOSTSTATE]+";"+blue.macro_x[blue_h.MACRO_HOSTSTATETYPE]+";"+blue.macro_x[blue_h.MACRO_HOSTATTEMPT]+";"+hst.plugin_output ;
	logger.warn( temp_buffer );
    
    logger.trace( "exiting " + cn + ".log_host_event");
}

/* logs host states */
public static int log_host_states(int type){
//	char temp_buffer[MAX_INPUT_BUFFER];
//	host *temp_host;

	/* bail if we shouldn't be logging initial states */
	if(type==blue_h.INITIAL_STATES && blue.log_initial_states==common_h.FALSE)
		return common_h.OK;

	/* grab summary macros */
	utils.grab_summary_macros(null);

	for ( objects_h.host  temp_host : (ArrayList<objects_h.host>)  objects.host_list ) {

		/* grab the host macros */
		utils.clear_volatile_macros();
		utils.grab_host_macros(temp_host);

		String temp_buffer = temp_host.name + " HOST STATE: "+blue.macro_x[blue_h.MACRO_HOSTSTATE]+";"+((type==blue_h.INITIAL_STATES)?"INITIAL":"CURRENT")+";"+blue.macro_x[blue_h.MACRO_HOSTSTATETYPE]+";"+blue.macro_x[blue_h.MACRO_HOSTATTEMPT]+";"+temp_host.plugin_output ;
        logger.info(temp_buffer);
	        }

	return common_h.OK;
        }


/* logs service states */
public static int log_service_states(int type)
{

	/* bail if we shouldn't be logging initial states */
	if(type==blue_h.INITIAL_STATES && blue.log_initial_states==common_h.FALSE)
		return common_h.OK;

	/* grab summary macros */
	utils.grab_summary_macros(null);

	for ( objects_h.service temp_service : (ArrayList<objects_h.service>)  objects.service_list ) {

		/* find the associated host */
		objects_h.host temp_host= objects.find_host(temp_service.host_name);

		/* grab service macros */
		utils.clear_volatile_macros();
		utils.grab_host_macros(temp_host);
		utils.grab_service_macros(temp_service);

		String temp_buffer = ((type==blue_h.INITIAL_STATES)?"INITIAL":"CURRENT") + " SERVICE STATE: "+temp_service.host_name+";"+temp_service.description+";"+blue.macro_x[blue_h.MACRO_SERVICESTATE]+";"+blue.macro_x[blue_h.MACRO_SERVICESTATETYPE]+";"+blue.macro_x[blue_h.MACRO_SERVICEATTEMPT]+";" + temp_service.plugin_output;
		logger.info( temp_buffer );
	        }

	return common_h.OK;
        }


/* rotates the main log file */
public static int rotate_log_file(long rotation_time)
{
    
//	char temp_buffer[MAX_INPUT_BUFFER];
//	char method_string[16];
//	char log_archive[MAX_FILENAME_LENGTH];
//	struct tm *t;
//	int rename_result;
//
//#ifdef DEBUG0
//	printf("rotate_log_file() start\n");
//#endif
//
//	if(log_rotation_method==LOG_ROTATION_NONE){
//
//#ifdef DEBUG1
//		printf("\tWe're not supposed to be doing log rotations!\n");
//#endif
//		return OK;
//	        }
//	else if(log_rotation_method==LOG_ROTATION_HOURLY)
//		strcpy(method_string,"HOURLY");
//	else if(log_rotation_method==LOG_ROTATION_DAILY)
//		strcpy(method_string,"DAILY");
//	else if(log_rotation_method==LOG_ROTATION_WEEKLY)
//		strcpy(method_string,"WEEKLY");
//	else if(log_rotation_method==LOG_ROTATION_MONTHLY)
//		strcpy(method_string,"MONTHLY");
//	else
//		return ERROR;
//
//	/* update the last log rotation time and status log */
//	last_log_rotation=time(NULL);
//	update_program_status(FALSE);
//
//	t=localtime(&rotation_time);
//
//	/* get the archived filename to use */
//	snprintf(log_archive,sizeof(log_archive),"%s%snagios-%02d-%02d-%d-%02d.log",log_archive_path,(log_archive_path[strlen(log_archive_path)-1]=='/')?"":"/",t.tm_mon+1,t.tm_mday,t.tm_year+1900,t.tm_hour);
//	log_archive[sizeof(log_archive)-1]='\x0';
//
//	/* rotate the log file */
//	rename_result=my_rename(log_file,log_archive);
//
//	if(rename_result){
//
//#ifdef DEBUG1
//		printf("\tError: Could not rotate main log file to '%s'\n",log_archive);
//#endif
//
		return common_h.ERROR;
	        }
//
//#ifdef USE_EVENT_BROKER
//	/* send data to the event broker */
//	broker_log_data(NEBTYPE_LOG_ROTATION,NEBFLAG_NONE,NEBATTR_NONE,log_archive,log_rotation_method,NULL);
//#endif
//
//	/* record the log rotation after it has been done... */
//	snprintf(temp_buffer,sizeof(temp_buffer),"LOG ROTATION: %s\n",method_string);
//	temp_buffer[sizeof(temp_buffer)-1]='\x0';
//	write_to_all_logs_with_timestamp(temp_buffer,NSLOG_PROCESS_INFO,&rotation_time);
//
//	/* record log file version format */
//	write_log_file_info(&rotation_time);
//
//	/* log current host and service state */
//	log_host_states(CURRENT_STATES,&rotation_time);
//	log_service_states(CURRENT_STATES,&rotation_time);
//
//#ifdef DEBUG3
//	printf("\tRotated main log file to '%s'\n",log_archive);
//#endif
//
//#ifdef DEBUG0
//	printf("rotate_log_file() end\n");
//#endif
//
//	return OK;
//        }
//
//
///* record log file version/info */
//int write_log_file_info(time_t *timestamp){
//	char temp_buffer[MAX_INPUT_BUFFER];
//
//#ifdef DEBUG0
//	printf("write_log_file_info() start\n");
//#endif
//
//	/* write log version */
//	snprintf(temp_buffer,sizeof(temp_buffer),"LOG VERSION: %s\n",LOG_VERSION_2);
//	temp_buffer[sizeof(temp_buffer)-1]='\x0';
//	write_to_all_logs_with_timestamp(temp_buffer,NSLOG_PROCESS_INFO,timestamp);
//
//#ifdef DEBUG0
//	printf("write_log_file_info() end\n");
//#endif
//
//	return OK;
//        }
}