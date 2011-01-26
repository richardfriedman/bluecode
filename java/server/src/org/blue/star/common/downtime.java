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

package org.blue.star.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.base.blue;
import org.blue.star.base.broker;
import org.blue.star.base.events;
import org.blue.star.base.utils;
import org.blue.star.include.blue_h;
import org.blue.star.include.broker_h;
import org.blue.star.include.comments_h;
import org.blue.star.include.common_h;
import org.blue.star.include.downtime_h;
import org.blue.star.include.objects_h;
import org.blue.star.xdata.xdddefault;

public class downtime {
  
    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.common");
    private static String cn = "org.blue.common.downtime";
    
    public static ArrayList scheduled_downtime_list = new ArrayList(); // scheduled_downtime

/******************************************************************/
/**************** INITIALIZATION/CLEANUP FUNCTIONS ****************/
/******************************************************************/


    /* initializes scheduled downtime data */
    public static int initialize_downtime_data(String config_file){
        /**** IMPLEMENTATION-SPECIFIC CALLS ****/
        return xdddefault.xdddefault_initialize_downtime_data(config_file);
    }
    
    
    /* cleans up scheduled downtime data */
    public static int cleanup_downtime_data(String config_file){
        int result;
        
        /**** IMPLEMENTATION-SPECIFIC CALLS ****/
        result = xdddefault.xdddefault_cleanup_downtime_data(config_file);
        
        /* free memory allocated to downtime data */
        free_downtime_data();
        
        return result;
    }


/******************************************************************/
/********************** SCHEDULING FUNCTIONS **********************/
/******************************************************************/
    
