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

import org.blue.star.include.blue_h;
import org.blue.star.include.broker_h;
import org.blue.star.include.common_h;
import org.blue.star.include.nebcallbacks_h;
import org.blue.star.include.nebstructs_h;
import org.blue.star.include.objects_h;

public class broker
{
   
/******************************************************************/
/************************* EVENT FUNCTIONS ************************/
/******************************************************************/
   
   /**
    *  sends program data (starts, restarts, stops, etc) to broker.
    */
   public static void broker_program_state(int type, int flags, int attr, blue_h.timeval timestamp){
      nebstructs_h.nebstruct_process_data ds = new nebstructs_h.nebstruct_process_data();
      
      if(0==(blue.event_broker_options & broker_h.BROKER_PROGRAM_STATE))
         return;
      
      /* fill struct with relevant data */
      ds.type=type;
      ds.flags=flags;
      ds.attr=attr;
      ds.timestamp=get_broker_timestamp(timestamp);
      
      /* make callbacks */
      nebmods.neb_make_callbacks( nebcallbacks_h.NEBCALLBACK_PROCESS_DATA,ds);
      
   }
   
   /* send timed event data to broker */
   public static void broker_timed_event(int type, int flags, int attr, blue_h.timed_event event, blue_h.timeval timestamp){
      nebstructs_h.nebstruct_timed_event_data ds = new nebstructs_h.nebstruct_timed_event_data ();
      
      if(0==(blue.event_broker_options & broker_h.BROKER_TIMED_EVENTS))
         return;
      
      if(event==null)
         return;
      
      /* fill struct with relevant data */
      ds.type=type;
      ds.flags=flags;
      ds.attr=attr;
      ds.timestamp=get_broker_timestamp(timestamp);
      
      ds.event_type=event.event_type;
      ds.recurring=event.recurring;
      ds.run_time=event.run_time;
      ds.event_data=event.event_data;
      
      /* make callbacks */
      nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_TIMED_EVENT_DATA,ds);
      
      return;
   }
   

   
   /* send log data to broker */
   public static void broker_log_data(int type, int flags, int attr, String data, long data_type, long entry_time, blue_h.timeval timestamp){
      nebstructs_h.nebstruct_log_data ds = new nebstructs_h.nebstruct_log_data();
      
      if(0==(blue.event_broker_options & broker_h.BROKER_LOGGED_DATA))
         return;
      
      /* fill struct with relevant data */
      ds.type=type;
      ds.flags=flags;
      ds.attr=attr;
      ds.timestamp=get_broker_timestamp(timestamp);
      
      ds.entry_time=entry_time;
      ds.data_type=data_type;
      ds.data=data;
      
      /* make callbacks */
      nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_LOG_DATA,ds);
   }
   
   /* send system command data to broker */
   public static void broker_system_command(int type, int flags, int attr, blue_h.timeval start_time, blue_h.timeval end_time, double exectime, int timeout, int early_timeout, int retcode, String cmd, String output, blue_h.timeval timestamp){
      nebstructs_h.nebstruct_system_command_data ds = new nebstructs_h.nebstruct_system_command_data();
      
      if(0==(blue.event_broker_options & broker_h.BROKER_SYSTEM_COMMANDS))
         return;
      
      if(cmd==null)
         return;
      
      /* fill struct with relevant data */
      ds.type=type;
      ds.flags=flags;
      ds.attr=attr;
      ds.timestamp=get_broker_timestamp(timestamp);
      
      ds.start_time=start_time;
      ds.end_time=end_time;
      ds.timeout=timeout;
      ds.command_line=cmd;
      ds.early_timeout=early_timeout;
      ds.execution_time=exectime;
      ds.return_code=retcode;
      ds.output=output;
      
      /* make callbacks */
      nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_SYSTEM_COMMAND_DATA,ds);
   }



