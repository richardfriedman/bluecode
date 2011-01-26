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

/* This class performs many of the functions associated with obsessive compulsive checking,
 * running of global service event handlers/service event handlers and global host event handlers,
 * host event handlers.
 * 
 * It operates in roughly the same way as you would when submitting a normal command. Checks to see if we
 * are obsessing over hosts, if yes, retrieve the command we use to obsess and run it, store results etc. This
 * process is repeated for all the above mentioned scenarios.
 */

package org.blue.star.base;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.common.downtime;
import org.blue.star.common.objects;
import org.blue.star.include.blue_h;
import org.blue.star.include.broker_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;


public class sehandlers
{ 

    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.base.sehandlers");
    private static String cn = "org.blue.base.sehandlers";

/******************************************************************/
/************* OBSESSIVE COMPULSIVE HANDLER FUNCTIONS *************/
/******************************************************************/



/*
 * This method handles service checking in an obsessive compulsive manner. The 
 * obsessive compulsive manner of a service means that as soon as a result is returned from
 *  a service check, this command will be run. 
 *  
 *  @param = service object. This is passed to check if we are obsessing over this particular service.
 */

    
public static int obsessive_compulsive_service_check_processor(objects_h.service svc)
{
	
	String raw_command_line;
	String processed_command_line;
	objects_h.host temp_host;
	
	int macro_options=blue_h.STRIP_ILLEGAL_MACRO_CHARS|blue_h.ESCAPE_MACRO_CHARS;

	logger.trace( "entering " + cn + ".obsessive_compulsive_service_check_processor" );

	/* Check to see if we are obsessing at all? */
	if(blue.obsess_over_services==common_h.FALSE)
		return common_h.OK;
	
	/* If we are obsessing, are we obsessing over this service? */
	if(svc.obsess_over_service==common_h.FALSE)
		return common_h.OK;

	/* if there is no valid command, exit */
	if(blue.ocsp_command==null)
		return common_h.ERROR;

	/* find the associated host */
	temp_host= objects.find_host(svc.host_name);

	/* update service macros */
	utils.clear_volatile_macros();
	
	/* Get all macros associated with this host i.e make $HOSTNAME = host.hostname */
	utils.grab_host_macros(temp_host);
	
	/* Get all macros associated with this service i.e. Make $SERVICEDESCRIPTION = service.servicedescrition */
	utils.grab_service_macros(svc);
	
	utils.grab_summary_macros(null);

	/* get the raw command line */
    raw_command_line = utils.get_raw_command_line(blue.ocsp_command,macro_options);
    raw_command_line = utils.strip(raw_command_line);

	logger.debug("\tRaw obsessive compulsive service processor command line: " + raw_command_line);

	/* Turns any macros in the raw command line into their real values */
    processed_command_line = utils.process_macros(raw_command_line,macro_options);

	logger.debug("\tProcessed obsessive compulsive service processor command line: "  + processed_command_line);

	/* run the command */
    utils.system_result result = utils.my_system(processed_command_line,blue.ocsp_timeout);
    
    /* check to see if the command timed out */
    if( result.early_timeout==true)
		logger.warn( "Warning: OCSP command '"+processed_command_line+"' for service '"+svc.description+"' on host '"+svc.host_name+"' timed out after "+blue.ocsp_timeout+" seconds");
	
	logger.trace( "exiting " + cn + ".obsessive_compulsive_service_check_processor" );

	return common_h.OK;
}



/*
 * This method is similar to that of obsessive service checking. After each host check is 
 * performed, if we are obsessing then this command is also run.
 * 
 * @param = Host object, used to check if we are obsessing over a particular host
 */

public static int obsessive_compulsive_host_check_processor(objects_h.host hst)
{
	String raw_command_line;
	String processed_command_line;
	int macro_options=blue_h.STRIP_ILLEGAL_MACRO_CHARS|blue_h.ESCAPE_MACRO_CHARS;

	logger.trace( "entering " + cn + ".obsessive_compulsive_host_check_processor" );

	/* bail out if we shouldn't be obsessing */
	if(blue.obsess_over_hosts==common_h.FALSE)
		return common_h.OK;
	
	if(hst.obsess_over_host==common_h.FALSE)
		return common_h.OK;

	/* if there is no valid command, exit */
	if(blue.ochp_command==null)
		return common_h.ERROR;

	/* update macros */
	utils.clear_volatile_macros();
	utils.grab_host_macros(hst);
	utils.grab_summary_macros(null);

	/* get the raw command line */
    raw_command_line = utils.get_raw_command_line(blue.ochp_command,macro_options);
    raw_command_line = utils.strip(raw_command_line);

	logger.debug("\tRaw obsessive compulsive host processor command line: " + raw_command_line);

	/* process any macros in the raw command line */
    processed_command_line = utils.process_macros(raw_command_line,macro_options);

	logger.debug("\tProcessed obsessive compulsive host processor command line: " + processed_command_line);

	/* run the command */
    utils.system_result result = utils.my_system(processed_command_line,blue.ochp_timeout);

	/* check to see if the command timed out */
    if( result.early_timeout==true)
		logger.warn( "Warning: OCHP command '"+processed_command_line+"' for host '"+hst.name+"' timed out after "+blue.ochp_timeout+" seconds");

    logger.trace( "exiting " + cn + ".obsessive_compulsive_host_check_processor" );

	return common_h.OK;
}

/******************************************************************/
/**************** SERVICE EVENT HANDLER FUNCTIONS *****************/
/******************************************************************/


/* handles changes in the state of a service */
public static int handle_service_event(objects_h.service svc)
{
	objects_h.host temp_host;

	logger.trace( "entering " + cn + ".handle_service_event" );


	/* send event data to broker */
	broker.broker_statechange_data(broker_h.NEBTYPE_STATECHANGE_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.SERVICE_STATECHANGE,(Object)svc,svc.current_state,svc.state_type,svc.current_attempt,svc.max_attempts,null);
    
	/* bail out if we shouldn't be running event handlers */
	if(blue.enable_event_handlers==common_h.FALSE)
		return common_h.OK;
	if(svc.event_handler_enabled==common_h.FALSE)
		return common_h.OK;

	/* find the host */
	temp_host= objects.find_host(svc.host_name);

	/* update service macros */
	utils.clear_volatile_macros();
    utils.grab_host_macros(temp_host);
    utils.grab_service_macros(svc);
    utils.grab_summary_macros(null);

	/* run the global service event handler */
	run_global_service_event_handler(svc);

	/* run the event handler command if there is one */
	if(svc.event_handler!=null)
		run_service_event_handler(svc);

	/* check for external commands - the event handler may have given us some directives... */
	commands.check_for_external_commands();

	logger.trace( "exiting " + cn + ".handle_service_event" );

	return common_h.OK;
 }



/* runs the global service event handler */
public static int run_global_service_event_handler(objects_h.service svc)
{
	String raw_command_line;
	String processed_command_line;
	int macro_options= blue_h.STRIP_ILLEGAL_MACRO_CHARS| blue_h.ESCAPE_MACRO_CHARS;

	logger.trace( "entering " + cn + ".run_global_service_event_handler" );

	/* bail out if we shouldn't be running event handlers */
	if(blue.enable_event_handlers==common_h.FALSE)
		return common_h.OK;

	/* a global service event handler command has not been defined */
	if(blue.global_service_event_handler==null)
		return common_h.ERROR;

	/* get start time */
	blue_h.timeval start_time = new blue_h.timeval();
	
	/* send event data to broker */
    blue_h.timeval end_time = new blue_h.timeval(0,0);
	broker.broker_event_handler(broker_h.NEBTYPE_EVENTHANDLER_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.GLOBAL_SERVICE_EVENTHANDLER,(Object)svc,svc.current_state,svc.state_type,start_time,end_time,0,0,common_h.FALSE,0,blue.global_service_event_handler,null,null,null);

	/* get the raw command line */
    raw_command_line = utils.get_raw_command_line(blue.global_service_event_handler,macro_options);
    raw_command_line = utils.strip(raw_command_line);

	logger.debug("\tRaw global service event handler command line: " + raw_command_line);

	/* process any macros in the raw command line */
    processed_command_line = utils.process_macros(raw_command_line,macro_options);

	logger.debug("\tProcessed global service event handler command line: " + processed_command_line);

	if(blue.log_event_handlers==common_h.TRUE)
	{
		String temp_buffer = "GLOBAL SERVICE EVENT HANDLER: "+svc.host_name+";"+svc.description+";"+blue.macro_x[blue_h.MACRO_SERVICESTATE]+";"+blue.macro_x[blue_h.MACRO_SERVICESTATETYPE]+";"+blue.macro_x[blue_h.MACRO_SERVICEATTEMPT]+";"+blue.global_service_event_handler;
		logger.info( temp_buffer );
    }

	/* run the command */
    utils.system_result result = utils.my_system(processed_command_line,blue.event_handler_timeout);

	/* check to see if the event handler timed out */
    if( result.early_timeout==true)
		logger.warn( "Warning: Global service event handler command '"+processed_command_line+"' timed out after "+blue.event_handler_timeout+" seconds");
		
    /* get end time */
    end_time = new blue_h.timeval();
    
    /* send event data to broker */
    broker.broker_event_handler(broker_h.NEBTYPE_EVENTHANDLER_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.GLOBAL_SERVICE_EVENTHANDLER,(Object)svc,svc.current_state,svc.state_type,start_time,end_time,result.exec_time,blue.event_handler_timeout,result.early_timeout?common_h.TRUE:common_h.FALSE,result.result,blue.global_service_event_handler,processed_command_line,result.output,null);

    logger.trace( "exiting " + cn + ".run_global_service_event_handler" );

	return common_h.OK;
}



/* runs a service event handler command */
public static int run_service_event_handler(objects_h.service svc)
{
	String raw_command_line;
	String processed_command_line;
//	int result;
	int macro_options=blue_h.STRIP_ILLEGAL_MACRO_CHARS| blue_h.ESCAPE_MACRO_CHARS;

	logger.trace( "entering " + cn + ".run_service_event_handler" );

	/* bail if there's no command */
	if(svc.event_handler==null)
		return common_h.ERROR;


	/* get start time */
	blue_h.timeval start_time = new blue_h.timeval();
	
	/* send event data to broker */
    blue_h.timeval end_time = new blue_h.timeval(0,0);
	broker.broker_event_handler(broker_h.NEBTYPE_EVENTHANDLER_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.SERVICE_EVENTHANDLER,(Object)svc,svc.current_state,svc.state_type,start_time,end_time,0,blue.event_handler_timeout,common_h.FALSE,0,svc.event_handler,null,null,null);

	/* get the raw command line */
    raw_command_line = utils.get_raw_command_line(svc.event_handler,macro_options);
    raw_command_line = utils.strip(raw_command_line);

	logger.debug("\tRaw service event handler command line: " + raw_command_line);

	/* process any macros in the raw command line */
    processed_command_line= utils.process_macros(raw_command_line,macro_options);

	logger.debug("\tProcessed service event handler command line: " + processed_command_line);

	if(blue.log_event_handlers==common_h.TRUE){
		String temp_buffer = "SERVICE EVENT HANDLER: "+svc.host_name+";"+svc.description+";"+blue.macro_x[blue_h.MACRO_SERVICESTATE]+";"+blue.macro_x[blue_h.MACRO_SERVICESTATETYPE]+";"+blue.macro_x[blue_h.MACRO_SERVICEATTEMPT]+";"+svc.event_handler ;
		logger.info( temp_buffer );
	        }

	/* run the command */
    utils.system_result result = utils.my_system(processed_command_line,blue.event_handler_timeout);

	/* check to see if the event handler timed out */
    if( result.early_timeout==true)
		logger.warn( "Warning: Service event handler command '"+processed_command_line+"' timed out after "+blue.event_handler_timeout+" seconds");
		
    
    /* get end time */
    end_time = new blue_h.timeval();
    
    /* send event data to broker */
    broker.broker_event_handler(broker_h.NEBTYPE_EVENTHANDLER_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.SERVICE_EVENTHANDLER,(Object)svc,svc.current_state,svc.state_type,start_time,end_time,result.exec_time,blue.event_handler_timeout,result.early_timeout?common_h.TRUE:common_h.FALSE,result.result,svc.event_handler,processed_command_line,result.output,null);

    logger.trace( "exiting " + cn + ".run_service_event_handler" );

	return common_h.OK;
 }




/******************************************************************/
/****************** HOST EVENT HANDLER FUNCTIONS ******************/
/******************************************************************/


/* handles a change in the status of a host */
public static int handle_host_event(objects_h.host hst){

   logger.trace( "entering " + cn + ".handle_host_event" );
   
   /* send event data to broker */
   broker.broker_statechange_data(broker_h.NEBTYPE_STATECHANGE_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.HOST_STATECHANGE,(Object)hst,hst.current_state,hst.state_type,hst.current_attempt,hst.max_attempts,null);

	/* bail out if we shouldn't be running event handlers */
	if(blue.enable_event_handlers==common_h.FALSE)
		return common_h.OK;
	if(hst.event_handler_enabled==common_h.FALSE)
		return common_h.OK;

	/* update host macros */
	utils.clear_volatile_macros();
    utils.grab_host_macros(hst);
    utils.grab_summary_macros(null);

	/* run the global host event handler */
	run_global_host_event_handler(hst);

	/* run the event handler command if there is one */
	if(hst.event_handler!=null)
		run_host_event_handler(hst);

	/* check for external commands - the event handler may have given us some directives... */
    commands.check_for_external_commands();

logger.trace( "exiting " + cn + ".handle_host_event" );

	return common_h.OK;
        }


/* runs the global host event handler */
public static int run_global_host_event_handler(objects_h.host hst){
	String raw_command_line;
	String processed_command_line;
	int macro_options=blue_h.STRIP_ILLEGAL_MACRO_CHARS|blue_h.ESCAPE_MACRO_CHARS;

logger.trace( "entering " + cn + ".run_global_host_event_handler" );

	/* bail out if we shouldn't be running event handlers */
	if(blue.enable_event_handlers==common_h.FALSE)
		return common_h.OK;

	/* no global host event handler command is defined */
	if(blue.global_host_event_handler==null)
		return common_h.ERROR;

	/* get start time */
	blue_h.timeval start_time = new blue_h.timeval();
	
	/* send event data to broker */
    blue_h.timeval end_time = new blue_h.timeval(0,0);
    broker.broker_event_handler(broker_h.NEBTYPE_EVENTHANDLER_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.GLOBAL_HOST_EVENTHANDLER,(Object)hst,hst.current_state,hst.state_type,start_time,end_time,0,blue.event_handler_timeout,common_h.FALSE,0,blue.global_host_event_handler,null,null,null);

/* get the raw command line */
    raw_command_line = utils.get_raw_command_line( blue.global_host_event_handler,macro_options);
    raw_command_line = utils.strip(raw_command_line);

	logger.debug("\tRaw global host event handler command line: " + raw_command_line);

	/* process any macros in the raw command line */
    processed_command_line = utils.process_macros(raw_command_line,macro_options);

	logger.debug("\tProcessed global host event handler command line: " + processed_command_line);

	if(blue.log_event_handlers==common_h.TRUE){
		String temp_buffer = "GLOBAL HOST EVENT HANDLER: "+hst.name+";"+blue.macro_x[blue_h.MACRO_HOSTSTATE]+";"+blue.macro_x[blue_h.MACRO_HOSTSTATETYPE]+";"+blue.macro_x[blue_h.MACRO_HOSTATTEMPT]+";"+blue.global_host_event_handler ;
		
		logger.info( temp_buffer );
	        }

	/* run the command */
    utils.system_result result = utils.my_system(processed_command_line,blue.event_handler_timeout);

	/* check for a timeout in the execution of the event handler command */
    if( result.early_timeout==true)
		logger.warn( "Warning: Global host event handler command '"+processed_command_line+"' timed out after "+blue.event_handler_timeout+" seconds");

    /* get end time */
    end_time = new blue_h.timeval();
    
    /* send event data to broker */
    broker.broker_event_handler(broker_h.NEBTYPE_EVENTHANDLER_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.GLOBAL_HOST_EVENTHANDLER,(Object)hst,hst.current_state,hst.state_type,start_time,end_time,result.exec_time,blue.event_handler_timeout,result.early_timeout?common_h.TRUE:common_h.FALSE,result.result,blue.global_host_event_handler,processed_command_line,result.output,null);

logger.trace( "exiting " + cn + ".run_global_host_event_handler" );

	return common_h.OK;
        }


/* runs a host event handler command */
public static int run_host_event_handler(objects_h.host hst){
	String raw_command_line;
	String processed_command_line;
	int macro_options=blue_h.STRIP_ILLEGAL_MACRO_CHARS| blue_h.ESCAPE_MACRO_CHARS;

logger.trace( "entering " + cn + ".run_host_event_handler" );

	/* bail if there's no command */
	if(hst.event_handler==null)
		return common_h.ERROR;


	/* get start time */
	blue_h.timeval start_time = new blue_h.timeval();
	
	/* send event data to broker */
    blue_h.timeval end_time = new blue_h.timeval(0,0);
	broker.broker_event_handler(broker_h.NEBTYPE_EVENTHANDLER_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.HOST_EVENTHANDLER,(Object)hst,hst.current_state,hst.state_type,start_time,end_time,0,blue.event_handler_timeout,common_h.FALSE,0,hst.event_handler,null,null,null);

	/* get the raw command line */
    raw_command_line = utils.get_raw_command_line(hst.event_handler,macro_options);
    raw_command_line = utils.strip(raw_command_line);

	logger.debug("\tRaw host event handler command line: "+raw_command_line );

	/* process any macros in the raw command line */
    processed_command_line = utils.process_macros(raw_command_line,macro_options);

	logger.debug("\tProcessed host event handler command line: "+processed_command_line );

	if( blue.log_event_handlers==common_h.TRUE){
		String temp_buffer = "HOST EVENT HANDLER: "+hst.name+";"+blue.macro_x[blue_h.MACRO_HOSTSTATE]+";"+blue.macro_x[blue_h.MACRO_HOSTSTATETYPE]+";"+blue.macro_x[blue_h.MACRO_HOSTATTEMPT]+";"+hst.event_handler;
        logger.info( temp_buffer );
	        }

	/* run the command */
    utils.system_result result = utils.my_system(processed_command_line,blue.event_handler_timeout);

	/* check to see if the event handler timed out */
    if( result.early_timeout==true)
		logger.warn( "Warning: Host event handler command '"+processed_command_line+"' timed out after "+blue.event_handler_timeout+" seconds");

    /* get end time */
    end_time = new blue_h.timeval();
    
    /* send event data to broker */
    broker.broker_event_handler(broker_h.NEBTYPE_EVENTHANDLER_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.HOST_EVENTHANDLER,(Object)hst,hst.current_state,hst.state_type,start_time,end_time,result.exec_time,blue.event_handler_timeout,result.early_timeout?common_h.TRUE:common_h.FALSE,result.result,hst.event_handler,processed_command_line,result.output,null);

logger.trace( "exiting " + cn + ".run_host_event_handler" );

	return common_h.OK;
        }




/******************************************************************/
/****************** HOST STATE HANDLER FUNCTIONS ******************/
/******************************************************************/


/* top level host state handler - occurs after every host check (soft/hard and active/passive) */
public static int handle_host_state(objects_h.host hst){
	int state_change=common_h.FALSE;
	long current_time;

logger.trace( "entering " + cn + ".handle_host_state" );

	/* get current time */
	current_time = utils.currentTimeInSeconds();

	/* obsess over this host check */
	obsessive_compulsive_host_check_processor(hst);

	/* update performance data */
	perfdata.update_host_performance_data(hst);

	/* record latest time for current state */
	switch(hst.current_state){
	case blue_h.HOST_UP:
		hst.last_time_up=current_time;
		break;
	case blue_h.HOST_DOWN:
		hst.last_time_down=current_time;
		break;
	case blue_h.HOST_UNREACHABLE:
		hst.last_time_unreachable=current_time;
		break;
	default:
		break;
	        }

	/* has the host state changed? */
	if(hst.last_state!=hst.current_state || (hst.current_state==blue_h.HOST_UP && hst.state_type==common_h.SOFT_STATE))
		state_change=common_h.TRUE;

	/* if the host state has changed... */
	if(state_change==common_h.TRUE){

		/* update last state change times */
		hst.last_state_change=current_time;
		if(hst.state_type==common_h.HARD_STATE)
			hst.last_hard_state_change=current_time;

		/* reset the acknowledgement flag if necessary */
		if(hst.acknowledgement_type==common_h.ACKNOWLEDGEMENT_NORMAL){
			hst.problem_has_been_acknowledged=common_h.FALSE;
			hst.acknowledgement_type=common_h.ACKNOWLEDGEMENT_NONE;
		        }
		else if(hst.acknowledgement_type==common_h.ACKNOWLEDGEMENT_STICKY && hst.current_state==blue_h.HOST_UP){
			hst.problem_has_been_acknowledged=common_h.FALSE;
			hst.acknowledgement_type=common_h.ACKNOWLEDGEMENT_NONE;
		        }

		/* reset the next and last notification times */
		hst.last_host_notification=0;
		hst.next_host_notification=0;

		/* reset notification suppression option */
		hst.no_more_notifications=common_h.FALSE;

		/* the host just recovered, so reset the current host attempt */
		/* 11/11/05 EG - moved below */
		/*
		if(hst.current_state==HOST_UP)
			hst.current_attempt=1;
		*/

		/* write the host state change to the main log file */
		if(hst.state_type==common_h.HARD_STATE || (hst.state_type==common_h.SOFT_STATE && blue.log_host_retries==common_h.TRUE))
			logging.log_host_event(hst);

		/* check for start of flexible (non-fixed) scheduled downtime */
		if(hst.state_type==common_h.HARD_STATE)
			downtime.check_pending_flex_host_downtime(hst);

		/* notify contacts about the recovery or problem if its a "hard" state */
		if(hst.state_type==common_h.HARD_STATE)
			notifications.host_notification(hst,blue_h.NOTIFICATION_NORMAL,null,null);

		/* handle the host state change */
		handle_host_event(hst);

		/* the host just recovered, so reset the current host attempt */
		if(hst.current_state==blue_h.HOST_UP)
			hst.current_attempt=1;

		/* the host recovered, so reset the current notification number and state flags (after the recovery notification has gone out) */
		if(hst.current_state==blue_h.HOST_UP){
			hst.current_notification_number=0;
			hst.notified_on_down=common_h.FALSE;
			hst.notified_on_unreachable=common_h.FALSE;
		        }
	        }

	/* else the host state has not changed */
	else{

		/* notify contacts if host is still down or unreachable */
		if(hst.current_state!=blue_h.HOST_UP && hst.state_type==common_h.HARD_STATE)
			notifications.host_notification(hst,blue_h.NOTIFICATION_NORMAL,null,null);

		/* if we're in a soft state and we should log host retries, do so now... */
		if(hst.state_type==common_h.SOFT_STATE && blue.log_host_retries==common_h.TRUE)
			logging.log_host_event(hst);
	        }

logger.trace( "exiting " + cn + ".handle_host_state" );

	return common_h.OK;
        }


}