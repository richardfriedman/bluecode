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

/*********** COMMON HEADER FILES ***********/

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.common.comments;
import org.blue.star.common.statusdata;
import org.blue.star.include.blue_h;
import org.blue.star.include.broker_h;
import org.blue.star.include.comments_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;

public class flapping { 

    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.base.flapping");
    private static String cn = "org.blue.base.flapping";

/******************************************************************/
/******************** FLAP DETECTION FUNCTIONS ********************/
/******************************************************************/


/* detects service flapping */
public static void check_for_service_flapping(objects_h.service svc, int update_history){
	int is_flapping=common_h.FALSE;
	int x,y;
	int last_state_history_value=blue_h.STATE_OK;
	double curved_changes=0.0;
	double curved_percent_change=0.0;
	double low_threshold=0.0;
	double high_threshold=0.0;
	double low_curve_value=0.75;
	double high_curve_value=1.25;

	logger.trace( "entering " + cn + ".check_for_service_flapping" );

	/* if this is a soft service state and not a soft recovery, don't record this in the history */
	/* only hard states and soft recoveries get recorded for flap detection */
	if(svc.state_type==common_h.SOFT_STATE && svc.current_state!=blue_h.STATE_OK)
		return;

	/* what threshold values should we use (global or service-specific)? */
	low_threshold=(svc.low_flap_threshold<=0.0)?blue.low_service_flap_threshold:svc.low_flap_threshold;
	high_threshold=(svc.high_flap_threshold<=0.0)?blue.high_service_flap_threshold:svc.high_flap_threshold;

	if(update_history==common_h.TRUE){

		/* record the current state in the state history */
		svc.state_history[svc.state_history_index]=svc.current_state;

		/* increment state history index to next available slot */
		svc.state_history_index++;
		if(svc.state_history_index>=objects_h.MAX_STATE_HISTORY_ENTRIES)
			svc.state_history_index=0;
	        }

	/* calculate overall and curved percent state changes */
	for(x=0,y=svc.state_history_index;x<objects_h.MAX_STATE_HISTORY_ENTRIES;x++){

		if(x==0){
			last_state_history_value=svc.state_history[y];
			y++;
			if(y>=objects_h.MAX_STATE_HISTORY_ENTRIES)
				y=0;
			continue;
		        }

		if(last_state_history_value!=svc.state_history[y])
			curved_changes+=(((x-1)*(high_curve_value-low_curve_value))/((objects_h.MAX_STATE_HISTORY_ENTRIES-2)))+low_curve_value;

		last_state_history_value=svc.state_history[y];

		y++;
		if(y>=objects_h.MAX_STATE_HISTORY_ENTRIES)
			y=0;
	        }

	/* calculate overall percent change in state */
	curved_percent_change=(( curved_changes*100.0)/ (objects_h.MAX_STATE_HISTORY_ENTRIES-1));

	svc.percent_state_change=curved_percent_change;


	/* are we flapping, undecided, or what?... */

	/* we're undecided, so don't change the current flap state */
	if(curved_percent_change>low_threshold && curved_percent_change<high_threshold)
		return;

	/* we're below the lower bound, so we're not flapping */
	else if(curved_percent_change<=low_threshold)
		is_flapping=common_h.FALSE;
       
	/* else we're above the upper bound, so we are flapping */
	else if(curved_percent_change>=high_threshold)
		is_flapping=common_h.TRUE;

	/* so what should we do (if anything)? */

	/* don't do anything if we don't have flap detection enabled on a program-wide basis */
	if(blue.enable_flap_detection==common_h.FALSE)
		return;

	/* don't do anything if we don't have flap detection enabled for this service */
	if(svc.flap_detection_enabled==common_h.FALSE)
		return;

	/* did the service just start flapping? */
	if(is_flapping==common_h.TRUE && svc.is_flapping==common_h.FALSE)
		set_service_flap(svc,curved_percent_change,high_threshold,low_threshold);

	/* did the service just stop flapping? */
	else if(is_flapping==common_h.FALSE && svc.is_flapping==common_h.TRUE)
		clear_service_flap(svc,curved_percent_change,high_threshold,low_threshold);

	logger.trace( "exiting " + cn + ".check_for_service_flapping" );

	return;
        }


/* detects host flapping */
public static void check_for_host_flapping(objects_h.host hst, int update_history){
	int is_flapping=common_h.FALSE;
	int x;
	int last_state_history_value=blue_h.HOST_UP;
	long wait_threshold;
	double curved_changes=0.0;
	double curved_percent_change=0.0;
	long current_time;
	double low_threshold=0.0;
	double high_threshold=0.0;
	double low_curve_value=0.75;
	double high_curve_value=1.25;

	logger.trace( "entering " + cn + ".check_for_host_flapping" );

	current_time = utils.currentTimeInSeconds();

	/* period to wait for updating archived state info if we have no state change */
	if(hst.total_services==0)
		wait_threshold=hst.notification_interval*blue.interval_length;
	
	else
		wait_threshold=(hst.total_service_check_interval*blue.interval_length)/hst.total_services;

	/* if we haven't waited long enough since last record, only update if we've had a state change */
	if((current_time-hst.last_state_history_update)<wait_threshold){

		/* get the last recorded state */
		last_state_history_value=hst.state_history[(hst.state_history_index==0)?objects_h.MAX_STATE_HISTORY_ENTRIES-1:hst.state_history_index-1];

		/* if we haven't had a state change since our last recorded state, bail out */
		if(last_state_history_value==hst.current_state)
			return;
	        }

	/* what thresholds should we use (global or host-specific)? */
	low_threshold=(hst.low_flap_threshold<=0.0)?blue.low_host_flap_threshold:hst.low_flap_threshold;
	high_threshold=(hst.high_flap_threshold<=0.0)?blue.high_host_flap_threshold:hst.high_flap_threshold;

	if(update_history==common_h.TRUE){

		/* update the last record time */
		hst.last_state_history_update=current_time;

		/* record the current state in the state history */
		hst.state_history[hst.state_history_index]=hst.current_state;

		/* increment state history index to next available slot */
		hst.state_history_index++;
		if(hst.state_history_index>=objects_h.MAX_STATE_HISTORY_ENTRIES)
			hst.state_history_index=0;
	        }

	/* calculate overall changes in state */
	for(x=0;x<objects_h.MAX_STATE_HISTORY_ENTRIES;x++){

		if(x==0){
			last_state_history_value=hst.state_history[x];
			continue;
		        }

		if(last_state_history_value!=hst.state_history[x])
			curved_changes+=(((x-1)*(high_curve_value-low_curve_value))/((objects_h.MAX_STATE_HISTORY_ENTRIES-2)))+low_curve_value;

		last_state_history_value=hst.state_history[x];
	        }

	/* calculate overall percent change in state */
	curved_percent_change=((curved_changes*100.0)/(objects_h.MAX_STATE_HISTORY_ENTRIES-1));

	hst.percent_state_change=curved_percent_change;


	/* are we flapping, undecided, or what?... */

	/* we're undecided, so don't change the current flap state */
	if(curved_percent_change>low_threshold && curved_percent_change<high_threshold)
		return;

	/* we're below the lower bound, so we're not flapping */
	else if(curved_percent_change<=low_threshold)
		is_flapping=common_h.FALSE;
       
	/* else we're above the upper bound, so we are flapping */
	else if(curved_percent_change>=high_threshold)
		is_flapping=common_h.TRUE;

	/* so what should we do (if anything)? */

	/* don't do anything if we don't have flap detection enabled on a program-wide basis */
	if(blue.enable_flap_detection==common_h.FALSE)
		return;

	/* don't do anything if we don't have flap detection enabled for this host */
	if(hst.flap_detection_enabled==common_h.FALSE)
		return;

	/* did the host just start flapping? */
	if(is_flapping==common_h.TRUE && hst.is_flapping==common_h.FALSE)
		set_host_flap(hst,curved_percent_change,high_threshold,low_threshold);

	/* did the host just stop flapping? */
	else if(is_flapping==common_h.FALSE && hst.is_flapping==common_h.TRUE)
		clear_host_flap(hst,curved_percent_change,high_threshold,low_threshold);

	logger.trace( "exiting " + cn + ".check_for_host_flapping" );

	return;
        }


/******************************************************************/
/********************* FLAP HANDLING FUNCTIONS ********************/
/******************************************************************/


/* handles a service that is flapping */
public static void set_service_flap(objects_h.service svc, double percent_change, double high_threshold, double low_threshold){

	logger.trace( "entering " + cn + ".set_service_flap" );

	/* log a notice - this one is parsed by the history CGI */
    String buffer = "SERVICE FLAPPING ALERT: "+svc.host_name+";"+svc.description+";STARTED; Service appears to have started flapping ("+percent_change+"% change >= "+high_threshold+"% threshold)";
    logger.fatal( buffer );

	/* add a non-persistent comment to the service */
    buffer = "Notifications for this service are being suppressed because it was detected as having been flapping between different states ("+percent_change+"% change >= "+high_threshold+"% threshold).  When the service state stabilizes and the flapping stops, notifications will be re-enabled.";
    
	comments_h.comment temp_comment = comments.add_new_service_comment( comments_h.FLAPPING_COMMENT,svc.host_name,svc.description,utils.currentTimeInSeconds(),"(Nagios Process)",buffer,0,comments_h.COMMENTSOURCE_INTERNAL,common_h.FALSE,0);
    svc.flapping_comment_id = temp_comment.comment_id;

	/* set the flapping indicator */
	svc.is_flapping=common_h.TRUE;

	/* send data to event broker */
	broker.broker_flapping_data(broker_h.NEBTYPE_FLAPPING_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.SERVICE_FLAPPING,svc,percent_change,high_threshold,low_threshold,null);

	/* see if we should check to send a recovery notification out when flapping stops */
	if(svc.current_state!=blue_h.STATE_OK && svc.current_notification_number>0)
		svc.check_flapping_recovery_notification=common_h.TRUE;
	else
		svc.check_flapping_recovery_notification=common_h.FALSE;

	/* send a notification */
	notifications.service_notification(svc,blue_h.NOTIFICATION_FLAPPINGSTART,null,null);

	logger.trace( "exiting " + cn + ".set_service_flap" );

	return;
        }


/* handles a service that has stopped flapping */
public static void clear_service_flap(objects_h.service svc, double percent_change, double high_threshold, double low_threshold){

	logger.trace( "entering " + cn + ".clear_service_flap" );

	/* log a notice - this one is parsed by the history CGI */
	String buffer = "SERVICE FLAPPING ALERT: "+svc.host_name+";"+svc.description+";STOPPED; Service appears to have stopped flapping ("+percent_change+"% change < "+low_threshold+"% threshold)";
	logger.fatal( buffer );

	/* delete the comment we added earlier */
	if(svc.flapping_comment_id!=0)
		comments.delete_service_comment(svc.flapping_comment_id);
	svc.flapping_comment_id=0;

	/* clear the flapping indicator */
	svc.is_flapping=common_h.FALSE;

	/* send data to event broker */
	broker.broker_flapping_data(broker_h.NEBTYPE_FLAPPING_STOP,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_FLAPPING_STOP_NORMAL,blue_h.SERVICE_FLAPPING,svc,percent_change,high_threshold,low_threshold,null);

    /* added 2.7 send a notification */
    notifications.service_notification(svc, blue_h.NOTIFICATION_FLAPPINGSTOP, null, null);
    
	/* should we send a recovery notification? */
	if(svc.check_flapping_recovery_notification==common_h.TRUE && svc.current_state==blue_h.STATE_OK)
        notifications.service_notification(svc,blue_h.NOTIFICATION_NORMAL,null,null);

	/* clear the recovery notification flag */
	svc.check_flapping_recovery_notification=common_h.FALSE;
    
	logger.trace( "exiting " + cn + ".clear_service_flap" );

	return;
        }


/* handles a host that is flapping */
public static void set_host_flap(objects_h.host hst, double percent_change, double high_threshold, double low_threshold){
	String buffer;

	logger.trace( "entering " + cn + ".set_host_flap" );

	/* log a notice - this one is parsed by the history CGI */
    buffer =  "HOST FLAPPING ALERT: "+hst.name+";STARTED; Host appears to have started flapping ("+percent_change+"% change > "+high_threshold+"% threshold)";
	logger.warn(buffer);

	/* add a non-persistent comment to the host */
	buffer = "Notifications for this host are being suppressed because it was detected as having been flapping between different states ("+percent_change+"% change > "+high_threshold+"% threshold).  When the host state stabilizes and the flapping stops, notifications will be re-enabled.";
	comments_h.comment temp_comment = comments.add_new_host_comment( comments_h.FLAPPING_COMMENT,hst.name,utils.currentTimeInSeconds(),"(Nagios Process)",buffer,0, comments_h.COMMENTSOURCE_INTERNAL,common_h.FALSE,0);
    hst.flapping_comment_id = temp_comment.comment_id;
    

	/* set the flapping indicator */
	hst.is_flapping=common_h.TRUE;

	/* send data to event broker */
	broker.broker_flapping_data(broker_h.NEBTYPE_FLAPPING_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.HOST_FLAPPING,hst,percent_change,high_threshold,low_threshold,null);

	/* see if we should check to send a recovery notification out when flapping stops */
	if(hst.current_state!=blue_h.HOST_UP && hst.current_notification_number>0)
		hst.check_flapping_recovery_notification=common_h.TRUE;
	else
		hst.check_flapping_recovery_notification=common_h.FALSE;

	/* send a notification */
	notifications.host_notification(hst,blue_h.NOTIFICATION_FLAPPINGSTART,null,null);

	logger.trace( "exiting " + cn + ".set_host_flap" );

	return;
        }


/* handles a host that has stopped flapping */
public static void clear_host_flap(objects_h.host hst, double percent_change, double high_threshold, double low_threshold){

	logger.trace( "entering " + cn + ".clear_host_flap" );

	/* log a notice - this one is parsed by the history CGI */
	String buffer = "HOST FLAPPING ALERT: "+hst.name+";STOPPED; Host appears to have stopped flapping ("+percent_change+"% change < "+low_threshold+"% threshold)";
	logger.fatal(buffer);

	/* delete the comment we added earlier */
	if(hst.flapping_comment_id!=0)
		comments.delete_host_comment(hst.flapping_comment_id);
	hst.flapping_comment_id=0;

	/* clear the flapping indicator */
	hst.is_flapping=common_h.FALSE;

	/* send data to event broker */
	broker.broker_flapping_data(broker_h.NEBTYPE_FLAPPING_STOP,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_FLAPPING_STOP_NORMAL,blue_h.HOST_FLAPPING,hst,percent_change,high_threshold,low_threshold,null);

    /* send a notification */
    notifications.host_notification(hst,blue_h.NOTIFICATION_FLAPPINGSTOP,null,null);

	/* should we send a recovery notification? */
	if(hst.check_flapping_recovery_notification==common_h.TRUE && hst.current_state==blue_h.HOST_UP)
		notifications.host_notification(hst,blue_h.NOTIFICATION_NORMAL,null,null);

	/* clear the recovery notification flag */
	hst.check_flapping_recovery_notification=common_h.FALSE;

	logger.trace( "exiting " + cn + ".clear_host_flap" );

	return;
        }



/******************************************************************/
/***************** FLAP DETECTION STATUS FUNCTIONS ****************/
/******************************************************************/

/* enables flap detection on a program wide basis */
public static void enable_flap_detection_routines(){

	logger.trace( "entering " + cn + ".enable_flap_detection" );

	/* set flap detection flag */
	blue.enable_flap_detection=common_h.TRUE;

	/* update program status */
	statusdata.update_program_status(common_h.FALSE);

	logger.trace( "exiting " + cn + ".enable_flap_detection" );

	return;
        }



/* disables flap detection on a program wide basis */
public static void disable_flap_detection_routines(){

	logger.trace( "entering " + cn + ".disable_flap_detection" );

	/* set flap detection flag */
	blue.enable_flap_detection=common_h.FALSE;

	/* update program status */
	statusdata.update_program_status(common_h.FALSE);

	logger.trace( "exiting " + cn + ".disable_flap_detection" );

	return;
        }



/* enables flap detection for a specific host */
public static void enable_host_flap_detection(objects_h.host hst){
	int x;

	logger.trace( "entering " + cn + ".enable_host_flap_detection" );

	/* nothing to do... */
	if(hst.flap_detection_enabled==common_h.TRUE)
		return;

	/* reset the archived state history */
	for(x=0;x<objects_h.MAX_STATE_HISTORY_ENTRIES;x++)
		hst.state_history[x]=hst.current_state;

	/* reset percent state change indicator */
	hst.percent_state_change=0.0;

	/* set the flap detection enabled flag */
	hst.flap_detection_enabled=common_h.TRUE;

	/* update host status */
	statusdata.update_host_status(hst,common_h.FALSE);

	logger.trace( "exiting " + cn + ".enable_host_flap_detection" );

	return;
        }



/* disables flap detection for a specific host */
public static void disable_host_flap_detection(objects_h.host hst){

	logger.trace( "entering " + cn + ".disable_host_flap_detection" );

	/* nothing to do... */
	if(hst.flap_detection_enabled==common_h.FALSE)
		return;

	/* set the flap detection enabled flag */
	hst.flap_detection_enabled=common_h.FALSE;

	/* if the host was flapping, remove the flapping indicator */
	if(hst.is_flapping==common_h.TRUE){

		hst.is_flapping=common_h.FALSE;

		/* delete the original comment we added earlier */
		if(hst.flapping_comment_id!=0)
			comments.delete_host_comment(hst.flapping_comment_id);
		hst.flapping_comment_id=0;

		/* log a notice - this one is parsed by the history CGI */
		logger.fatal( "HOST FLAPPING ALERT: "+hst.name+";DISABLED; Flap detection has been disabled");

		/* send data to event broker */
		broker.broker_flapping_data(broker_h.NEBTYPE_FLAPPING_STOP,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_FLAPPING_STOP_DISABLED,blue_h.HOST_FLAPPING,hst,hst.percent_state_change,0.0,0.0,null);
	}

	/* reset the percent change indicator */
	hst.percent_state_change=0.0;

	/* update host status */
	statusdata.update_host_status(hst,common_h.FALSE);

	logger.trace( "exiting " + cn + ".disable_host_flap_detection" );

	return;
        }


/* enables flap detection for a specific service */
public static void enable_service_flap_detection(objects_h.service svc){
	int x;

	logger.trace( "entering " + cn + ".enable_service_flap_detection" );

	/* nothing to do... */
	if(svc.flap_detection_enabled==common_h.TRUE)
		return;

	/* reset the archived state history */
	for(x=0;x<objects_h.MAX_STATE_HISTORY_ENTRIES;x++)
		svc.state_history[x]=svc.current_state;

	/* reset percent state change indicator */
	svc.percent_state_change=0.0;

	/* set the flap detection enabled flag */
	svc.flap_detection_enabled=common_h.TRUE;

	/* update service status */
	statusdata.update_service_status(svc,common_h.FALSE);

	logger.trace( "exiting " + cn + ".enable_service_flap_detection" );

	return;
        }



/* disables flap detection for a specific service */
public static void disable_service_flap_detection(objects_h.service svc){

	logger.trace( "entering " + cn + ".disable_service_flap_detection" );

	/* nothing to do... */
	if(svc.flap_detection_enabled==common_h.FALSE)
		return;

	/* set the flap detection enabled flag */
	svc.flap_detection_enabled=common_h.FALSE;

	/* if the service was flapping, remove the flapping indicator */
	if(svc.is_flapping==common_h.TRUE){

		svc.is_flapping=common_h.FALSE;

		/* delete the original comment we added earlier */
		if(svc.flapping_comment_id!=0)
			comments.delete_service_comment(svc.flapping_comment_id);
		svc.flapping_comment_id=0;

		/* log a notice - this one is parsed by the history CGI */
		logger.fatal( "SERVICE FLAPPING ALERT: "+svc.host_name+";"+svc.description+";DISABLED; Flap detection has been disabled");

		/* send data to event broker */
		broker.broker_flapping_data(broker_h.NEBTYPE_FLAPPING_STOP,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_FLAPPING_STOP_DISABLED,blue_h.SERVICE_FLAPPING,svc,svc.percent_state_change,0.0,0.0,null);
	}

	/* reset the percent change indicator */
	svc.percent_state_change=0.0;

	/* update service status */
	statusdata.update_service_status(svc,common_h.FALSE);

	logger.trace( "exiting " + cn + ".disable_service_flap_detection" );

	return;
        }
}