/* send event handler data to broker */
public static void broker_event_handler(int type, int flags, int attr, int eventhandler_type, Object data, int state, int state_type, blue_h.timeval start_time, blue_h.timeval end_time, double exectime, int timeout, int early_timeout, int retcode, String command, String cmdline, String output, blue_h.timeval timestamp){
	objects_h.service temp_service=null;
	objects_h.host temp_host=null;
	String command_name=null;
	String command_args=null;
     nebstructs_h.nebstruct_event_handler_data ds = new nebstructs_h.nebstruct_event_handler_data ();

	if(0==(blue.event_broker_options & broker_h.BROKER_EVENT_HANDLERS))
		return;
	
	if(data==null)
		return;
	
	/* get command name/args */
	if(command!=null){
	   String[] split = command.split( "[!]", 2 );
	   command_name=split[0];
	   command_args=split[1]; 
	}

	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.eventhandler_type=eventhandler_type;
	if(eventhandler_type==blue_h.SERVICE_EVENTHANDLER || eventhandler_type==blue_h.GLOBAL_SERVICE_EVENTHANDLER){
		temp_service=(objects_h.service)data;
		ds.host_name=temp_service.host_name;
		ds.service_description=temp_service.description;
	        }
	else{
		temp_host=(objects_h.host)data;
		ds.host_name=temp_host.name;
		ds.service_description=null;
	        }
	ds.state=state;
	ds.state_type=state_type;
	ds.start_time=start_time;
	ds.end_time=end_time;
	ds.timeout=timeout;
	ds.command_name=command_name;
	ds.command_args=command_args;
	ds.command_line=cmdline;
	ds.early_timeout=early_timeout;
	ds.execution_time=exectime;
	ds.return_code=retcode;
	ds.output=output;

	/* make callbacks */
	nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_EVENT_HANDLER_DATA,ds);

        }
 



/* send host check data to broker */
public static void broker_host_check(int type, int flags, int attr, objects_h.host hst, int check_type, int state, int state_type, blue_h.timeval start_time, blue_h.timeval end_time, String command, double latency, double exectime, int timeout, int early_timeout, int retcode, String cmdline, String output, String perfdata, blue_h.timeval timestamp)
{
	String command_buf=null;
	String command_name=null;
	String command_args="";
	
    nebstructs_h.nebstruct_host_check_data ds = new nebstructs_h.nebstruct_host_check_data ();

	if(0==(blue.event_broker_options & broker_h.BROKER_HOST_CHECKS))
		return;
	
	if(hst==null)
		return;

	/* get command name/args */
	
	if(command!=null)
	{
	         
	   /*
	   String[] split = command.split("[!]",2);
	   command_name=split[0];
	   command_args=split[1];
	   */
	   
	   //TODO - Rob 12/01/07 - Changed this because not all host checks must contain arguements.
	   //In many typical Blue/Nagios monitoring setups, a host check can be completed with statically
	   // defined parameters...we simply need a way of checking that the host is alive, this should
	   // not differ between hosts. We can however check for the presence of arguements, but should not
	   //expect them!
	   
	   String[] split = command.split("[!]",2);
	   command_name=split[0];
	   
	   if(split.length > 1)
		   command_args=split[1];
    }

	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.host_name=hst.name;
	ds.check_type=check_type;
	ds.current_attempt=hst.current_attempt;
	ds.max_attempts=hst.max_attempts;
	ds.state=state;
	ds.state_type=state_type;
	ds.timeout=timeout;
	ds.command_name=command_name;
	ds.command_args=command_args;
	ds.command_line=cmdline;
	ds.start_time=start_time;
	ds.end_time=end_time;
	ds.early_timeout=early_timeout;
	ds.execution_time=exectime;
	ds.latency=latency;
	ds.return_code=retcode;
	ds.output=output;
	ds.perf_data=perfdata;

	/* make callbacks */
	nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_HOST_CHECK_DATA, ds);
 }