    /* schedules a host or service downtime */
    public static downtime_h.scheduled_downtime schedule_downtime(int type, String host_name, String service_description, long entry_time, String author, String comment_data, long start_time, long end_time, int fixed, long triggered_by, long duration ){
        
        /* don't add old or invalid downtimes */
        if(start_time>=end_time || end_time<= utils.currentTimeInSeconds() )
            return null;
        
        /* add a new downtime entry */
        downtime_h.scheduled_downtime new_downtime = add_new_downtime(type,host_name,service_description,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration );
        
        /* register the scheduled downtime */
        register_downtime(type,new_downtime.downtime_id);
        
        return new_downtime;
    }

    
    /* unschedules a host or service downtime */
    public static int unschedule_downtime(int type, long downtime_id){
        downtime_h.scheduled_downtime temp_downtime;
        objects_h.host hst=null;
        objects_h.service svc=null;
        blue_h.timed_event temp_event=null;
        String temp_buffer;	
        
        /* find the downtime entry in the list in memory */
        temp_downtime = find_downtime(type,downtime_id);
        if(temp_downtime==null)
            return common_h.ERROR;
        
        /* find the host or service associated with this downtime */
        if(temp_downtime.type==common_h.HOST_DOWNTIME){
            hst=objects.find_host(temp_downtime.host_name);
            if(hst==null)
                return common_h.ERROR;
        }
        else{
            svc=objects.find_service(temp_downtime.host_name,temp_downtime.service_description);
            if(svc==null)
                return common_h.ERROR;
        }
        
        /* decrement pending flex downtime if necessary ... */
        if(temp_downtime.fixed==common_h.FALSE && temp_downtime.incremented_pending_downtime==common_h.TRUE){
            if(temp_downtime.type==common_h.HOST_DOWNTIME)
                hst.pending_flex_downtime--;
            else
                svc.pending_flex_downtime--;
        }
        
        /* decrement the downtime depth variable and update status data if necessary */
        if(temp_downtime.is_in_effect==common_h.TRUE){
            
           /* send data to event broker */
           broker.broker_downtime_data(broker_h.NEBTYPE_DOWNTIME_STOP,broker_h.NEBFLAG_NONE, broker_h.NEBATTR_DOWNTIME_STOP_CANCELLED,temp_downtime.type,temp_downtime.host_name,temp_downtime.service_description,temp_downtime.entry_time,temp_downtime.author,temp_downtime.comment,temp_downtime.start_time,temp_downtime.end_time,temp_downtime.fixed,temp_downtime.triggered_by,temp_downtime.duration,temp_downtime.downtime_id,null);
            
            if(temp_downtime.type==common_h.HOST_DOWNTIME){
                
                hst.scheduled_downtime_depth--;
                statusdata.update_host_status(hst,common_h.FALSE);
                
                /* log a notice - this is parsed by the history CGI */
                if(hst.scheduled_downtime_depth==0)
                    logger.warn( "HOST DOWNTIME ALERT: "+hst.name+";CANCELLED; Scheduled downtime for host has been cancelled." );
            }
            
            else{
                
                svc.scheduled_downtime_depth--;
                statusdata.update_service_status(svc,common_h.FALSE);
                
                /* log a notice - this is parsed by the history CGI */
                if(svc.scheduled_downtime_depth==0)
                    logger.warn( "SERVICE DOWNTIME ALERT: "+svc.host_name+";"+svc.description+";CANCELLED; Scheduled downtime for service has been cancelled.");
            }
        }
        
        /* remove scheduled entry from event queue */
        
        for ( Iterator iter = events.event_list_high.iterator(); iter.hasNext(); ) {
           temp_event = (blue_h.timed_event) iter.next();
           if(temp_event.event_type!=blue_h.EVENT_SCHEDULED_DOWNTIME)
              continue;           
           if ( ((Long) temp_event.event_data) == downtime_id ) 
              break;
           temp_event = null;
        }

        if(temp_event!=null)
           events.remove_event(temp_event, events.event_list_high);
        
        /* delete downtime entry */
        if(temp_downtime.type==common_h.HOST_DOWNTIME)
            delete_host_downtime(downtime_id);
        else
            delete_service_downtime(downtime_id);
        
        /* unschedule all downtime entries that were triggered by this one */
        for ( ListIterator iter = scheduled_downtime_list.listIterator(); iter.hasNext(); ) {
            temp_downtime = (downtime_h.scheduled_downtime) iter.next();
            if ( temp_downtime.triggered_by == downtime_id )
               unschedule_downtime( common_h.ANY_DOWNTIME,temp_downtime.downtime_id);
        }
        return common_h.OK;
    }
    


/* registers scheduled downtime (schedules it, adds comments, etc.) */
public static int register_downtime(int type,  long downtime_id){
 	String temp_buffer;
	downtime_h.scheduled_downtime temp_downtime;
	objects_h.host hst=null;
	objects_h.service svc=null;
    comments_h.comment comment;
	String type_string="";
	int hours;
	int minutes;

	/* find the downtime entry in memory */
	temp_downtime = find_downtime(type,downtime_id);
	if(temp_downtime==null)
		return common_h.ERROR;

	/* find the host or service associated with this downtime */
	if(temp_downtime.type==common_h.HOST_DOWNTIME){
		hst=objects.find_host(temp_downtime.host_name);
		if(hst==null)
			return common_h.ERROR;
	        }
	else{
		svc=objects.find_service(temp_downtime.host_name,temp_downtime.service_description);
		if(svc==null)
			return common_h.ERROR;
	        }

	/* create the comment */
    
    String start_time_string = new Date( temp_downtime.start_time * 1000 ).toString();
    String end_time_string = new Date( temp_downtime.end_time * 1000 ).toString();
	hours= (int) temp_downtime.duration/3600;
	minutes= (int) ((temp_downtime.duration-(hours*3600))/60);
	if(temp_downtime.type==common_h.HOST_DOWNTIME)
		type_string="host";
	else
		type_string="service";
    
	if(temp_downtime.fixed==common_h.TRUE){
	    temp_buffer = "This "+type_string+" has been scheduled for fixed downtime from "+start_time_string+" to "+end_time_string+".  Notifications for the "+type_string+" will not be sent out during that time period.";
	        }
	else{
		temp_buffer = "This "+type_string+" has been scheduled for flexible downtime starting between "+start_time_string+" and "+end_time_string+" and lasting for a period of "+hours+" hours and "+minutes+" minutes.  Notifications for the "+type_string+" will not be sent out during that time period.";
	        }

	/* add a non-persistent comment to the host or service regarding the scheduled outage */
	if(temp_downtime.type==common_h.SERVICE_DOWNTIME)
	    comment = comments.add_new_comment(comments_h.SERVICE_COMMENT,comments_h.DOWNTIME_COMMENT,svc.host_name,svc.description, utils.currentTimeInSeconds(),"(Nagios Process)",temp_buffer,0,comments_h.COMMENTSOURCE_INTERNAL,common_h.FALSE, 0);
	else
		comment = comments.add_new_comment(comments_h.HOST_COMMENT,comments_h.DOWNTIME_COMMENT,hst.name,null,utils.currentTimeInSeconds(),"(Nagios Process)",temp_buffer,0,comments_h.COMMENTSOURCE_INTERNAL,common_h.FALSE,0 );

    temp_downtime.comment_id = comment.comment_id;

	/*** SCHEDULE DOWNTIME - FLEXIBLE (NON-FIXED) DOWNTIME IS HANDLED AT A LATER POINT ***/

    
	/* only non-triggered downtime is scheduled... */
	if(temp_downtime.triggered_by==0)
        events.schedule_new_event(blue_h.EVENT_SCHEDULED_DOWNTIME,common_h.TRUE,temp_downtime.start_time,common_h.FALSE,0,null,common_h.FALSE,temp_downtime.downtime_id,null);

	return common_h.OK;
        }



/* handles scheduled downtime (id passed from timed event queue) */
public static int handle_scheduled_downtime_by_id(long downtime_id){
    downtime_h.scheduled_downtime temp_downtime=null;

    /* find the downtime entry */
    temp_downtime = find_downtime( common_h.ANY_DOWNTIME, downtime_id);
    if( temp_downtime == null )
        return common_h.ERROR;

    /* handle the downtime */
    return handle_scheduled_downtime(temp_downtime);
    }
    

/* handles scheduled host or service downtime */
public static int handle_scheduled_downtime(downtime_h.scheduled_downtime temp_downtime){
	downtime_h.scheduled_downtime this_downtime;
	objects_h.host hst=null;
    objects_h.service svc=null;
	long event_time;

    logger.trace("entering " + cn + ".handle_scheduled_downtime");

	if(temp_downtime==null)
		return common_h.ERROR;
	
	/* find the host or service associated with this downtime */
	if(temp_downtime.type==common_h.HOST_DOWNTIME){
	    hst=objects.find_host(temp_downtime.host_name);
	    if(hst==null)
	        return common_h.ERROR;
	}
	else{
	    svc=objects.find_service(temp_downtime.host_name,temp_downtime.service_description);
	    if(svc==null)
	        return common_h.ERROR;
	}
	
	/* if downtime if flexible and host/svc is in an ok state, don't do anything right now (wait for event handler to kill it off) */
	/* start_flex_downtime variable is set to TRUE by event handler functions */
	if(temp_downtime.fixed==common_h.FALSE){
	    
	    /* we're not supposed to force a start of flex downtime... */
	    if(temp_downtime.start_flex_downtime==common_h.FALSE){
	        
	        /* host is up or service is ok, so we don't really do anything right now */
	        if((temp_downtime.type==common_h.HOST_DOWNTIME && hst.current_state==blue_h.HOST_UP) || (temp_downtime.type==common_h.SERVICE_DOWNTIME && svc.current_state==blue_h.STATE_OK)){
	            
	            /* increment pending flex downtime counter */
	            if(temp_downtime.type==common_h.HOST_DOWNTIME)
	                hst.pending_flex_downtime++;
	            else
	                svc.pending_flex_downtime++;
	            temp_downtime.incremented_pending_downtime=common_h.TRUE;
	            
	            /*** SINCE THE FLEX DOWNTIME MAY NEVER START, WE HAVE TO PROVIDE A WAY OF EXPIRING UNUSED DOWNTIME... ***/
	            
	            events.schedule_new_event( blue_h.EVENT_EXPIRE_DOWNTIME,common_h.TRUE,(temp_downtime.end_time+1),common_h.FALSE,0,null,common_h.FALSE,null,null);
	            
	            return common_h.OK;
	        }
	    }
	}

	/* have we come to the end of the scheduled downtime? */
	if(temp_downtime.is_in_effect==common_h.TRUE){

		/* send data to event broker */
		broker.broker_downtime_data(broker_h.NEBTYPE_DOWNTIME_STOP,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_DOWNTIME_STOP_NORMAL,temp_downtime.type,temp_downtime.host_name,temp_downtime.service_description,temp_downtime.entry_time,temp_downtime.author,temp_downtime.comment,temp_downtime.start_time,temp_downtime.end_time,temp_downtime.fixed,temp_downtime.triggered_by,temp_downtime.duration,temp_downtime.downtime_id,null);

		/* decrement the downtime depth variable */
		if(temp_downtime.type==common_h.HOST_DOWNTIME)
			hst.scheduled_downtime_depth--;
		else
			svc.scheduled_downtime_depth--;

		if(temp_downtime.type==common_h.HOST_DOWNTIME && hst.scheduled_downtime_depth==0){
            /* log a notice - this one is parsed by the history CGI */
            logger.warn("HOST DOWNTIME ALERT: "+hst.name+";STOPPED; Host has exited from a period of scheduled downtime");
		        }

		else if(temp_downtime.type==common_h.SERVICE_DOWNTIME && svc.scheduled_downtime_depth==0){

			/* log a notice - this one is parsed by the history CGI */
            logger.warn( "SERVICE DOWNTIME ALERT: "+svc.host_name+";"+svc.description+";STOPPED; Service has exited from a period of scheduled downtime");
		        }


		/* update the status data */
		if(temp_downtime.type==common_h.HOST_DOWNTIME)
            statusdata.update_host_status(hst,common_h.FALSE);
		else
			statusdata.update_service_status(svc,common_h.FALSE);

		/* decrement pending flex downtime if necessary */
		if(temp_downtime.fixed==common_h.FALSE && temp_downtime.incremented_pending_downtime==common_h.TRUE){
			if(temp_downtime.type==common_h.HOST_DOWNTIME){
				if(hst.pending_flex_downtime>0)
					hst.pending_flex_downtime--;
			        }
			else{
				if(svc.pending_flex_downtime>0)
					svc.pending_flex_downtime--;
			        }
		        }

        /* handle (stop) downtime that is triggered by this one */
        for (ListIterator iter = scheduled_downtime_list.listIterator(); iter.hasNext(); ) {
            this_downtime = (downtime_h.scheduled_downtime) iter.next();
            if(this_downtime.triggered_by==temp_downtime.downtime_id)
                handle_scheduled_downtime(this_downtime);
        }
        
        /* delete downtime entry from the log */
        if(temp_downtime.type==common_h.HOST_DOWNTIME)
           delete_host_downtime(temp_downtime.downtime_id);
        else
           delete_service_downtime(temp_downtime.downtime_id);

	}

	/* else we are just starting the scheduled downtime */
	else{

		/* send data to event broker */
		broker.broker_downtime_data(broker_h.NEBTYPE_DOWNTIME_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,temp_downtime.type,temp_downtime.host_name,temp_downtime.service_description,temp_downtime.entry_time,temp_downtime.author,temp_downtime.comment,temp_downtime.start_time,temp_downtime.end_time,temp_downtime.fixed,temp_downtime.triggered_by,temp_downtime.duration,temp_downtime.downtime_id,null);

		if(temp_downtime.type==common_h.HOST_DOWNTIME && hst.scheduled_downtime_depth==0){

			/* log a notice - this one is parsed by the history CGI */
            logger.warn("HOST DOWNTIME ALERT: "+hst.name+";STARTED; Host has entered a period of scheduled downtime");
		        }

		else if(temp_downtime.type==common_h.SERVICE_DOWNTIME && svc.scheduled_downtime_depth==0){

			/* log a notice - this one is parsed by the history CGI */
            logger.warn( "SERVICE DOWNTIME ALERT: "+svc.host_name+";"+svc.description+";STOPPED; Service has entered a period of scheduled downtime");
		        }

		/* increment the downtime depth variable */
		if(temp_downtime.type==common_h.HOST_DOWNTIME)
			hst.scheduled_downtime_depth++;
		else
			svc.scheduled_downtime_depth++;

		/* set the in effect flag */
		temp_downtime.is_in_effect=common_h.TRUE;

		/* update the status data */
		if(temp_downtime.type==common_h.HOST_DOWNTIME)
			statusdata.update_host_status(hst,common_h.FALSE);
		else
            statusdata.update_service_status(svc,common_h.FALSE);

		/* schedule an event */
		if(temp_downtime.fixed==common_h.FALSE)
			event_time=utils.currentTimeInSeconds()+temp_downtime.duration ;
		else
			event_time=temp_downtime.end_time;

        events.schedule_new_event( blue_h.EVENT_SCHEDULED_DOWNTIME, common_h.TRUE,event_time,common_h.FALSE,0,null,common_h.FALSE,temp_downtime.downtime_id,null);

		/* handle (start) downtime that is triggered by this one */
        /* handle (stop) downtime that is triggered by this one */
        for (ListIterator iter = scheduled_downtime_list.listIterator(); iter.hasNext(); ) {
            this_downtime = (downtime_h.scheduled_downtime) iter.next();
            if(this_downtime.triggered_by==temp_downtime.downtime_id)
                handle_scheduled_downtime(this_downtime);
        }
	}
	
    logger.trace("exiting " + cn + ".handle_scheduled_downtime");
	return common_h.OK;
        }



/* checks for flexible (non-fixed) host downtime that should start now */
public static int check_pending_flex_host_downtime(objects_h.host hst){
	downtime_h.scheduled_downtime temp_downtime;
	long current_time;

    logger.trace("entering " + cn + ".check_pending_flex_host_downtime");

	if(hst==null)
		return common_h.ERROR;

    current_time = utils.currentTimeInSeconds();
    
	/* if host is currently up, nothing to do */
	if(hst.current_state==blue_h.HOST_UP)
		return common_h.OK;

	/* check all downtime entries */
    for (ListIterator iter = scheduled_downtime_list.listIterator(); iter.hasNext(); ) {
        temp_downtime = (downtime_h.scheduled_downtime) iter.next();

		if(temp_downtime.type!=common_h.HOST_DOWNTIME)
			continue;

		if(temp_downtime.fixed==common_h.TRUE)
			continue;

		if(temp_downtime.is_in_effect==common_h.TRUE)
			continue;

		/* triggered downtime entries should be ignored here */
		if(temp_downtime.triggered_by!=0)
			continue;

		/* this entry matches our host! */
		if(objects.find_host(temp_downtime.host_name)==hst){
			
			/* if the time boundaries are okay, start this scheduled downtime */
			if(temp_downtime.start_time<=current_time && current_time<=temp_downtime.end_time){
				temp_downtime.start_flex_downtime=common_h.TRUE;
				handle_scheduled_downtime(temp_downtime);
			        }
		        }
	        }

    logger.trace("exiting " + cn + ".check_pending_flex_host_downtime");
	return common_h.OK;
        }


/* checks for flexible (non-fixed) service downtime that should start now */
public static int check_pending_flex_service_downtime(objects_h.service svc){
	downtime_h.scheduled_downtime temp_downtime;
	long current_time;

    logger.trace("entering " + cn + ".check_pending_flex_service_downtime");

	if(svc==null)
		return common_h.ERROR;

    current_time = utils.currentTimeInSeconds();

	/* if service is currently ok, nothing to do */
	if(svc.current_state==blue_h.STATE_OK)
		return common_h.OK;

	/* check all downtime entries */
    for (ListIterator iter = scheduled_downtime_list.listIterator(); iter.hasNext(); ) {
        temp_downtime = (downtime_h.scheduled_downtime) iter.next();

		if(temp_downtime.type!=common_h.SERVICE_DOWNTIME)
			continue;

		if(temp_downtime.fixed==common_h.TRUE)
			continue;

		if(temp_downtime.is_in_effect==common_h.TRUE)
			continue;

		/* triggered downtime entries should be ignored here */
		if(temp_downtime.triggered_by!=0)
			continue;

		/* this entry matches our service! */
		if(objects.find_service(temp_downtime.host_name,temp_downtime.service_description)==svc){

			/* if the time boundaries are okay, start this scheduled downtime */
			if(temp_downtime.start_time<=current_time && current_time<=temp_downtime.end_time){
				temp_downtime.start_flex_downtime=common_h.TRUE;
				handle_scheduled_downtime(temp_downtime);
			        }
		        }
	        }

    logger.trace("exiting " + cn + ".check_pending_flex_service_downtime");
	return common_h.OK;
        }


/* checks for (and removes) expired downtime entries */
public static int check_for_expired_downtime(){
   logger.trace("entering " + cn + ".check_for_expired_downtime");
    
    long current_time = utils.currentTimeInSeconds();
    
    /* check all downtime entries... */
    for ( ListIterator iter = scheduled_downtime_list.listIterator(); iter.hasNext(); ) {
        downtime_h.scheduled_downtime temp_downtime = (downtime_h.scheduled_downtime) iter.next();
        
        /* this entry should be removed */
        if(temp_downtime.is_in_effect==common_h.FALSE && temp_downtime.end_time<current_time){
            /* delete the downtime entry */
            if(temp_downtime.type==common_h.HOST_DOWNTIME)
                delete_host_downtime(temp_downtime.downtime_id);
            else
                delete_service_downtime(temp_downtime.downtime_id);
        }
    }
    
    logger.trace("exiting " + cn + ".check_for_expired_downtime");
    return common_h.OK;
}



/******************************************************************/
/************************* SAVE FUNCTIONS *************************/
/******************************************************************/


/* save a host or service downtime */
public static downtime_h.scheduled_downtime add_new_downtime(int type, String host_name, String service_description, long entry_time, String author, String comment_data, long start_time, long end_time, int fixed, long triggered_by, long duration ){
    if(type==common_h.HOST_DOWNTIME)
        return add_new_host_downtime(host_name,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration );
    else
        return add_new_service_downtime(host_name,service_description,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration );
}


/* saves a host downtime entry */
public static downtime_h.scheduled_downtime add_new_host_downtime(String host_name, long entry_time, String author, String comment_data, long start_time, long end_time, int fixed, long triggered_by, long duration ){
    downtime_h.scheduled_downtime new_downtime;
    
    if(host_name==null)
        return null;
    
    /**** IMPLEMENTATION-SPECIFIC CALLS ****/
    new_downtime = xdddefault.xdddefault_add_new_host_downtime(host_name,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration);
    
    /* send data to event broker */
    broker.broker_downtime_data(broker_h.NEBTYPE_DOWNTIME_ADD,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,common_h.HOST_DOWNTIME,host_name,null,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration,new_downtime.downtime_id,null);
    
    return new_downtime;
}


/* saves a service downtime entry */
public static downtime_h.scheduled_downtime add_new_service_downtime(String host_name, String service_description, long entry_time, String author, String comment_data, long start_time, long end_time, int fixed,  long triggered_by,  long duration ){
    downtime_h.scheduled_downtime new_downtime;
    
    if(host_name==null || service_description==null)
        return null;
    
    /**** IMPLEMENTATION-SPECIFIC CALLS ****/
    new_downtime =  xdddefault.xdddefault_add_new_service_downtime(host_name,service_description,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration);
    
    /* send data to event broker */
    broker.broker_downtime_data(broker_h.NEBTYPE_DOWNTIME_ADD,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,common_h.SERVICE_DOWNTIME,host_name,service_description,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration,new_downtime.downtime_id,null);
    
    return new_downtime;
}



/******************************************************************/
/*********************** DELETION FUNCTIONS ***********************/
/******************************************************************/


/* deletes a scheduled host or service downtime entry from the list in memory */
public static int delete_downtime(int type, long downtime_id){
    int result;
    
    /* find the downtime we should remove */
    downtime_h.scheduled_downtime temp_downtime = find_downtime(type, downtime_id);
    
    /* remove the downtime from the list in memory */
    if(temp_downtime!=null){
        
        /* first remove the comment associated with this downtime */
        if(temp_downtime.type==common_h.HOST_DOWNTIME)
            comments.delete_host_comment(temp_downtime.comment_id);
        else
            comments.delete_service_comment(temp_downtime.comment_id);
        
        /* send data to event broker */
        broker.broker_downtime_data(broker_h.NEBTYPE_DOWNTIME_DELETE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,type,temp_downtime.host_name,temp_downtime.service_description,temp_downtime.entry_time,temp_downtime.author,temp_downtime.comment,temp_downtime.start_time,temp_downtime.end_time,temp_downtime.fixed,temp_downtime.triggered_by,temp_downtime.duration,downtime_id,null);
        
        scheduled_downtime_list.remove( temp_downtime );
        result=common_h.OK;
    }
    else
        result=common_h.ERROR;
    
    return result;
}


/* deletes a scheduled host downtime entry */
public static int delete_host_downtime(long downtime_id){
    int result;
    
    /* delete the downtime from memory */
    delete_downtime(common_h.HOST_DOWNTIME,downtime_id);
    
    /**** IMPLEMENTATION-SPECIFIC CALLS ****/
    result=xdddefault.xdddefault_delete_host_downtime(downtime_id);
    
    return result;
}


/* deletes a scheduled service downtime entry */
public static int delete_service_downtime(long downtime_id){
    int result;
    
    /* delete the downtime from memory */
    delete_downtime(common_h.SERVICE_DOWNTIME,downtime_id);
    
    /**** IMPLEMENTATION-SPECIFIC CALLS ****/
    result=xdddefault.xdddefault_delete_service_downtime(downtime_id);
    
    return result;
}


/******************************************************************/
/************************ INPUT FUNCTIONS *************************/
/******************************************************************/

/* reads all downtime data */
public static int read_downtime_data(String main_config_file){
    int result;
    
    /**** IMPLEMENTATION-SPECIFIC CALLS ****/
    result=xdddefault.xdddefault_read_downtime_data(main_config_file);
    
    return result;
}



/******************************************************************/
/******************** ADDITION FUNCTIONS **************************/
/******************************************************************/

/* adds a host downtime entry to the list in memory */
public static downtime_h.scheduled_downtime add_host_downtime(String host_name, long entry_time, String author, String comment_data, long start_time, long end_time, int fixed,  long triggered_by,  long duration,  long downtime_id){
    return add_downtime(common_h.HOST_DOWNTIME,host_name,null,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration,downtime_id);
}


/* adds a service downtime entry to the list in memory */
public static downtime_h.scheduled_downtime add_service_downtime(String host_name, String svc_description, long entry_time, String author, String comment_data, long start_time, long end_time, int fixed,  long triggered_by,  long duration,  long downtime_id){
    return add_downtime(common_h.SERVICE_DOWNTIME,host_name,svc_description,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration,downtime_id);
}


/* adds a host or service downtime entry to the list in memory */
public static downtime_h.scheduled_downtime add_downtime(int downtime_type, String host_name, String svc_description, long entry_time, String author, String comment_data, long start_time, long end_time, int fixed,  long triggered_by,  long duration,  long downtime_id){
    
    /* don't add triggered downtimes that don't have a valid parent */
    if(triggered_by>0  && find_downtime(common_h.ANY_DOWNTIME,triggered_by)==null)
        return null;
    
    /* allocate memory for the downtime */
    downtime_h.scheduled_downtime new_downtime = new downtime_h.scheduled_downtime();
    new_downtime.host_name=host_name;
    
    new_downtime.service_description=svc_description;
    new_downtime.author=author;
    new_downtime.comment=comment_data;
    
    new_downtime.type=downtime_type;
    new_downtime.entry_time=entry_time;
    new_downtime.start_time=start_time;
    new_downtime.end_time=end_time;
    new_downtime.fixed=(fixed>0)?common_h.TRUE:common_h.FALSE;
    new_downtime.triggered_by=triggered_by;
    new_downtime.duration=duration;
    new_downtime.downtime_id=downtime_id;
    new_downtime.comment_id=0;
    new_downtime.is_in_effect=common_h.FALSE;
    new_downtime.start_flex_downtime=common_h.FALSE;
    new_downtime.incremented_pending_downtime=common_h.FALSE;
    
    
    /* add new downtime to downtime list, sorted by start time */
    scheduled_downtime_list.add( new_downtime );

    if ( blue.is_core == true) {
       /* send data to event broker */
       broker.broker_downtime_data(broker_h.NEBTYPE_DOWNTIME_LOAD,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,downtime_type,host_name,svc_description,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration,downtime_id,null);
    }
    
    return new_downtime;
}

/******************************************************************/
/************************ SEARCH FUNCTIONS ************************/
/******************************************************************/

/* finds a specific downtime entry */
public static downtime_h.scheduled_downtime find_downtime(int type,  long downtime_id){
    downtime_h.scheduled_downtime temp_downtime;
    
    for ( ListIterator iter =  scheduled_downtime_list.listIterator(); iter.hasNext(); ) {
        temp_downtime = (downtime_h.scheduled_downtime) iter.next();
        if(type!=common_h.ANY_DOWNTIME && temp_downtime.type!=type)
            continue;
        if(temp_downtime.downtime_id==downtime_id)
            return temp_downtime;
    }
    
    return null;
}


/* finds a specific host downtime entry */
public static downtime_h.scheduled_downtime find_host_downtime( long downtime_id){
    
    return find_downtime(common_h.HOST_DOWNTIME,downtime_id);
}


/* finds a specific service downtime entry */
public static downtime_h.scheduled_downtime find_service_downtime(long downtime_id){
    
    return find_downtime(common_h.SERVICE_DOWNTIME,downtime_id);
}



/******************************************************************/
/********************* CLEANUP FUNCTIONS **************************/
/******************************************************************/

/* frees memory allocated for the scheduled downtime data */
public static void free_downtime_data(){
    scheduled_downtime_list.clear();
    return;
}


}