/* send service check data to broker */
public static void broker_service_check(int type, int flags, int attr, objects_h.service svc, int check_type, blue_h.timeval start_time, blue_h.timeval end_time, String command, double latency, double exectime, int timeout, int early_timeout, int retcode, String cmdline, blue_h.timeval timestamp){
	String command_buf=null;
	String command_name=null;
	String command_args=null;
     nebstructs_h.nebstruct_service_check_data ds = new nebstructs_h.nebstruct_service_check_data();

	if(0==(blue.event_broker_options & broker_h.BROKER_SERVICE_CHECKS))
		return;
	
	if(svc==null)
		return;

	/* get command name/args */
	if(command!=null)
	{
       /* Updated to deal with service checks that have no parameters to their check command */
	   String[] split = command.split( "[!]", 2 );
       
       if(split.length == 2)
       {
    	   command_name=split[0];
    	   command_args=split[1];
       }
       else
       {
    	   command_name=split[0];
    	   command_args="";
       }
    }

	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.host_name=svc.host_name;
	ds.service_description=svc.description;
	ds.check_type=check_type;
	ds.current_attempt=svc.current_attempt;
	ds.max_attempts=svc.max_attempts;
	ds.state=svc.current_state;
	ds.state_type=svc.state_type;
	ds.timeout=timeout;
	ds.command_name=command_name;
	ds.command_args=command_args;
	ds.command_line=cmdline;
	ds.start_time=start_time;
	ds.end_time=end_time;
	ds.early_timeout=early_timeout;
	ds.execution_time=exectime;
	ds.latency=latency;
	ds.return_code=retcode;
	ds.output=svc.plugin_output;
	ds.perf_data=svc.perf_data;

	/* make callbacks */
	nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_SERVICE_CHECK_DATA, ds);

        }



/* send comment data to broker */
public static void broker_comment_data(int type, int flags, int attr, int comment_type, int entry_type, String host_name, String svc_description, long entry_time, String author_name, String comment_data, int persistent, int source, int expires, long expire_time, long comment_id, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_comment_data ds = new nebstructs_h.nebstruct_comment_data ();
   
   if(0==(blue.event_broker_options & broker_h.BROKER_COMMENT_DATA))
      return;
   
   /* fill struct with relevant data */
   ds.type=type;
   ds.flags=flags;
   ds.attr=attr;
   ds.timestamp=get_broker_timestamp(timestamp);
   
   ds.comment_type=comment_type;
   ds.entry_type=entry_type;
   ds.host_name=host_name;
   ds.service_description=svc_description;
   ds.entry_time=entry_time;
   ds.author_name=author_name;
   ds.comment_data=comment_data;
   ds.persistent=persistent;
   ds.source=source;
   ds.expires=expires;
   ds.expire_time=expire_time;
   ds.comment_id=comment_id;
   
   /* make callbacks */
   nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_COMMENT_DATA, ds);
   
}



/* send downtime data to broker */
public static void broker_downtime_data(int type, int flags, int attr, int downtime_type, String host_name, String svc_description, long entry_time, String author_name, String comment_data, long start_time, long end_time, int fixed, long triggered_by, long duration, long downtime_id, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_downtime_data ds = new nebstructs_h.nebstruct_downtime_data ();
   
   if(0==(blue.event_broker_options & broker_h.BROKER_DOWNTIME_DATA))
      return;
   
   /* fill struct with relevant data */
   ds.type=type;
   ds.flags=flags;
   ds.attr=attr;
   ds.timestamp=get_broker_timestamp(timestamp);
   
   ds.downtime_type=downtime_type;
   ds.host_name=host_name;
   ds.service_description=svc_description;
   ds.entry_time=entry_time;
   ds.author_name=author_name;
   ds.comment_data=comment_data;
   ds.start_time=start_time;
   ds.end_time=end_time;
   ds.fixed=fixed;
   ds.duration=duration;
   ds.triggered_by=triggered_by;
   ds.downtime_id=downtime_id;
   
   /* make callbacks */
   nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_DOWNTIME_DATA, ds);
   
}

/* send flapping data to broker */
public static void broker_flapping_data(int type, int flags, int attr, int flapping_type, Object data, double percent_change, double high_threshold, double low_threshold, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_flapping_data ds = new nebstructs_h.nebstruct_flapping_data ();
   objects_h.host temp_host=null;
   objects_h.service temp_service=null;
   
   if(0==(blue.event_broker_options & broker_h.BROKER_FLAPPING_DATA))
      return;
   
   if(data==null)
      return;
   
   /* fill struct with relevant data */
   ds.type=type;
   ds.flags=flags;
   ds.attr=attr;
   ds.timestamp=get_broker_timestamp(timestamp);
   
   ds.flapping_type=flapping_type;
   if(flapping_type==blue_h.SERVICE_FLAPPING){
      temp_service=(objects_h.service)data;
      ds.host_name=temp_service.host_name;
      ds.service_description=temp_service.description;
      ds.comment_id=temp_service.flapping_comment_id;
   }
   else{
      temp_host=(objects_h.host)data;
      ds.host_name=temp_host.name;
      ds.service_description=null;
      ds.comment_id=temp_host.flapping_comment_id;
   }
   ds.percent_change=percent_change;
   ds.high_threshold=high_threshold;
   ds.low_threshold=low_threshold;
   
   /* make callbacks */
   nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_FLAPPING_DATA,ds);
}


/* sends program status updates to broker */
public static void broker_program_status(int type, int flags, int attr, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_program_status_data ds = new nebstructs_h.nebstruct_program_status_data();

	if(0==(blue.event_broker_options & broker_h.BROKER_STATUS_DATA))
		return;

	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.program_start=blue.program_start;
	ds.pid=blue.blue_pid;
	ds.daemon_mode=blue.daemon_mode;
	ds.last_command_check=blue.last_command_check;
	ds.last_log_rotation=blue.last_log_rotation;
	ds.notifications_enabled=blue.enable_notifications;
	ds.active_service_checks_enabled=blue.execute_service_checks;
	ds.passive_service_checks_enabled=blue.accept_passive_service_checks;
	ds.active_host_checks_enabled=blue.execute_host_checks;
	ds.passive_host_checks_enabled=blue.accept_passive_host_checks;
	ds.event_handlers_enabled=blue.enable_event_handlers;
	ds.flap_detection_enabled=blue.enable_flap_detection;
	ds.failure_prediction_enabled=blue.enable_failure_prediction;
	ds.process_performance_data=blue.process_performance_data;
	ds.obsess_over_hosts=blue.obsess_over_hosts;
	ds.obsess_over_services=blue.obsess_over_services;
	ds.modified_host_attributes=blue.modified_host_process_attributes;
	ds.modified_service_attributes=blue.modified_service_process_attributes;
	ds.global_host_event_handler=blue.global_host_event_handler;
	ds.global_service_event_handler=blue.global_service_event_handler;

	/* make callbacks */
	nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_PROGRAM_STATUS_DATA, ds);

        }



/* sends host status updates to broker */
public static void broker_host_status(int type, int flags, int attr, objects_h.host hst, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_host_status_data ds = new nebstructs_h.nebstruct_host_status_data ();

	if(0==(blue.event_broker_options & broker_h.BROKER_STATUS_DATA))
		return;

	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.object_ptr = hst;

	/* make callbacks */
	nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_HOST_STATUS_DATA, ds);
        }



/* sends service status updates to broker */
public static void broker_service_status(int type, int flags, int attr, objects_h.service svc, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_service_status_data ds = new nebstructs_h.nebstruct_service_status_data ();

	if(0==(blue.event_broker_options & broker_h.BROKER_STATUS_DATA))
		return;

	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.object_ptr = svc;

	/* make callbacks */
	nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_SERVICE_STATUS_DATA, ds);

        }



/* send notification data to broker */
public static void broker_notification_data(int type, int flags, int attr, int notification_type, int reason_type, blue_h.timeval start_time, blue_h.timeval end_time, Object data, String ack_author, String ack_data, int escalated, int contacts_notified, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_notification_data ds = new nebstructs_h.nebstruct_notification_data ();
	objects_h.host temp_host=null;
	objects_h.service temp_service=null;

	if(0==(blue.event_broker_options & broker_h.BROKER_NOTIFICATIONS))
		return;
	
	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.notification_type=notification_type;
	ds.start_time=start_time;
	ds.end_time=end_time;
	ds.reason_type=reason_type;
	if(notification_type==blue_h.SERVICE_NOTIFICATION){
		temp_service=(objects_h.service)data;
		ds.host_name=temp_service.host_name;
		ds.service_description=temp_service.description;
		ds.state=temp_service.current_state;
		ds.output=temp_service.plugin_output;
	        }
	else{
		temp_host=(objects_h.host)data;
		ds.host_name=temp_host.name;
		ds.service_description=null;
		ds.state=temp_host.current_state;
		ds.output=temp_host.plugin_output;
	        }
	ds.ack_author=ack_author;
	ds.ack_data=ack_data;
	ds.escalated=escalated;
	ds.contacts_notified=contacts_notified;

	/* make callbacks */
	nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_NOTIFICATION_DATA, ds);
        }



/* send contact notification data to broker */
public static void broker_contact_notification_data(int type, int flags, int attr, int notification_type, int reason_type, blue_h.timeval start_time, blue_h.timeval end_time, Object data, objects_h.contact cntct, String ack_author, String ack_data, int escalated, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_contact_notification_data ds = new nebstructs_h.nebstruct_contact_notification_data ();
	objects_h.host temp_host=null;
	objects_h.service temp_service=null;

	if(0==(blue.event_broker_options & broker_h.BROKER_NOTIFICATIONS))
		return;
	
	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.notification_type=notification_type;
	ds.start_time=start_time;
	ds.end_time=end_time;
	ds.reason_type=reason_type;
	ds.contact_name=cntct.name;
	if(notification_type==blue_h.SERVICE_NOTIFICATION)
	{
		temp_service=(objects_h.service)data;
		ds.host_name=temp_service.host_name;
		ds.service_description=temp_service.description;
		ds.state=temp_service.current_state;
		ds.output=temp_service.plugin_output;
	}
	else
	{
		temp_host=(objects_h.host)data;
		ds.host_name=temp_host.name;
		ds.service_description=null;
		ds.state=temp_host.current_state;
		ds.output=temp_host.plugin_output;
	}
	
	ds.ack_author=ack_author;
	ds.ack_data=ack_data;
	ds.escalated=escalated;

	/* make callbacks */
	nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_CONTACT_NOTIFICATION_DATA,ds);

        }


/* send contact notification data to broker */
public static void broker_contact_notification_method_data(int type, int flags, int attr, int notification_type, int reason_type, blue_h.timeval start_time, blue_h.timeval end_time, Object data, objects_h.contact cntct, String command, String ack_author, String ack_data, int escalated, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_contact_notification_method_data ds = new nebstructs_h.nebstruct_contact_notification_method_data ();
	objects_h.host temp_host=null;
	objects_h.service temp_service=null;
	String command_buf=null;
	String command_name=null;
	String command_args="";

	if(0==(blue.event_broker_options & broker_h.BROKER_NOTIFICATIONS))
		return;

	/* get command name/args */
	if(command!=null)
	{
       String[] split = command.split("[!]",2);
       command_name=split[0];
       if(split.length > 1)
    	   command_args=split[1]; 
    }

	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.notification_type=notification_type;
	ds.start_time=start_time;
	ds.end_time=end_time;
	ds.reason_type=reason_type;
	ds.contact_name=cntct.name;
	ds.command_name=command_name;
	ds.command_args=command_args;
	if(notification_type==blue_h.SERVICE_NOTIFICATION)
	{
		temp_service=(objects_h.service)data;
		ds.host_name=temp_service.host_name;
		ds.service_description=temp_service.description;
		ds.state=temp_service.current_state;
		ds.output=temp_service.plugin_output;
	}
	else
	{
		temp_host=(objects_h.host)data;
		ds.host_name=temp_host.name;
		ds.service_description=null;
		ds.state=temp_host.current_state;
		ds.output=temp_host.plugin_output;
	}
	
	ds.ack_author=ack_author;
	ds.ack_data=ack_data;
	ds.escalated=escalated;

	/* make callbacks */
	nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_CONTACT_NOTIFICATION_METHOD_DATA, ds);

        }


/* sends adaptive programs updates to broker */
public static void broker_adaptive_program_data(int type, int flags, int attr, int command_type, long modhattr, long modhattrs, long modsattr, long modsattrs, String gheh, String gseh, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_adaptive_program_data ds = new nebstructs_h.nebstruct_adaptive_program_data();

	if(0==(blue.event_broker_options & broker_h.BROKER_ADAPTIVE_DATA))
		return;

	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.command_type=command_type;
	ds.modified_host_attribute=modhattr;
	ds.modified_host_attributes=modhattrs;
	ds.modified_service_attribute=modsattr;
	ds.modified_service_attributes=modsattrs;
	ds.global_host_event_handler=gheh;
	ds.global_service_event_handler=gseh;

	/* make callbacks */
	nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_ADAPTIVE_PROGRAM_DATA, ds);

        }


/* sends adaptive host updates to broker */
public static void broker_adaptive_host_data(int type, int flags, int attr, objects_h.host hst, int command_type, long modattr, long modattrs, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_adaptive_host_data ds= new nebstructs_h.nebstruct_adaptive_host_data ();

	if(0==(blue.event_broker_options & broker_h.BROKER_ADAPTIVE_DATA))
		return;

	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.command_type=command_type;
	ds.modified_attribute=modattr;
	ds.modified_attributes=modattrs;
	ds.object_ptr= hst;

	/* make callbacks */
	nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_ADAPTIVE_HOST_DATA, ds);

        }


/* sends adaptive service updates to broker */
public static void broker_adaptive_service_data(int type, int flags, int attr, objects_h.service svc, int command_type, long modattr, long modattrs, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_adaptive_service_data ds = new nebstructs_h.nebstruct_adaptive_service_data();

	if(0==(blue.event_broker_options & broker_h.BROKER_ADAPTIVE_DATA))
		return;

	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.command_type=command_type;
	ds.modified_attribute=modattr;
	ds.modified_attributes=modattrs;
	ds.object_ptr= svc;

	/* make callbacks */
	nebmods.neb_make_callbacks( nebcallbacks_h.NEBCALLBACK_ADAPTIVE_SERVICE_DATA, ds);

        }

/* sends external commands to broker */
public static void broker_external_command(int type, int flags, int attr, int command_type, long entry_time, String command_string, String command_args, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_external_command_data ds = new nebstructs_h.nebstruct_external_command_data ();

	if(0==(blue.event_broker_options & broker_h.BROKER_EXTERNALCOMMAND_DATA))
		return;

	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.command_type=command_type;
	ds.entry_time=entry_time;
	ds.command_string=command_string;
	ds.command_args=command_args;

	/* make callbacks */
	nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_EXTERNAL_COMMAND_DATA, ds);

        }

/* brokers aggregated status dumps */
public static void broker_aggregated_status_data(int type, int flags, int attr, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_aggregated_status_data ds = new nebstructs_h.nebstruct_aggregated_status_data ();

	if(0==(blue.event_broker_options & broker_h.BROKER_STATUS_DATA))
		return;

	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	/* make callbacks */
	nebmods.neb_make_callbacks( nebcallbacks_h.NEBCALLBACK_AGGREGATED_STATUS_DATA, ds);

        }


/* brokers retention data */
public static void broker_retention_data(int type, int flags, int attr, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_retention_data ds = new nebstructs_h.nebstruct_retention_data ();

	if(0==(blue.event_broker_options & broker_h.BROKER_RETENTION_DATA))
		return;

	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	/* make callbacks */
	nebmods.neb_make_callbacks( nebcallbacks_h.NEBCALLBACK_RETENTION_DATA, ds);

        }


/* send acknowledgement data to broker */
public static void broker_acknowledgement_data(int type, int flags, int attr, int acknowledgement_type, Object data, String ack_author, String ack_data, int subtype, int notify_contacts, int persistent_comment, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_acknowledgement_data ds = new nebstructs_h.nebstruct_acknowledgement_data ();
	objects_h.host temp_host=null;
	objects_h.service temp_service=null;

	if(0==(blue.event_broker_options & broker_h.BROKER_ACKNOWLEDGEMENT_DATA))
		return;
	
	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.acknowledgement_type=acknowledgement_type;
	if(acknowledgement_type==common_h.SERVICE_ACKNOWLEDGEMENT){
		temp_service=(objects_h.service)data;
		ds.host_name=temp_service.host_name;
		ds.service_description=temp_service.description;
		ds.state=temp_service.current_state;
	        }
	else{
		temp_host=(objects_h.host)data;
		ds.host_name=temp_host.name;
		ds.service_description=null;
		ds.state=temp_host.current_state;
	        }
	ds.author_name=ack_author;
	ds.comment_data=ack_data;
	ds.is_sticky=(subtype==common_h.ACKNOWLEDGEMENT_STICKY)?common_h.TRUE:common_h.FALSE;
	ds.notify_contacts=notify_contacts;
	ds.persistent_comment=persistent_comment;

	/* make callbacks */
	nebmods.neb_make_callbacks( nebcallbacks_h.NEBCALLBACK_ACKNOWLEDGEMENT_DATA, ds);
        }


/* send state change data to broker */
public static void broker_statechange_data(int type, int flags, int attr, int statechange_type, Object data, int state, int state_type, int current_attempt, int max_attempts, blue_h.timeval timestamp){
   nebstructs_h.nebstruct_statechange_data ds = new nebstructs_h.nebstruct_statechange_data ();
	objects_h.host temp_host=null;
	objects_h.service temp_service=null;

	if(0==(blue.event_broker_options & broker_h.BROKER_STATECHANGE_DATA))
		return;
	
	/* fill struct with relevant data */
	ds.type=type;
	ds.flags=flags;
	ds.attr=attr;
	ds.timestamp=get_broker_timestamp(timestamp);

	ds.statechange_type=statechange_type;
	if(statechange_type==blue_h.SERVICE_STATECHANGE){
		temp_service=(objects_h.service) data;
		ds.host_name=temp_service.host_name;
		ds.service_description=temp_service.description;
		ds.output=temp_service.plugin_output;
	        }
	else{
		temp_host=(objects_h.host)data;
		ds.host_name=temp_host.name;
		ds.service_description=null;
		ds.output=temp_host.plugin_output;
	        }
	ds.state=state;
	ds.state_type=state_type;
	ds.current_attempt=current_attempt;
	ds.max_attempts=max_attempts;

	/* make callbacks */
	nebmods.neb_make_callbacks(nebcallbacks_h.NEBCALLBACK_STATE_CHANGE_DATA, ds);

        }



/******************************************************************/
/************************ UTILITY FUNCTIONS ***********************/
/******************************************************************/

/* gets timestamp for use by broker */
public static blue_h.timeval get_broker_timestamp(blue_h.timeval timestamp){
   
   if ( timestamp == null )
      return new blue_h.timeval();
   else 
      return timestamp;
}

}
